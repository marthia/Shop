package com.poonehmedia.app.ui.editProfile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.model.FormData;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.data.repository.ProfileRepository;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;

import org.acra.ACRA;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Response;

@HiltViewModel
public class NewEditProfileViewModel extends BaseViewModel {
    private final ProfileRepository repository;
    private final DataController dataController;
    private final MutableLiveData<JsonObject> data = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> editData = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> submitAction = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> backAction = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> spinnerUpdate = new MutableLiveData<>();
    private final SavedStateHandle savedStateHandle;
    private final PreferenceManager preferenceManager;

    @Inject
    public NewEditProfileViewModel(ProfileRepository repository,
                                   DataController dataController,
                                   SavedStateHandle savedStateHandle,
                                   PreferenceManager preferenceManager,
                                   RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
        this.savedStateHandle = savedStateHandle;
        this.preferenceManager = preferenceManager;
    }

    public void resolveData() {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        if (navigationArgs != null && navigationArgs.getData() != null) {
            extractData(((JsonElement) navigationArgs.getData()));
        }
    }

    private void extractData(JsonElement body) {
        try {
            JsonArray dataItems = dataController.extractDataItemsAsJsonArray(body);
            JsonObject obj = dataItems.get(0).getAsJsonObject();
            editData.postValue(obj.get("fields").getAsJsonObject().getAsJsonObject("core").getAsJsonArray("fields"));
            JsonObject submitAction = dataController.extractInfo(body, "submitAction");
            this.submitAction.postValue(submitAction);
            JsonObject backAction = dataController.extractInfo(body, "backAction");
            this.submitAction.postValue(submitAction);
            this.backAction.postValue(backAction);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data in (NewEditProfileViewModel). data : " + body, e));
        }
    }

    private void handleUpdateSpinnerResponse(Response<JsonElement> jsonElementResponse) {
        String tag = jsonElementResponse.raw().request().tag(String.class);
        if (tag != null) {
            JsonObject result = jsonElementResponse.body().getAsJsonObject();
            result.addProperty("tag", tag);
            spinnerUpdate.postValue(result);
        }
    }

    public LiveData<JsonObject> getSpinnerUpdate() {
        return spinnerUpdate;
    }

    public LiveData<JsonObject> getData() {
        return data;
    }

    public LiveData<JsonObject> getSubmitAction() {
        return submitAction;
    }

    public LiveData<JsonObject> getBackAction() {
        return backAction;
    }

    public void fetchData() {
        requestData(
                repository.getEditProfileData(),
                response -> {
                    dataController.onSuccess(response);
                    extractData(response.body());
                },

                throwable -> {
                    dataController.onFailure(throwable);
                }
        );
    }

    public LiveData<JsonArray> getEditData() {
        return editData;
    }

    public JsonObject getPostData(Map<Integer, FormData> values) {

        JsonObject credentials = new JsonObject();

        for (Integer i : values.keySet()) {

            String fieldPostKey = values.get(i).getFieldNameKey();
            String key = values.get(i).getKey();
            String value = values.get(i).getValue();

            if (!key.equals(fieldPostKey))
                credentials.addProperty(fieldPostKey, key);
            else credentials.addProperty(fieldPostKey, value);
        }

        String password = credentials.get("password1").getAsString();
        credentials.addProperty("password2", password);

        JsonObject params = new JsonObject();
        params.add("jform", credentials);

        String token = preferenceManager.getToken();
        if (token != null && !token.equals(""))
            params.addProperty(token, "1");

        return params;
    }

    public void updateDependantSpinners(JsonObject item, String key) {
        boolean hasFieldOnChange = JsonHelper.has(item, "field_onchange");

        if (hasFieldOnChange) {

            JsonElement fieldOnchange1 = item.get("field_onchange");

            if (fieldOnchange1.isJsonObject()) {
                JsonObject fieldOnchange = fieldOnchange1.getAsJsonObject();

                String link = fieldOnchange.get("link").getAsString();
                JsonObject params = fieldOnchange.get("params").getAsJsonObject();
                boolean hasMethod = JsonHelper.has(fieldOnchange, "method");

                String method = "get";
                if (hasMethod)
                    method = "post";

                params.remove("namekey");
                params.addProperty("namekey", key);

                String tag = params.get("field_namekey").getAsString();
                requestData(
                        repository.postSpinner(tag, link, params, method),

                        response -> {
                            if (response.isSuccessful()) {
                                handleUpdateSpinnerResponse(response);
                            }
                        },
                        throwable -> {
                            Log.e("saveAddress", throwable.getMessage());
                        }
                );
            }
        }

    }
}
