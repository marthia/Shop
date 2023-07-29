package com.poonehmedia.app.data.repository;

import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

public class JsonObjectPagingSource extends RxPagingSource<Integer, JsonObject> {

    private final LoadingState loadingState;
    private final DataController dataController;
    private final String path;
    private final RestUtils restUtils;
    private final BaseApi baseApi;
    private final PagingMetadata pagingMetadata;
    private final JsonObject firstPageData;
    private final Map<String, Map<String, String>> filterParams;
    private String pageStartKey;
    private int limitStart = 0;
    private int limit;
    private boolean isFirstDataValid = true;

    public JsonObjectPagingSource(BaseApi baseApi,
                                  DataController dataController,
                                  RestUtils restUtils,
                                  String path,
                                  LoadingState loadingState,
                                  PagingMetadata pagingMetadata,
                                  JsonObject firstPageData,
                                  int limit,
                                  Map<String, Map<String, String>> filterParams, String pageStartKey) {
        this.baseApi = baseApi;
        this.dataController = dataController;
        this.restUtils = restUtils;
        this.path = path;
        this.loadingState = loadingState;
        this.pagingMetadata = pagingMetadata;
        this.firstPageData = firstPageData;
        this.limit = limit;
        this.filterParams = filterParams;
        this.pageStartKey = pageStartKey;
    }

    @NotNull
    @Override
    public Single<LoadResult<Integer, JsonObject>> loadSingle(@NotNull LoadParams<Integer> loadParams) {

        if (firstPageData != null && isFirstDataValid) {
            return Single.just( // position is the next page start position
                    toLoadResult(firstPageData)
            );
        }

        return baseApi.getFullPath(getUrl(limitStart, limit))
                .subscribeOn(Schedulers.io())
                .map((Response<JsonElement> response) -> {
                    dataController.onSuccess(response);
                    return toLoadResult(response.body().getAsJsonObject());
                })
                .onErrorReturn(LoadResult.Error::new);
    }

    private String getUrl(int position, int pageSize) {
        JsonObject params = new JsonObject();

        params.addProperty(pageStartKey, position);
        params.addProperty("limit", pageSize);

        if (filterParams != null && filterParams.size() > 0) {
            for (String key : filterParams.keySet()) {
                Map<String, String> paramsMap = filterParams.get(key);
                for (String paramKey : paramsMap.keySet()) {
                    params.addProperty(paramKey, paramsMap.get(paramKey));
                }
            }
        }

        return restUtils.resolveUrl(path, params);
    }

    private LoadResult<Integer, JsonObject> toLoadResult(JsonObject response) {

        // show shimmer loading only for the first page loading
        if (limitStart == 0) loadingState.loading(LoadingState.States.LOADING);


        List<JsonObject> content = dataController.extractDataItems(response);

        int loadSize = content.size();

        if (limitStart == 0 && loadSize == 0)
            loadingState.loading(LoadingState.States.EMPTY);
        else loadingState.loading(LoadingState.States.SUCCESS);

        JsonObject info = dataController.extractInfo(response, null);
        pagingMetadata.load(info);

        int total = dataController.getInfoInt(response, "total");
        limit = dataController.getInfoInt(response, "limit");
        int currentLimitStart = dataController.getInfoInt(response, "limitstart");
        limitStart = currentLimitStart + limit;

        // recover state
        if (firstPageData != null)
            isFirstDataValid = false;


        // avoid crash when data is not correct
        Integer nextKey = loadSize < limit ? null : limitStart;
        if (limit <= 0 || limitStart < 0 || total <= 0)
            nextKey = null;

        return new LoadResult.Page<>(content, null, nextKey);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, JsonObject> pagingState) {
        return pagingState.getAnchorPosition();
    }
}


