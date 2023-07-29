package com.poonehmedia.app.ui.manufacturer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;

import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.data.repository.JsonObjectPagingSource;
import com.poonehmedia.app.data.repository.LoadingState;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class ManufacturerViewModel extends BaseViewModel {
    private final BaseApi baseApi;
    private final DataController dataController;
    private final RestUtils restUtils;
    private final MutableLiveData<Boolean> errorResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emptyResponse = new MutableLiveData<>();
    // TODO PAGING NOT CORRECT
    private int PAGE_SIZE;
    private JsonObject firstPageData;
    private String path;
    private String pageStartKey;

    @Inject
    public ManufacturerViewModel(BaseApi baseApi,
                                 DataController dataController,
                                 RestUtils restUtils,
                                 SavedStateHandle savedStateHandle,
                                 RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.baseApi = baseApi;
        this.dataController = dataController;
        this.restUtils = restUtils;

        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        if (navigationArgs != null && navigationArgs.getData() != null) {
            path = navigationArgs.getLink();
            PAGE_SIZE = navigationArgs.getPageSize();
            pageStartKey = navigationArgs.getPagingStartKey();
            firstPageData = (JsonObject) navigationArgs.getData();
        }
    }

    public Pager<Integer, JsonObject> fetchData(boolean isRefreshCall) {

        return new Pager<>(
                new PagingConfig(PAGE_SIZE, 5, true, PAGE_SIZE),
                () -> new JsonObjectPagingSource(baseApi,
                        dataController,
                        restUtils,
                        path,
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
                        isRefreshCall ? null : firstPageData,
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

}
