package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class FavoriteRepository {

    private final DataController dataController;
    private final BaseApi baseApi;

    @Inject
    public FavoriteRepository(DataController dataController, BaseApi baseApi) {
        this.dataController = dataController;
        this.baseApi = baseApi;
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
}
