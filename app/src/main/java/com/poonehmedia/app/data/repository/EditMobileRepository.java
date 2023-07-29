package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class EditMobileRepository {

    private final DataController dataController;
    private final PreferenceManager preferenceManager;
    private final BaseApi baseApi;
    private final RestUtils restUtils;

    @Inject
    public EditMobileRepository(DataController dataController, RestUtils restUtils, PreferenceManager preferenceManager, BaseApi baseApi) {
        this.dataController = dataController;
        this.restUtils = restUtils;
        this.preferenceManager = preferenceManager;
        this.baseApi = baseApi;
    }

    public Single<Response<JsonElement>> getValidationCode(String phone) {
        JsonObject params = new JsonObject();

        JsonObject credentials = new JsonObject();
        credentials.addProperty("newmobile", phone);
        params.add("jform", credentials);

        String token = preferenceManager.getToken();
        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        // TODO REPLACE WITH SERVER LINK
        String url = restUtils.resolveUrl("index.php?option=com_users&task=profile.updatemobile");

        return baseApi.postFullPath(url, params);

    }

    public Single<Response<JsonElement>> validateCode(String code) {
        JsonObject params = new JsonObject();

        JsonObject credentials = new JsonObject();
        credentials.addProperty("mobilevaildate", code);

        params.add("jform", credentials);

        String token = preferenceManager.getToken();

        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        String url = restUtils.resolveUrl("index.php?option=com_users&task=profile.updatemobile");

        return baseApi.postFullPath(url, params);

    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }

}
