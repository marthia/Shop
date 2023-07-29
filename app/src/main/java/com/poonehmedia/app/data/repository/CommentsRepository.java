package com.poonehmedia.app.data.repository;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.data.model.Comment;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

@Singleton
public class CommentsRepository {

    private final DataController dataController;
    private final BaseApi baseApi;
    private final RestUtils restUtils;
    private JsonObject addCommentObj;
    private boolean isSubmitLocked = false;

    @Inject
    public CommentsRepository(BaseApi baseApi, DataController dataController, RestUtils restUtils) {
        this.baseApi = baseApi;
        this.dataController = dataController;
        this.restUtils = restUtils;
    }

    public Pager<Integer, Comment> get(PagingSource<Integer, Comment> pagingSource, int pageSize) {
        return new Pager<>(
                new PagingConfig(pageSize, 2, true, pageSize),
                () -> pagingSource
        );
    }

    public Single<Response<JsonElement>> doAction(JsonObject obj) {
        String url = restUtils.resolveUrl(obj.get("link").getAsString(), obj.get("params").getAsJsonObject());
        return baseApi.getFullPath(url);
    }

    public Single<Response<JsonElement>> submitComment(String name, String email, String body) {
        JsonObject params = new JsonObject();

        params.addProperty("name", name);
        params.addProperty("email", email);
        params.addProperty("comment", body);

//        String token = baseRepository.preferenceManager.getToken();
//        if (token != null && !token.equals(""))
//            params.addProperty(token, "1");

        JsonObject defaultParams = addCommentObj.get("params").getAsJsonObject();

        for (String key : defaultParams.keySet()) {
            params.addProperty(key, defaultParams.get(key).getAsString());
        }

        String url = restUtils.resolveUrl(addCommentObj.get("link").getAsString());

        return baseApi.postFullPath("comment", url, params);
    }

    public Single<Response<JsonElement>> reportInappropriateContent(JsonObject object, String name, String body) {
        JsonObject params = new JsonObject();

        params.addProperty("commentid", object.get("commentId").getAsString());
        params.addProperty("name", name);
        params.addProperty("reason", body);

        JsonObject defaultParams = object.get("params").getAsJsonObject();
        for (String key : defaultParams.keySet()) {
            params.addProperty(key, defaultParams.get(key).getAsString());
        }

        return baseApi.post(object.get("link").getAsString(), params);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public JsonObject getAddCommentObj() {
        return addCommentObj;
    }

    public void setAddCommentObj(JsonObject metadata) {
        addCommentObj = metadata;
    }

    public boolean isSubmitLocked() {
        return isSubmitLocked;
    }

    public void setSubmitLocked(boolean submitLocked) {
        isSubmitLocked = submitLocked;
    }
}
