package com.poonehmedia.app.ui.checkout;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.data.repository.CheckoutRepository;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;

import org.acra.ACRA;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

@HiltViewModel
public class CheckoutViewModel extends BaseViewModel {

    private final CheckoutRepository repository;
    private final DataController dataController;
    private final MutableLiveData<JsonArray> content = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> nextButtonInfo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isNextButtonEnabled = new MutableLiveData<>(true);
    private final MutableLiveData<String> responseMessage = new MutableLiveData<>();
    private final SavedStateHandle savedStateHandle;
    private String path;

    @Inject
    public CheckoutViewModel(CheckoutRepository repository,
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
            boolean isCheckOut = validateCheckoutData(body);

            if (isCheckOut) {
                JsonArray dataItems = dataController.extractDataItemsAsJsonArray(body);

                JsonObject nextButton = extractNextButton(body);

                this.content.postValue(dataItems);
                this.nextButtonInfo.postValue(nextButton);
                isNextButtonEnabled.setValue(true);
            } else {
                dataController.onFailure(new Throwable(dataController.getMessageOrEmpty(body)));
                responseMessage.postValue(dataController.getMessageOrEmpty(body));
            }
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data in (CheckoutViewModel). response: " + body.toString(), e));
        }
    }

    public JsonObject extractNextButton(JsonElement response) {
        try {

            if (JsonHelper.isEmptyOrNull(response))
                return null;

            JsonObject body = response.getAsJsonObject();
            if (!JsonHelper.has(body, "data"))
                return null;

            JsonObject data = body.get("data").getAsJsonObject();

            if (!JsonHelper.has(data, "info"))
                return null;

            JsonObject info = data.get("info").getAsJsonObject();
            if (!JsonHelper.has(info, "next_step_btn"))
                return null;

            return info.get("next_step_btn").getAsJsonObject();

        } catch (Exception e) {
            Log.e("nextButton", "NO NextButton PROVIDED");
            ACRA.getErrorReporter().handleException(new CrashReportException("extractNextButton (CheckoutViewModel).", e));
        }
        return null;
    }

    private boolean validateCheckoutData(JsonElement body) {
        try {

            JsonObject item = body.getAsJsonObject();
            boolean hasItems = JsonHelper.has(item, "data") && JsonHelper.has(item.get("data"), "items");
            if (hasItems) {
                return JsonHelper.has(item.get("data").getAsJsonObject()
                        .get("items").getAsJsonArray().get(0), "content");
            } else return false;

        } catch (Exception e) {
            Log.e("validateCheckout", e.getMessage());
            return false;
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
                    Log.i("cart", throwable.toString());
                }
        );

    }

    public LiveData<JsonObject> getNextButton() {
        return nextButtonInfo;
    }

    public void postAddress(JsonObject actions, String id) {

        isNextButtonEnabled.setValue(false);

        JsonObject params = actions.get("select").getAsJsonObject().get("params").getAsJsonObject();
        String path = actions.get("select").getAsJsonObject().get("link").getAsString();

        requestData(

                repository.post(path, params, id, "checkout[address][shipping]"),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }

                },
                throwable -> {
                    dataController.onFailure(throwable);
                    Log.i("address", throwable.toString());
                }
        );

    }

    public void postShipping(JsonObject actions, String id) {

        if (!actions.has("select")) return;

        isNextButtonEnabled.setValue(false);

        JsonObject params = actions.get("select").getAsJsonObject().get("params").getAsJsonObject();
        String path = actions.get("select").getAsJsonObject().get("link").getAsString();

        requestData(

                repository.post(path, params, id, "checkout[shipping][0][id]"),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }

                },
                throwable -> {
                    dataController.onFailure(throwable);
                    Log.i("shipping", throwable.toString());
                }
        );
    }

    public LiveData<String> getResponseMessage() {
        return responseMessage;
    }


    public LiveData<Boolean> getNextButtonStatus() {
        return isNextButtonEnabled;
    }

    public void setNextButtonStatus(boolean b) {
        isNextButtonEnabled.setValue(b);
    }

    public void postPayment(JsonObject actions, String id) {

        if (!actions.has("select")) return;

        isNextButtonEnabled.setValue(false);
        JsonObject params = actions.get("select").getAsJsonObject().get("params").getAsJsonObject();
        String path = actions.get("select").getAsJsonObject().get("link").getAsString();

        requestData(

                repository.post(path, params, id, "checkout[payment][id]"),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }

                },
                throwable -> {
                    dataController.onFailure(throwable);
                    Log.i("payment", throwable.toString());
                }
        );
    }

    public void postCoupon(JsonObject actions, String editValue) {
        if (!actions.has("params")) return;

        isNextButtonEnabled.setValue(false);
        JsonObject params = actions.get("params").getAsJsonObject();
        String path = actions.get("link").getAsString();

        Single<Response<JsonElement>> post = null;


        if (editValue != null)
            post = repository.post(path, params, editValue, "checkout[coupon]");
        else
            post = repository.post(path, params);

        requestData(
                post,

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractResult(response.body());
                    }

                },
                throwable -> {
                    dataController.onFailure(throwable);
                    Log.i("coupon", throwable.toString());
                }
        );
    }

    public LiveData<JsonArray> getData() {
        return content;
    }

    public void saveReturn(JsonObject returnObj) {
        repository.saveReturn(returnObj);
    }
}
