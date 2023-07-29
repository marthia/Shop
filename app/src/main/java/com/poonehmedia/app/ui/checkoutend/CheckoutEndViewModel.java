package com.poonehmedia.app.ui.checkoutend;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.repository.CheckoutRepository;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CheckoutEndViewModel extends BaseViewModel {

    private final CheckoutRepository repository;
    private final DataController dataController;
    private final MutableLiveData<JsonObject> data = new MutableLiveData<>();
    private final String path;

    @Inject
    public CheckoutEndViewModel(CheckoutRepository repository,
                                DataController dataController,
                                SavedStateHandle savedStateHandle,
                                RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
        path = savedStateHandle.get("link");
    }

    private void extractResult(JsonElement body) {
        try {
            JsonArray dataItems = dataController.extractDataItemsAsJsonArray(body);
            data.postValue(dataItems.get(0).getAsJsonObject());
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data in (CheckoutEndViewModel). data: " + body, e));
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
                    Log.i("checkoutEnd", throwable.toString());
                }
        );
    }


    public LiveData<JsonObject> getData() {
        return data;
    }

}
