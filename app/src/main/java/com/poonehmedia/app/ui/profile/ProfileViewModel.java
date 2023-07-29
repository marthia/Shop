package com.poonehmedia.app.ui.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.model.Response;
import com.poonehmedia.app.data.repository.ProfileRepository;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends BaseViewModel {
    private final ProfileRepository repository;
    private final DataController dataController;
    private final MutableLiveData<JsonObject> data = new MutableLiveData<>();
    private final MutableLiveData<Response> logoutResponse = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> editData = new MutableLiveData<>();
    private final MutableLiveData<String> editMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditSuccess = new MutableLiveData<>();
    private final SavedStateHandle savedStateHandle;
    private String path;

    @Inject
    public ProfileViewModel(ProfileRepository repository,
                            DataController dataController,
                            SavedStateHandle savedStateHandle,
                            RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
        this.savedStateHandle = savedStateHandle;
    }

    public void resolveData(boolean isEdit) {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        if (navigationArgs != null && navigationArgs.getData() != null) {
            path = navigationArgs.getLink();
        }
        if (isEdit)
            extractEditData(((JsonElement) navigationArgs.getData()));
        else
            extractResult(((JsonElement) navigationArgs.getData()));
    }

    private void extractResult(JsonElement body) {
        try {
            List<JsonObject> items = dataController.extractDataItems(body);
            data.postValue(items.get(0));
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data in (ProfileViewModel). data: " + body, e));
        }
    }

    private void extractEditData(JsonElement body) {
        try {
            JsonArray dataItems = dataController.extractDataItemsAsJsonArray(body);
            editData.postValue(dataItems.get(0).getAsJsonObject());
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data for edit in (ProfileViewModel). data: " + body, e));
        }
    }

    public void fetchData() {
        requestData(
                repository.getData(path),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }
                },
                throwable -> {
                    dataController.onFailure(throwable);
                    Log.e("profile", throwable.getMessage());
                }
        );
    }

    public LiveData<JsonObject> getData() {
        return data;
    }

    public void logout() {
        requestData(
                repository.logout(),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        repository.clearCookies();
                        logoutResponse.setValue(new Response(new JsonObject()));
                    } else logoutResponse.setValue(new Response("Error"));
                },

                throwable -> {
                    dataController.onFailure(throwable);
                    logoutResponse.postValue(new Response("Error"));
                }
        );
    }

    public LiveData<Response> onLogoutResponse() {
        return logoutResponse;
    }

    public JsonArray convertJsonObjectToJsonArray(JsonObject jsonObject) {
        JsonArray array = new JsonArray();

        for (String key : jsonObject.keySet()) {
            array.add(jsonObject.get(key));
        }
        return array;
    }

    public boolean getDebuggingStateSetting() {
        return repository.getDebuggingStateSetting();
    }

    public void setDebuggingState(boolean isChecked) {
        repository.setDebuggingState(isChecked);
    }

    public void editProfile(String path, String id, String name, String username, String email, String password) {
        requestData(

                repository.edit(path, id, name, username, email, password),

                response -> {
                    dataController.onSuccess(response);
                    boolean isError = dataController.isError(response);

                    if (!isError) {
                        String successMessage = dataController.getMessageOrEmpty(response.body());
                        editMessage.postValue(successMessage);
                    } else {
                        editMessage.postValue(dataController.getErrorMessageOrNull(response.body()));
                    }
                    isEditSuccess.postValue(!isError);
                },

                throwable -> {
                    dataController.onFailure(throwable);
                    isEditSuccess.postValue(false);
                }
        );
    }

    public LiveData<String> getEditResponse() {
        return editMessage;
    }

    public LiveData<Boolean> isEditSuccess() {
        return isEditSuccess;
    }

    public void fetchEditData() {
        requestData(

                repository.getEditProfileData(),

                response -> {
                    dataController.onSuccess(response);
                    extractEditData(response.body());
                },

                throwable -> {
                    dataController.onFailure(throwable);
                }
        );
    }

    public LiveData<JsonObject> getEditData() {
        return editData;
    }

}
