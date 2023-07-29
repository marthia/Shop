package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class CustomerClubRepository {


    private final BaseApi baseApi;
    private final DataController dataController;
    private final PreferenceManager preferenceManager;

    @Inject
    public CustomerClubRepository(BaseApi baseApi, DataController dataController, PreferenceManager preferenceManager) {
        this.baseApi = baseApi;
        this.dataController = dataController;
        this.preferenceManager = preferenceManager;
    }

    public Single<Response<JsonElement>> fetchData(String path) {
        return baseApi.get(path);
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
