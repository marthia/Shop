package com.poonehmedia.app.ui.club;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.repository.CustomerClubRepository;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CustomerClubViewModel extends BaseViewModel {

    private final CustomerClubRepository repository;
    private final DataController dataController;
    private final MutableLiveData<JsonObject> data = new MutableLiveData<>();
    private final SavedStateHandle savedStateHandle;
    private String path;

    @Inject
    public CustomerClubViewModel(CustomerClubRepository repository,
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
        if (navigationArgs != null) {
            path = navigationArgs.getLink();
            extractResult((JsonElement) navigationArgs.getData());
        }
    }

    private void extractResult(JsonElement body) {
        try {
            JsonArray dataItems = dataController.extractDataItemsAsJsonArray(body);
            data.postValue(dataItems.get(0).getAsJsonObject());
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data: CustomerClubViewModel response:" + body, e));
        }
    }

    public void fetchData() {
        requestData(repository.fetchData(path), response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }
                },
                throwable -> {
                    dataController.onFailure(throwable);
                });
    }

    public LiveData<JsonObject> getData() {
        return data;
    }

    public String getLanguage() {
        return repository.getLanguage();
    }
}
