package com.poonehmedia.app.ui.orders;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class OrderDetailsViewModel extends BaseViewModel {

    private final SavedStateHandle savedStateHandle;
    private final MutableLiveData<JsonObject> orderDetails = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> priceData = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> totalPrice = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> shippingItem = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> billingItem = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> products = new MutableLiveData<>();
    private final DataController dataController;
    private final BaseApi baseApi;
    private String path;

    @Inject
    public OrderDetailsViewModel(BaseApi baseApi,
                                 RoutePersistence persistence,
                                 DataController dataController,
                                 SavedStateHandle savedStateHandle) {
        super(persistence, savedStateHandle);
        this.baseApi = baseApi;
        this.dataController = dataController;
        this.savedStateHandle = savedStateHandle;
    }

    public void resolveData() {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);

        if (navigationArgs != null && navigationArgs.getData() != null) {
            path = navigationArgs.getLink();
            extractData(((JsonElement) navigationArgs.getData()));
        }
    }

    private void extractData(JsonElement body) {
        List<JsonObject> items = dataController.extractDataItems(body);
        if (items.size() > 0) {
            JsonObject item = items.get(0);
            orderDetails.postValue(item);
            priceData.postValue(item.get("price").getAsJsonObject());
            totalPrice.postValue(item.get("price").getAsJsonObject().get("total").getAsJsonObject());
            shippingItem.postValue(item.get("shipping_address").getAsJsonObject());
            billingItem.postValue(item.get("billing_address").getAsJsonObject());
            products.postValue(item.get("products").getAsJsonArray());
        }
    }

    public LiveData<JsonObject> getOrderDetails() {
        return orderDetails;
    }

    public LiveData<JsonObject> getPriceData() {
        return priceData;
    }


    public LiveData<JsonObject> getShippingData() {
        return shippingItem;
    }


    public LiveData<JsonObject> getBillingData() {
        return billingItem;
    }

    public LiveData<JsonObject> getTotalPrice() {
        return totalPrice;
    }

    public LiveData<JsonArray> getProducts() {
        return products;
    }

    public void fetchData() {
        requestData(
                baseApi.get(path),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractData(response.body());
                    }
                },
                throwable -> {
                    dataController.onFailure(throwable);
                    Log.i("address", throwable.toString());
                }
        );

    }
}
