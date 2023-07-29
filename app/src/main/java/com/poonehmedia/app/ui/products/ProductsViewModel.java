package com.poonehmedia.app.ui.products;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.data.model.GroupedList;
import com.poonehmedia.app.data.model.ProductsPagingMetaData;
import com.poonehmedia.app.data.repository.LoadingState;
import com.poonehmedia.app.data.repository.ProductsPagingSource;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class ProductsViewModel extends BaseViewModel {

    private final MutableLiveData<Boolean> errorResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emptyResponse = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> filterData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> sortData = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalCount = new MutableLiveData<>();
    private final BaseApi baseApi;
    private final RestUtils restUtils;
    private final DataController dataController;
    private String pageStartKey;
    private int PAGE_SIZE;
    private String path;
    private JsonObject firstPageData;

    @Inject
    public ProductsViewModel(BaseApi baseApi,
                             RestUtils restUtils,
                             DataController dataController,
                             SavedStateHandle savedStateHandle,
                             RoutePersistence routePersistence) {

        super(routePersistence, savedStateHandle);
        this.baseApi = baseApi;
        this.restUtils = restUtils;
        this.dataController = dataController;

        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        if (navigationArgs != null) {
            path = navigationArgs.getLink();
            PAGE_SIZE = navigationArgs.getPageSize();
            pageStartKey = navigationArgs.getPagingStartKey();
            firstPageData = (JsonObject) navigationArgs.getData();
        }
    }

    public Pager<Integer, GroupedList> fetchData(boolean isRefreshCall, JsonObject filters) {

        return new Pager<>(
                new PagingConfig(PAGE_SIZE, 5, true, PAGE_SIZE),
                () -> new ProductsPagingSource(baseApi,
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
                            ProductsPagingMetaData metaData = (ProductsPagingMetaData) o;
                            if (metaData.isShouldBindFilters()) {
                                filterData.postValue(metaData.getFilters());
                                sortData.postValue(metaData.getSorts());
                            }
                            totalCount.postValue(metaData.getTotalCount());
                        },
                        isRefreshCall ? null : firstPageData,
                        PAGE_SIZE,
                        filters,
                        pageStartKey)
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

    public LiveData<JsonArray> getFilters() {
        return filterData;
    }

    public LiveData<JsonArray> getSorts() {
        return sortData;
    }

    public LiveData<Integer> getTotalCount() {
        return totalCount;
    }
}
