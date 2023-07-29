package com.poonehmedia.app.data.repository;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.data.model.AppParams;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.DeviceInfoManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.subjects.Subject;
import retrofit2.Response;

@Singleton
public class SettingsRepository {

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final Subject<Integer> levelSubject = PublishSubject.create();
    private final SingleSubject<Boolean> errorSubject = SingleSubject.create();
    private final SingleSubject<AppParams> forceUpdate = SingleSubject.create();
    private final DeviceInfoManager deviceInfoManager;
    private final RestUtils restUtils;
    private final BaseApi baseApi;
    private final DataController dataController;
    private JsonArray languageItems = new JsonArray();
    private JsonArray menuItems = new JsonArray();
    private int level = 0;
    private boolean isForceUpdateHandled = false;

    @Inject
    public SettingsRepository(RestUtils restUtils,
                              BaseApi baseApi,
                              DataController dataController,
                              DeviceInfoManager deviceInfoManager
    ) {
        this.restUtils = restUtils;
        this.baseApi = baseApi;
        this.dataController = dataController;
        this.deviceInfoManager = deviceInfoManager;
    }

    public JsonArray getLanguageItems() {
        return languageItems;
    }

    public void setLanguageItems(JsonArray languageItems) {
        this.languageItems = languageItems;
    }

    public JsonArray getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(JsonArray menuItems) {
        this.menuItems = menuItems;
    }

    public void init() {
        mDisposable.add(sendDeviceInfo()
                .doOnSuccess(this::handlePostDeviceInfoResponse)
                .doOnError(dataController::onFailure)
                // get languages
                .flatMap(jsonElementResponse -> getLanguages())
                .doOnSuccess(this::handleLanguagesResponse)
                .doOnError(dataController::onFailure)
                // get menus
                .flatMap(jsonElementResponse -> getMenus())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleMenusResponse, throwable -> {
                    dataController.onFailure(throwable);
                    errorSubject.onSuccess(true);
                    Log.e("errorInitial", throwable.getMessage());
                })
        );
    }

    private void handleMenusResponse(Response<JsonElement> jsonElementResponse) {
        dataController.onSuccess(jsonElementResponse);
        JsonArray menuItems = dataController.extractDataItemsAsJsonArray(jsonElementResponse);
        setMenuItems(menuItems);
        levelSubject.onNext(++level);
        // reset level for future as this class is  a singleton
        level = 0;
    }

    private void handlePostDeviceInfoResponse(Response<JsonElement> jsonElementResponse) {
        dataController.onSuccess(jsonElementResponse);
        if (jsonElementResponse.body().getAsJsonObject().has("params"))
            levelSubject.onNext(++level);
    }

    private void handleLanguagesResponse(Response<JsonElement> jsonElementResponse) {
        dataController.onSuccess(jsonElementResponse);

        JsonArray languageItems = dataController.extractDataItemsAsJsonArray(jsonElementResponse);

        setLanguageItems(languageItems);
        levelSubject.onNext(++level);
        handleForceUpdate(jsonElementResponse);

    }

    private void handleForceUpdate(Response<JsonElement> data) {
        try {
            AppParams forceUpdate = dataController.extractForceUpdate(data);
            if (forceUpdate != null) {
                if (forceUpdate.getAppVersion() > Float.parseFloat(deviceInfoManager.getAppVersion()))
                    this.forceUpdate.onSuccess(forceUpdate);
            }
        } catch (Exception e) {
            Log.e("forceUpdate", e.getMessage());
            this.forceUpdate.onError(e);
        }
    }

    public Single<Response<JsonElement>> sendDeviceInfo() {

        JsonObject params = new JsonObject();
        params.addProperty("device_details", deviceInfoManager.getDeviceFeatures());

        String url = restUtils.resolveUrl("index.php?option=com_rppamspro&task=device.authenticate");

        return baseApi.postFullPath(url, params);
    }

    public Single<Response<JsonElement>> getLanguages() {

        String url = restUtils.resolveUrl("index.php?option=com_rppamspro&task=language.getlist");

        return baseApi.getFullPath(url);
    }

    public Single<Response<JsonElement>> getMenus() {

        String url = restUtils.resolveUrl("index.php?option=com_rppamspro&task=menu.getlist");

        return baseApi.getFullPath(url);
    }

    public SingleSubject<AppParams> getForceUpdate() {
        return forceUpdate;
    }

    public SingleSubject<Boolean> getIsError() {
        return errorSubject;
    }

    public Subject<Integer> getLevel() {
        return levelSubject;
    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }

    public void setHasForceUpdateHandled(boolean b) {
        this.isForceUpdateHandled = b;
    }

    public boolean isForceUpdateHandled() {
        return isForceUpdateHandled;
    }

}
