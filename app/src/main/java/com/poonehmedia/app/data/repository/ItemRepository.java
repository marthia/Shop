package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class ItemRepository {

    private final DataController dataController;
    private final BaseApi baseApi;
    private final PreferenceManager preferenceManager;

    @Inject
    public ItemRepository(DataController dataController, PreferenceManager preferenceManager, BaseApi baseApi) {
        this.dataController = dataController;
        this.preferenceManager = preferenceManager;
        this.baseApi = baseApi;
    }

    public Single<Response<JsonElement>> getData(String path) {
        return baseApi.get(path);
    }

    public String getLanguage() {
        return preferenceManager.getLanguage();
    }

    public Single<Response<JsonElement>> post(String path, JsonObject params) {
        return baseApi.post(path, params);
    }

    public Single<Response<JsonElement>> postDataWithFile(String path,
                                                          Map<String, File> files,
                                                          JsonObject params,
                                                          String mimeType
    ) {

        List<MultipartBody.Part> parts = new ArrayList<>();

        for (String key : files.keySet()) {
            String replacedKey = key.replaceFirst("\\[\\d+]$", "[]");
            MultipartBody.Part part = MultipartBody.Part.createFormData(
                    replacedKey,
                    files.get(key).getName(),
                    RequestBody.create(files.get(key), MediaType.parse(mimeType))
            );
            parts.add(part);
        }

        Map<String, RequestBody> body = new HashMap<>();
        for (String key : params.keySet()) {
            RequestBody requestBody = RequestBody.create(params.get(key).getAsString(), MediaType.parse("text/plain"));
            body.put(key, requestBody);
        }

        return baseApi.postDataWithFile(path, parts, body);
    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }
}
