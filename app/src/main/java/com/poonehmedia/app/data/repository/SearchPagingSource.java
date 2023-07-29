package com.poonehmedia.app.data.repository;

import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.service.SearchApi;
import com.poonehmedia.app.util.base.DataController;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

public class SearchPagingSource extends RxPagingSource<Integer, JsonObject> {

    private final DataController dataController;
    private final SearchApi api;
    private final String path;
    private final String query;
    private final LoadingState loadingState;
    private int limitStart = 0;
    private int limit;

    public SearchPagingSource(DataController dataController,
                              SearchApi api,
                              String path,
                              int pageSize,
                              String query,
                              LoadingState loadingState
    ) {
        this.dataController = dataController;
        this.api = api;
        this.path = path;
        this.query = query;
        this.loadingState = loadingState;
        this.limit = pageSize;
    }

    @NotNull
    @Override
    public Single<LoadResult<Integer, JsonObject>> loadSingle(@NotNull LoadParams<Integer> loadParams) {

        return api.search(path, query, limitStart, limit)
                .subscribeOn(Schedulers.io())
                .map(this::toLoadResult)
                .onErrorReturn(throwable -> {
                    dataController.onFailure(throwable);
                    loadingState.loading(LoadingState.States.ERROR);
                    return new LoadResult.Error<>(throwable);
                });
    }

    private LoadResult<Integer, JsonObject> toLoadResult(Response<JsonElement> response) {
        dataController.onSuccess(response);

        // show shimmer loading only for the first page loading
        if (limitStart == 0)
            loadingState.loading(LoadingState.States.LOADING);

        List<JsonObject> list = dataController.extractDataItems(response.body());
        int total = dataController.getInfoInt(response, "total");
        limit = dataController.getInfoInt(response, "limit");
        int currentLimitStart = dataController.getInfoInt(response, "limitstart");
        limitStart = currentLimitStart + limit;

        if (currentLimitStart == 0 && list.size() == 0)
            loadingState.loading(LoadingState.States.EMPTY);
        else loadingState.loading(LoadingState.States.SUCCESS);

        // avoid crash when data is not correct
        Integer nextKey = list.size() < limit ? null : limitStart;
        if (limit <= 0 || limitStart < 0 || total <= 0)
            nextKey = null;

        return new LoadResult.Page<>(list, null, nextKey);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, JsonObject> pagingState) {
        return pagingState.getAnchorPosition();
    }
}
