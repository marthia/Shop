package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class ProductRepository {

    private final BaseApi baseApi;
    private final PreferenceManager preferenceManager;
    private final RestUtils restUtils;

    @Inject
    public ProductRepository(BaseApi baseApi, PreferenceManager preferenceManager, RestUtils restUtils) {
        this.baseApi = baseApi;
        this.preferenceManager = preferenceManager;
        this.restUtils = restUtils;
    }

    public Single<Response<JsonElement>> updateCart(String path, int quantity) {
        String s = path.replaceFirst("quantity-", "quantity-" + quantity);
        return baseApi.get("updateCart", s);
    }

    public Single<Response<JsonElement>> updateCart(String path, int quantity, JsonObject params) {
        for (String key :
                params.keySet()) {
            if (key.contains("checkout[cart][item]")) {
                params.remove(key);
                params.addProperty(key, quantity);
                break;
            }
        }

        String link = restUtils.resolveUrl(path, params);
        return baseApi.getFullPath("updateCart", link);
    }

    public Single<Response<JsonElement>> addToWishCart(String path, JsonObject params) {
        String link = restUtils.resolveUrl(path, params);
        return baseApi.getFullPath("updateCart", link);
    }

    public Single<Response<JsonElement>> getData(String path) {
        return baseApi.get(path).retry(5);
    }


    public Single<Response<JsonElement>> addToWaitList(String path, JsonObject params) {
        String token = preferenceManager.getToken();
        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        return baseApi.post("updateCart", path, params);
    }

    public boolean isLogin() {
        return !preferenceManager.getUser().isEmpty();
    }

    public String getLanguage() {
        return preferenceManager.getLanguage();
    }

    public void saveReturnAction(JsonObject returnAction) {
        preferenceManager.saveReturn(returnAction);
    }
}
