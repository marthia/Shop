package com.poonehmedia.app.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.repository.HomeRepository;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class HomeViewModel extends BaseViewModel {

    private final String TAG = getClass().getSimpleName();
    private final HomeRepository repository;
    private final DataController dataController;
    private final MutableLiveData<JsonArray> itemModulesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isErrorLiveData = new MutableLiveData<>(false);
    private final SavedStateHandle savedStateHandle;
    private String path;

    @Inject
    public HomeViewModel(HomeRepository repository,
                         DataController dataController,
                         SavedStateHandle savedStateHandle,
                         RoutePersistence routePersistence) {

        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
        this.savedStateHandle = savedStateHandle;
    }

    public void resolveData() {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        if (navigationArgs != null && navigationArgs.getData() != null) {
            path = navigationArgs.getLink();
            extractResult(((JsonElement) navigationArgs.getData()));
        }
    }

    private void extractResult(JsonElement body) {
        try {
            JsonArray itemModules = extractItemModules(body);
            itemModulesLiveData.postValue(itemModules);
            isErrorLiveData.postValue(false);
        } catch (Exception e) {
            Log.e(TAG, "Could not bind home data");
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not bind home data in (HomeViewModel).", e));
        }
    }

    private JsonArray extractItemModules(JsonElement body) {
        try {
            JsonObject element = body.getAsJsonObject();
            return element.get("data").getAsJsonObject().get("modules").getAsJsonArray();

        } catch (Exception e) {
            Log.e("itemModules", e.getMessage());
        }
        return null;
    }

    public void fetchData() {
        requestData(
                repository.fetchData(path),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }
                }
                , throwable -> {
                    dataController.onFailure(throwable);
                    isErrorLiveData.postValue(true);
                    Log.e("home", throwable.getMessage());
                }
        );
    }


    public LiveData<JsonArray> getData() {
        return itemModulesLiveData;
    }

    public LiveData<Boolean> getIsError() {
        return isErrorLiveData;
    }

    public String getLanguage() {
        return repository.getLanguage();
    }
}
