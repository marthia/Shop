package com.poonehmedia.app.ui.product;

import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.repository.ProductRepository;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class ProductCartViewModel extends BaseViewModel {

    private final ProductRepository repository;
    private final DataController dataController;
    private final MutableLiveData<JsonObject> rawData = new MutableLiveData<>();
    private final MutableLiveData<Integer> productQuantity = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> hasMaxQuantityReached = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasMinQuantityReached = new MutableLiveData<>(true);
    private final MutableLiveData<Drawable> quantityLeftIcon = new MutableLiveData<>();
    private final MutableLiveData<Boolean> progressUpdatingCart = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> cartInfoCount = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> cartInfo = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> updateCartResponse = new MutableLiveData<>();


    @Inject
    public ProductCartViewModel(ProductRepository repository,
                                DataController dataController,
                                SavedStateHandle savedStateHandle,
                                RoutePersistence routePersistence) {

        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
    }


    public LiveData<Boolean> getProgressUpdatingCart() {
        return progressUpdatingCart;
    }

    public void setProgressUpdatingCart(Boolean progressUpdatingCart) {
        this.progressUpdatingCart.postValue(progressUpdatingCart);
    }

    public LiveData<Integer> getProductQuantity() {
        return productQuantity;
    }

    public void incrementProductQuantity() {
        Integer currentQuantity = productQuantity.getValue();
        productQuantity.setValue(++currentQuantity);
    }

    public void updateProductQuantity(int count) {
        productQuantity.setValue(count);
    }

    public void subtractProductQuantity() {
        Integer currentQuantity = productQuantity.getValue();
        productQuantity.setValue(--currentQuantity);
    }

    public LiveData<Boolean> isMinQuantity(JsonObject data) {
        int min = data.getAsJsonObject().get("min_quantity").getAsInt();


        return Transformations.switchMap(productQuantity, input -> {

            hasMinQuantityReached.postValue(input == min);


            return hasMinQuantityReached;
        });
    }

    public LiveData<Boolean> isMaxQuantity(JsonObject data) {

        int max = data.getAsJsonObject().get("max_quantity").getAsInt();

        return Transformations.switchMap(productQuantity, input -> {
            if (max != 0) {
                hasMaxQuantityReached.postValue(input == max);
            }
            return hasMaxQuantityReached;
        });
    }

    public LiveData<Drawable> getQuantityLeftIcon() {
        return quantityLeftIcon;
    }

    public void setQuantityLeftIcon(Drawable drawable) {
        quantityLeftIcon.postValue(drawable);
    }

    public void updateCart(String path, String productId) {

        requestData(
                repository.updateCart(path, productQuantity.getValue()),

                response -> {
                    dataController.onSuccess(response);

                    if (response.isSuccessful()) {

                        rawData.setValue(response.body().getAsJsonObject());

                        JsonObject cartInfo = dataController.extractModule(response.body(), "shopMiniCart");
                        this.cartInfo.postValue(cartInfo);

                        String s = dataController.extractAddToCartFailureMessage(cartInfo);
                        if (!s.isEmpty()) dataController.onFailure(new Throwable(s));

                        else {
                            int cartTotalCount = dataController.getCartTotalCount(cartInfo);

                            int countForProductId = dataController.getCountForProductId(cartInfo, productId);
                            progressUpdatingCart.postValue(false);
                            productQuantity.postValue(countForProductId);
                            cartInfoCount.postValue(cartTotalCount);
                        }

                    } else dataController.onFailure(new Throwable("خطایی اتفاق افتاد"));
                },
                throwable -> {
                    Log.e("errorCart", throwable.getMessage());
                    dataController.onFailure(throwable);
                    progressUpdatingCart.postValue(false);
                }
        );

    }

    public LiveData<JsonObject> getRawData() {
        return rawData;
    }

    public void updateCartCart(String path, int quantity, JsonObject params) {

        requestData(
                repository.updateCart(path, quantity, params),
                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        JsonArray items = dataController.extractDataItemsAsJsonArray(response);
                        updateCartResponse.postValue(items.get(0).getAsJsonObject());
                    }
                },
                throwable -> {
                    Log.e("errorCart", throwable.getMessage());
                    dataController.onFailure(throwable);
                }
        );
    }

    public LiveData<JsonObject> getUpdateCartResponse() {
        return updateCartResponse;
    }

    public LiveData<Integer> getCartCount() {
        return cartInfoCount;
    }

    public LiveData<JsonObject> getCartInfo() {
        return cartInfo;
    }

}
