package com.poonehmedia.app.ui.signupIn;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginRegisterViewModel extends BaseViewModel {

    private final String TAG = getClass().getSimpleName();
    private final MutableLiveData<JsonObject> submitAction = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> resetAction = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> backAction = new MutableLiveData<>();
    private final MutableLiveData<String> subtitle = new MutableLiveData<>();
    private final PreferenceManager preferenceManager;
    private final DataController dataController;
    private final SavedStateHandle savedStateHandle;

    @Inject
    public LoginRegisterViewModel(RoutePersistence persistence,
                                  PreferenceManager preferenceManager,
                                  DataController dataController,
                                  SavedStateHandle savedStateHandle) {
        super(persistence, savedStateHandle);
        this.preferenceManager = preferenceManager;
        this.dataController = dataController;
        this.savedStateHandle = savedStateHandle;
    }

    public void resolveData() {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        if (navigationArgs != null && navigationArgs.getData() != null) {
            extractResult((JsonObject) navigationArgs.getData());
        }
    }

    private void extractResult(JsonObject object) {
        try {

            JsonObject submitAction = dataController.extractInfo(object, "submitAction");
            this.submitAction.postValue(submitAction);

            JsonObject resetAction = dataController.extractInfo(object, "resetAction");
            this.resetAction.postValue(resetAction);

            JsonObject backAction = dataController.extractInfo(object, "backAction");
            this.backAction.postValue(backAction);

            String description = dataController.getInfoString(object, "decription");
            subtitle.postValue(description);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException(TAG, e));
        }
    }

    public LiveData<JsonObject> getSubmitAction() {
        return submitAction;
    }

    public LiveData<JsonObject> getResetAction() {
        return resetAction;
    }

    public LiveData<String> getSubtitle() {
        return subtitle;
    }

    public LiveData<JsonObject> getBackAction() {
        return backAction;
    }

    public JsonObject getPostData(String password) {
        JsonObject params = new JsonObject();

        JsonObject credentials = new JsonObject();
        credentials.addProperty("password", password);

        params.add("jform", credentials);

        String token = preferenceManager.getToken();

        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        return params;
    }
}
