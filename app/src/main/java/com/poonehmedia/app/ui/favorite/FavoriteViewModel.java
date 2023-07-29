package com.poonehmedia.app.ui.favorite;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.repository.FavoriteRepository;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FavoriteViewModel extends BaseViewModel {


    private final FavoriteRepository repository;
    private final DataController dataController;
    private final MutableLiveData<JsonObject> data = new MutableLiveData<>();
    private final SavedStateHandle savedStateHandle;
    private String path;

    @Inject
    public FavoriteViewModel(
            FavoriteRepository repository,
            DataController dataController,
            SavedStateHandle savedStateHandle,
            RoutePersistence routePersistence
    ) {
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
            JsonArray array = dataController.extractDataItemsAsJsonArray(body);
            data.postValue(array.get(0).getAsJsonObject());
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not extract data from received Json : FavoriteViewModel", e));
        }
    }

    public void fetchData() {
        requestData(
                repository.fetchData(path),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }
                },
                throwable -> {
                    dataController.onFailure(throwable);
                    Log.i("favorite", throwable.toString());
                }
        );

    }

    public LiveData<JsonObject> getData() {
        return data;
    }

}
