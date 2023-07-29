package com.poonehmedia.app.ui.signupIn;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.repository.AccountRepository;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class ValidationViewModel extends BaseViewModel {

    private final String TAG = getClass().getSimpleName();
    private final AccountRepository repository;
    private final DataController dataController;
    private final MutableLiveData<JsonObject> submitAction = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> tokenInfo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> tokenResent = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> backAction = new MutableLiveData<>();
    private final SavedStateHandle savedStateHandle;
    private final PreferenceManager preferenceManager;


    @Inject
    public ValidationViewModel(AccountRepository repository,
                               DataController dataController,
                               PreferenceManager preferenceManager,
                               SavedStateHandle savedStateHandle,
                               RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
        this.preferenceManager = preferenceManager;
        this.savedStateHandle = savedStateHandle;
    }

    public void resolveData() {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        extractResult((JsonObject) navigationArgs.getData());
    }

    private void extractResult(JsonObject object) {
        try {
            JsonObject submitAction = dataController.extractInfo(object, "submitAction");
            JsonObject tokenInfo = dataController.extractInfo(object, "tokenInfo");
            JsonObject backAction = dataController.extractInfo(object, "backAction");
            this.submitAction.postValue(submitAction);
            this.tokenInfo.postValue(tokenInfo);
            this.backAction.postValue(backAction);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException(TAG, e));
        }
    }

    public LiveData<JsonObject> getSubmitAction() {
        return submitAction;
    }

    public LiveData<JsonObject> getTokenInfo() {
        return tokenInfo;
    }

    public LiveData<JsonObject> getBackAction() {
        return backAction;
    }

    public JsonObject getPostData(String password) {
        JsonObject params = new JsonObject();

        JsonObject credentials = new JsonObject();
        credentials.addProperty("token", password);

        params.add("jform", credentials);

        String token = preferenceManager.getToken();

        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        return params;
    }

    public LiveData<Boolean> getTokenResent() {
        return tokenResent;
    }

    public void resendCode(String resendCodeLink) {
        requestData(repository.resendCode(resendCodeLink),
                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        tokenResent.postValue(true);
                    } else tokenResent.postValue(false);
                },
                throwable -> {
                    tokenResent.postValue(false);
                    dataController.onFailure(throwable);
                });
    }
}