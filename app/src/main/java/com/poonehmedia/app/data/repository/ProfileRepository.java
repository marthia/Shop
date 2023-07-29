package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class ProfileRepository {

    private final DataController dataController;
    private final PreferenceManager preferenceManager;
    private final RestUtils restUtils;
    private final BaseApi baseApi;

    @Inject
    public ProfileRepository(DataController dataController, PreferenceManager preferenceManager, RestUtils restUtils, BaseApi baseApi) {
        this.dataController = dataController;
        this.preferenceManager = preferenceManager;
        this.restUtils = restUtils;
        this.baseApi = baseApi;
    }

    public Single<Response<JsonElement>> getData(String path) {
        String link = restUtils.resolveUrl(path);
        return baseApi.getFullPath(link);
    }

    public Single<Response<JsonElement>> getEditProfileData() {

        String url = restUtils.resolveUrl("index.php?option=com_users&view=profile&layout=edit");
        return baseApi.getFullPath(url);
    }

    public Single<Response<JsonElement>> logout() {

        String url = restUtils.resolveUrl("index.php?option=com_users&task=user.logout");

        return baseApi.getFullPath(url).retry(5);
    }

    public boolean getDebuggingStateSetting() {
        return preferenceManager.getDebuggingState();
    }

    public void setDebuggingState(boolean isChecked) {
        preferenceManager.setDebuggingState(isChecked);
    }

    public Single<Response<JsonElement>> edit(String path, String id, String name, String username, String email, String password) {
        JsonObject params = new JsonObject();

        JsonObject credentials = new JsonObject();
        credentials.addProperty("id", id);
        credentials.addProperty("name", name);
        credentials.addProperty("username", username);
        credentials.addProperty("password1", password);
        credentials.addProperty("password2", password);
        credentials.addProperty("email1", email);
        credentials.addProperty("email2", email);
        params.add("jform", credentials);

        String token = preferenceManager.getToken();
        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        String url = restUtils.resolveUrl("index.php?task=profile.save");

        return baseApi.postFullPath(url, params).retry(5);

    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }

    public void clearCookies() {
        dataController.clearCookies();
    }

    public Single<Response<JsonElement>> postSpinner(String tag, String path, JsonObject params, String type) {

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
}
