package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class ShopUserAddressesRepository {

    private final DataController dataController;
    private final PreferenceManager preferenceManager;
    private final BaseApi baseApi;
    private final RestUtils restUtils;

    @Inject
    public ShopUserAddressesRepository(DataController dataController, PreferenceManager preferenceManager, BaseApi baseApi, RestUtils restUtils) {
        this.dataController = dataController;
        this.preferenceManager = preferenceManager;
        this.baseApi = baseApi;
        this.restUtils = restUtils;
    }

    public Single<Response<JsonElement>> fetchData(String path) {
        return baseApi.get(path);
    }

    public Single<Response<String>> deleteItem(JsonObject selectedItem) {
        return baseApi.deleteAddress(selectedItem.get("deleteLink").getAsString());
    }

    public Single<Response<JsonElement>> save(String tag, String path, JsonObject params, String type) {

        if (type.equals("get")) {
            String link = restUtils.resolveUrl(path, params);
            return baseApi.getFullPath(tag, link);
        } else {
            String token = preferenceManager.getToken();
            if (token != null && !token.equals(""))
                params.addProperty(token, "1");
            return baseApi.postFullPath(tag, path, params);
        }
    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }
}
