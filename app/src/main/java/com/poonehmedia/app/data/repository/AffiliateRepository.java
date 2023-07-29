package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.service.BaseApi;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class AffiliateRepository {

    private final BaseApi baseApi;

    @Inject
    public AffiliateRepository(BaseApi baseApi) {
        this.baseApi = baseApi;
    }

    public Single<Response<JsonElement>> fetchData(String path) {
        return baseApi.get(path);
    }

    public Single<Response<JsonElement>> saveSettings(String path, JsonObject params) {
        return baseApi.post(path, params);
    }
}
