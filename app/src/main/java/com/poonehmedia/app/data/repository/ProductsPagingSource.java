package com.poonehmedia.app.data.repository;

import android.util.Log;

import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.data.model.GroupedList;
import com.poonehmedia.app.data.model.ProductsPagingMetaData;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;

import org.acra.ACRA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

public class ProductsPagingSource extends RxPagingSource<Integer, GroupedList> {

    private final Set<String> titles = new HashSet<>();
    private final LoadingState loadingState;
    private final DataController dataController;
    private final String path;
    private final RestUtils restUtils;
    private final BaseApi baseApi;
    private final PagingMetadata pagingMetadata;
    private final JsonObject firstPageData;
    private final JsonObject filterParams;
    private String pageStartKey;
    private int limitStart = 0;
    private int limit;
    private boolean isFirstDataValid = true;

    public ProductsPagingSource(BaseApi baseApi,
                                DataController dataController,
                                RestUtils restUtils,
                                String path,
                                LoadingState loadingState,
                                PagingMetadata pagingMetadata,
                                JsonObject firstPageData,
                                int limit,
                                JsonObject filterParams, String pageStartKey) {
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
    public Single<LoadResult<Integer, GroupedList>> loadSingle(@NotNull LoadParams<Integer> loadParams) {

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
            for (Map.Entry<String, JsonElement> entry : filterParams.entrySet()) {
                params.addProperty(entry.getKey(), entry.getValue().getAsString());
            }
        }

        return restUtils.resolveUrl(path, params);
    }

    private LoadResult<Integer, GroupedList> toLoadResult(JsonObject response) {

        List<GroupedList> list = new ArrayList<>();
        ProductsPagingMetaData productsPagingMetaData = new ProductsPagingMetaData();

        // only wake the live data for the first load
        if (limitStart == 0)
            loadingState.loading(LoadingState.States.LOADING);
        else productsPagingMetaData.setShouldBindFilters(false);

        int emittedSize = dataController.getDataItemsCount(response);

        // essential for showing in FilterCategoriesFragment bottom bar
        int total = dataController.getInfoInt(response, "total");
        productsPagingMetaData.setTotalCount(total);

        if (limitStart == 0 && emittedSize == 0)
            loadingState.loading(LoadingState.States.EMPTY);
        else loadingState.loading(LoadingState.States.SUCCESS);

        if (emittedSize > 0) {

            // only at first request
            if (limitStart == 0) {

                JsonObject categories = dataController.extractModule(response, "ShopCategories2");
                list = bindItemsToGroupList(response, categories);

                // extract filters
                JsonObject shopFilters = dataController.extractModule(response, "shopfilters");
                if (JsonHelper.isNotEmptyNorNull(shopFilters))
                    productsPagingMetaData.setFilters(shopFilters.get("content").getAsJsonArray());

                // extract sorts
                JsonObject shopSorts = dataController.extractModule(response, "shopsorts");
                if (JsonHelper.isNotEmptyNorNull(shopSorts))
                    productsPagingMetaData.setSorts(shopSorts.get("content").getAsJsonArray());


            } else {
                // on next pages without category
                list = bindItemsToGroupList(response, null);
            }

            limit = dataController.getInfoInt(response, "limit");
            limitStart = dataController.getInfoInt(response, "limitstart") + limit;

        }

        pagingMetadata.load(productsPagingMetaData);

        // recover state
        if (firstPageData != null)
            isFirstDataValid = false;

        // avoid crash when data is not correct
        Integer nextKey = emittedSize < limit ? null : limitStart;
        if (limit <= 0 || limitStart < 0 || total <= 0)
            nextKey = null;

        return new LoadResult.Page<>(list, null, nextKey);
    }

    private List<GroupedList> bindItemsToGroupList(JsonObject object, JsonObject categories) {
        List<GroupedList> result = new ArrayList<>();

        try {

            JsonArray items = object.get("data").getAsJsonObject()
                    .get("items").getAsJsonArray();

            // first off we must add the category to show it at top
            if (categories != null) {
                GroupedList groupedList = new GroupedList();
                groupedList.setId(UUID.randomUUID().toString());
                groupedList.setViewType(GroupedList.ViewType.CATEGORY);
                groupedList.setCategory(categories);
                result.add(groupedList);
            }

            if (items.get(0).getAsJsonObject().has("rows")) {
                for (int j = 0; j < items.size(); j++) {
                    JsonObject item = items.get(j).getAsJsonObject();

                    String title = item.get("title").getAsString();
                    if (!titles.contains(title)) {
                        GroupedList header = new GroupedList();
                        header.setId(UUID.randomUUID().toString());
                        header.setViewType(GroupedList.ViewType.HEADER);
                        header.setHeaderTitle(title);
                        result.add(header);

                        titles.add(title);
                    }

                    for (int i = 0; i < item.get("rows").getAsJsonArray().size(); i++) {
                        GroupedList data = new GroupedList();
                        data.setId(UUID.randomUUID().toString());
                        data.setViewType(GroupedList.ViewType.CONTENT);
                        data.setData(item.get("rows").getAsJsonArray().get(i).getAsJsonObject());

                        result.add(data);
                    }
                }
            } else {
                for (int i = 0; i < items.getAsJsonArray().size(); i++) {
                    GroupedList data = new GroupedList();
                    data.setId(UUID.randomUUID().toString());
                    data.setViewType(GroupedList.ViewType.CONTENT);
                    data.setData(items.getAsJsonArray().get(i).getAsJsonObject());

                    result.add(data);
                }
            }
            return result;

        } catch (Exception e) {
            Log.e("sectionedList", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not bind products Json to GroupedList item, response-> " + object.toString(), e));
            return result;
        }
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, GroupedList> pagingState) {
        return pagingState.getAnchorPosition();
    }
}


