package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class CheckoutRepository {

    private final RestUtils restUtils;
    private final DataController dataController;
    private final BaseApi baseApi;
    private final PreferenceManager preferenceManager;

    @Inject
    public CheckoutRepository(DataController dataController, PreferenceManager preferenceManager, BaseApi baseApi, RestUtils restUtils) {
        this.dataController = dataController;
        this.preferenceManager = preferenceManager;
        this.baseApi = baseApi;
        this.restUtils = restUtils;
    }

    public Single<Response<JsonElement>> fetchData(String path) {
        return baseApi.get(path).retry(5);
    }

    public Single<Response<JsonElement>> post(String path, JsonObject params, String value, String key) {
        for (String eachKey : params.keySet()) {

            if (eachKey.contains(key)) {
                params.remove(eachKey);
                params.addProperty(key, value);
                break;
            }
        }

        String link = restUtils.resolveUrl(path, params);
        return baseApi.getFullPath(link);
    }

    public Single<Response<JsonElement>> post(String path, JsonObject params) {
        String link = restUtils.resolveUrl(path, params);
        return baseApi.getFullPath(link);
    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }

    public void saveReturn(JsonObject returnObj) {
        preferenceManager.saveReturn(returnObj);
    }
}
