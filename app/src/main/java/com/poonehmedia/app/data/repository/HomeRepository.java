package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class HomeRepository {

    private final DataController dataController;
    private final BaseApi baseApi;
    private final PreferenceManager preferenceManager;

    @Inject
    public HomeRepository(DataController dataController, BaseApi baseApi, PreferenceManager preferenceManager) {
        this.dataController = dataController;
        this.baseApi = baseApi;
        this.preferenceManager = preferenceManager;
    }

    public Single<Response<JsonElement>> fetchData(String path) {
        return baseApi.get(path).retry(5);
    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }

    public String getLanguage() {
        return preferenceManager.getLanguage();
    }
}
