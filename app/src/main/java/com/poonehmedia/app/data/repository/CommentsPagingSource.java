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
import com.poonehmedia.app.data.model.Comment;
import com.poonehmedia.app.data.model.CommentsPagingMetaData;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

public class CommentsPagingSource extends RxPagingSource<Integer, Comment> {

    private final LoadingState loadingState;
    private final DataController dataController;
    private final String path;
    private final RestUtils restUtils;
    private final BaseApi baseApi;
    private final PagingMetadata pagingMetadata;
    private final JsonObject firstPageData;
    private int limitStart = 0;
    private int limit;
    private String pageStartKey;
    private boolean isFirstDataValid = true;

    public CommentsPagingSource(BaseApi baseApi,
                                DataController dataController,
                                RestUtils restUtils,
                                String path,
                                LoadingState loadingState,
                                PagingMetadata pagingMetadata,
                                JsonObject firstPageData,
                                int limit, String pageStartKey) {
        this.baseApi = baseApi;
        this.dataController = dataController;
        this.restUtils = restUtils;
        this.path = path;
        this.loadingState = loadingState;
        this.pagingMetadata = pagingMetadata;
        this.firstPageData = firstPageData;
        this.limit = limit;
        this.pageStartKey = pageStartKey;
    }

    @NotNull
    @Override
    public Single<LoadResult<Integer, Comment>> loadSingle(@NotNull LoadParams<Integer> loadParams) {

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

        return restUtils.resolveUrl(path, params);
    }

    private LoadResult<Integer, Comment> toLoadResult(JsonObject response) {

        // show shimmer loading only for the first page loading
        if (limitStart == 0) {
            try {
                loadingState.loading(LoadingState.States.LOADING);

                JsonObject addComment = dataController.extractInfo(response, "addComment");
                JsonObject commentsLocked = dataController.extractInfo(response, "commentsLocked");

                boolean isLocked = false;
                String lockedText = "";

                if (commentsLocked != null) {
                    isLocked = true;
                    lockedText = commentsLocked.get("text").getAsString();
                }

                pagingMetadata.load(new CommentsPagingMetaData(addComment, isLocked, lockedText));
            } catch (Exception e) {
                Log.e("Comments", e.getMessage());
                ACRA.getErrorReporter().handleException(new CrashReportException("Could not extract data from received Json : CommentsPagingSource \nreceived Json-> " + response, e));
            }
        }


        JsonArray content = dataController.extractDataItemsAsJsonArray(response);

        List<Comment> list = dataController.bindCommentsJsonToObject(content);
        int loadSize = list.size();

        if (limitStart == 0 && loadSize == 0)
            loadingState.loading(LoadingState.States.EMPTY);
        else loadingState.loading(LoadingState.States.SUCCESS);

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

        return new LoadResult.Page<>(list, null, nextKey);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, Comment> pagingState) {
        return pagingState.getAnchorPosition();
    }
}


