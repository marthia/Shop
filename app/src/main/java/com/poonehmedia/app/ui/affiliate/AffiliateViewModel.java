package com.poonehmedia.app.ui.affiliate;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.data.repository.AffiliateRepository;
import com.poonehmedia.app.data.repository.JsonObjectPagingSource;
import com.poonehmedia.app.data.repository.LoadingState;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import org.acra.ACRA;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class AffiliateViewModel extends BaseViewModel {

    private final AffiliateRepository repository;
    private final DataController dataController;
    private final Context context;
    private final MutableLiveData<JsonObject> data = new MutableLiveData<>();
    private final MutableLiveData<String> saveResponse = new MutableLiveData<>();
    private final MutableLiveData<List<JsonObject>> clickResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> errorResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emptyResponse = new MutableLiveData<>();
    private final BaseApi baseApi;
    private final SavedStateHandle savedStateHandle;
    private final RestUtils restUtils;
    private int PAGE_SIZE;
    private String path;
    private String pageStartKey;

    @Inject
    public AffiliateViewModel(AffiliateRepository repository,
                              DataController dataController,
                              BaseApi baseApi,
                              RestUtils restUtils,
                              SavedStateHandle savedStateHandle,
                              @ApplicationContext Context context,
                              RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
        this.baseApi = baseApi;
        this.restUtils = restUtils;
        this.savedStateHandle = savedStateHandle;
        this.context = context;
    }

    public void resolveData() {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        if (navigationArgs != null && navigationArgs.getData() != null) {
            path = navigationArgs.getLink();
            PAGE_SIZE = navigationArgs.getPageSize();
            pageStartKey = navigationArgs.getPagingStartKey();
            extractResult(((JsonElement) navigationArgs.getData()));
        }
    }

    private void extractResult(JsonElement body) {
        try {
            JsonArray items = dataController.extractDataItemsAsJsonArray(body);
            data.postValue(items.get(0).getAsJsonObject());
            isLoading.postValue(false);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data in (AffiliateViewModel). response: " + body.toString(), e));
        }
    }

    public void fetchData() {
        isLoading.postValue(true);
        requestData(
                repository.fetchData(path),
                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }
                }, dataController::onFailure
        );
    }

    public LiveData<JsonObject> subscribeData() {
        return data;
    }

    public void saveSettings(String path, JsonObject params) {
        isLoading.postValue(true);
        requestData(
                repository.saveSettings(path, params),
                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        saveResponse.postValue(context.getString(R.string.save_success));
                        JsonArray items = dataController.extractDataItemsAsJsonArray(response.body());
                        data.postValue(items.get(0).getAsJsonObject());
                        isLoading.postValue(false);
                    }
                },
                error -> {
                    dataController.onFailure(error);
                    saveResponse.postValue(context.getString(R.string.save_failure));
                    isLoading.postValue(false);
                });
    }

    public LiveData<String> subscribePostResponse() {
        return saveResponse;
    }

    public Pager<Integer, JsonObject> fetchClickData(String link) {

        return new Pager<>(
                new PagingConfig(PAGE_SIZE, 5, true, PAGE_SIZE),
                () -> new JsonObjectPagingSource(baseApi,
                        dataController,
                        restUtils,
                        link,
                        states -> {
                            if (states.equals(LoadingState.States.SUCCESS)) {
                                loadingResponse.postValue(false);
                                errorResponse.postValue(false);
                                emptyResponse.postValue(false);
                            } else {
                                loadingResponse.postValue(states.equals(LoadingState.States.LOADING));
                                errorResponse.postValue(states.equals(LoadingState.States.ERROR));
                                emptyResponse.postValue(states.equals(LoadingState.States.EMPTY));
                            }
                        },
                        o -> {

                        },
                        null,
                        PAGE_SIZE,
                        null, pageStartKey)
        );
    }

    public LiveData<Boolean> getLoadingResponse() {
        return loadingResponse;
    }

    public LiveData<Boolean> getEmptyResponse() {
        return emptyResponse;
    }

    public LiveData<Boolean> getErrorResponse() {
        return errorResponse;
    }

    public LiveData<Boolean> isPosting() {
        return isLoading;
    }
}
