package com.poonehmedia.app.data.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class AccountRepository {

    private final DataController dataController;
    private final BaseApi baseApi;
    private final RestUtils restUtils;
    private final PreferenceManager preferenceManager;

    @Inject
    public AccountRepository(DataController dataController, RestUtils restUtils, PreferenceManager preferenceManager, BaseApi baseApi) {
        this.dataController = dataController;
        this.restUtils = restUtils;
        this.preferenceManager = preferenceManager;
        this.baseApi = baseApi;
    }

    public Single<Response<JsonElement>> login(String user, String pass) {
        JsonObject params = new JsonObject();
        params.addProperty("username", user);
        params.addProperty("password", pass);
        params.addProperty("remember", "true");
        String token = preferenceManager.getToken();
        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        String url = restUtils.resolveUrl("index.php?option=com_users&task=user.login");

        return baseApi.postFullPath(url, params);
    }

    public Single<Response<JsonElement>> signUp(String phoneNumberText, String passwordText) {
        JsonObject params = new JsonObject();

        JsonObject credentials = new JsonObject();
        credentials.addProperty("username", phoneNumberText);
        credentials.addProperty("password1", passwordText);

        params.add("jform", credentials);

        String token = preferenceManager.getToken();

        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        String url = restUtils.resolveUrl("index.php?option=com_users&task=registration.register");

        return baseApi.postFullPath(url, params);
    }

    public Single<Response<JsonElement>> getResetPasswordValidationCode(String phone) {
        JsonObject params = new JsonObject();

        JsonObject credentials = new JsonObject();
        credentials.addProperty("email", phone);
        params.add("jform", credentials);

        String token = preferenceManager.getToken();
        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        String url = restUtils.resolveUrl("index.php?option=com_users&task=reset.request");

        return baseApi.postFullPath(url, params);
    }

    public Single<Response<JsonElement>> finalizedResetPassword(String password) {

        JsonObject params = new JsonObject();
        JsonObject credentials = new JsonObject();
        credentials.addProperty("password1", password);
        credentials.addProperty("password2", password);

        params.add("jform", credentials);

        String token = preferenceManager.getToken();
        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        String url = restUtils.resolveUrl("index.php?option=com_users&task=reset.complete");

        return baseApi.postFullPath(url, params);
    }

    public Single<Response<JsonElement>> signupValidate(String code) {

        JsonObject params = new JsonObject();

        JsonObject credentials = new JsonObject();
        credentials.addProperty("mobilevalidate", code);

        params.add("jform", credentials);

        String token = preferenceManager.getToken();

        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        String url = restUtils.resolveUrl("index.php?option=com_users&task=registration.register");

        return baseApi.postFullPath(url, params);
    }

    public Single<Response<JsonElement>> resetPasswordValidation(String code) {
        JsonObject params = new JsonObject();

        JsonObject credentials = new JsonObject();
        credentials.addProperty("token", code);

        params.add("jform", credentials);

        String token = preferenceManager.getToken();

        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        String url = restUtils.resolveUrl("index.php?option=com_users&task=reset.confirm");

        return baseApi.postFullPath(url, params);

    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }

    public Single<Response<JsonElement>> resendCode(String path) {
        return baseApi.get(path);
    }
}
