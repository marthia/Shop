package com.poonehmedia.app.ui.address;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.DataState;
import com.poonehmedia.app.data.model.FormData;
import com.poonehmedia.app.data.repository.ShopUserAddressesRepository;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;

import org.acra.ACRA;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Response;

@HiltViewModel
public class ShopUserAddressesViewModel extends BaseViewModel {


    private final ShopUserAddressesRepository repository;
    private final DataController dataController;
    private final Context context;
    private final MutableLiveData<JsonObject> data = new MutableLiveData<>();
    private final MutableLiveData<DataState> deleteResponse = new MutableLiveData<>(new DataState.Nothing());
    private final MutableLiveData<JsonObject> editItem = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> spinnerUpdate = new MutableLiveData<>();
    private final MutableLiveData<DataState> saveResponse = new MutableLiveData<>(new DataState.Nothing());
    private final SavedStateHandle savedStateHandle;
    private JsonObject addressActions = null;
    private String path;

    @Inject
    public ShopUserAddressesViewModel(ShopUserAddressesRepository repository,
                                      DataController dataController,
                                      SavedStateHandle savedStateHandle,
                                      @ApplicationContext Context context,
                                      RoutePersistence routePersistence
    ) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
        this.savedStateHandle = savedStateHandle;
        this.context = context;
    }

    public void resolveData(boolean isEdit) {
        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);
        if (navigationArgs != null) {
            path = navigationArgs.getLink();
            if (isEdit)
                extractEditData((JsonElement) navigationArgs.getData());
            else
                extractResult((JsonElement) navigationArgs.getData());
        }
    }

    private void extractResult(JsonElement object) {
        try {
            JsonArray items = dataController.extractDataItemsAsJsonArray(object);
            data.postValue(items.get(0).getAsJsonObject());
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data in (ShopUserAddressesViewModel). response : " + object, e));
        }
    }

    private void extractEditData(JsonElement body) {
        JsonArray array = dataController.extractDataItemsAsJsonArray(body);
        JsonObject items = array.get(0).getAsJsonObject();
        addressActions = items.get("address_action").getAsJsonObject();
        editItem.postValue(items);
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
                    Log.i("address", throwable.toString());
                }
        );
    }

    public LiveData<JsonObject> getAddress() {
        return data;
    }

    public void fetchEditData() {
        requestData(
                repository.fetchData(path),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        extractEditData(response.body());
                    }
                },

                throwable -> {
                    dataController.onFailure(throwable);
                    Log.i("editAddress", throwable.toString());
                }
        );
    }

    public LiveData<JsonObject> getEditIem() {
        return editItem;
    }

    public void delete(JsonObject selectedItem) {
        requestData(
                repository.deleteItem(selectedItem),

                response -> {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            boolean isSuccess = response.body().equals("1");
                            if (isSuccess)
                                deleteResponse.postValue(
                                        new DataState.Success(context.getResources().getString(R.string.address_delete_success))
                                );
                            else deleteResponse.postValue(
                                    new DataState.Error(context.getResources().getString(R.string.address_delete_fail))
                            );
                        }
                    }
                },

                throwable -> {
                    Log.e("deleteAddress", throwable.getMessage());
                    deleteResponse.postValue(
                            new DataState.Error(context.getResources().getString(R.string.error_connection))
                    );
                }
        );
    }

    public MutableLiveData<DataState> getOnDeleteAddress() {
        return deleteResponse;
    }

    public void update(JsonObject item, String key) {
        boolean hasFieldOnChange = JsonHelper.has(item, "field_onchange");

        if (hasFieldOnChange) {

            JsonElement fieldOnchange1 = item.get("field_onchange");

            if (fieldOnchange1.isJsonObject()) {
                JsonObject fieldOnchange = fieldOnchange1.getAsJsonObject();

                String link = fieldOnchange.get("link").getAsString();
                JsonObject params = fieldOnchange.get("params").getAsJsonObject();
                boolean hasMethod = JsonHelper.has(fieldOnchange, "method");

                String method = "get";
                if (hasMethod)
                    method = "post";

                params.remove("namekey");
                params.addProperty("namekey", key);

                String tag = params.get("field_namekey").getAsString();
                requestData(
                        repository.save(tag, link, params, method),

                        response -> {
                            if (response.isSuccessful()) {
                                handleUpdateSpinnerResponse(response);
                            }
                        },
                        throwable -> {
                            Log.e("saveAddress", throwable.getMessage());
                        }
                );
            }
        }
    }

    private void handleUpdateSpinnerResponse(Response<JsonElement> jsonElementResponse) {
        String tag = jsonElementResponse.raw().request().tag(String.class);
        if (tag != null) {
            JsonObject result = jsonElementResponse.body().getAsJsonObject();
            result.addProperty("tag", tag);
            spinnerUpdate.postValue(result);
        }
    }

    public LiveData<JsonObject> getSpinnerUpdate() {
        return spinnerUpdate;
    }

    public void saveNewAddress(Map<Integer, FormData> values) {
        if (addressActions == null) return;


        String path = addressActions.get("link").getAsString();

        String type = "get";

        if (JsonHelper.has(addressActions, "type")) {
            type = addressActions.get("type").getAsString();
        }
        JsonObject defaultParams = addressActions.get("params").getAsJsonObject();

        for (Integer i : values.keySet()) {

            String fieldPostKey = values.get(i).getFieldNameKey();
            String key = values.get(i).getKey();
            String value = values.get(i).getValue();
            JsonObject data = defaultParams.get("data").getAsJsonObject().get("address").getAsJsonObject();

            if (!key.equals(fieldPostKey))
                data.addProperty(fieldPostKey, key);
            else data.addProperty(fieldPostKey, value);
        }
        requestData(
                repository.save(null, path, defaultParams, type),

                response -> {
                    if (response.isSuccessful()) {
                        handleSaveResponse(response);
                    }
                },
                throwable -> {
                    Log.e("saveAddress", throwable.getMessage());
                }
        );
    }

    private void handleSaveResponse(Response<JsonElement> jsonElementResponse) {
        JsonArray elements = dataController.extractDataItemsAsJsonArray(jsonElementResponse);
        if (elements.size() > 0)
            saveResponse.postValue(new DataState.Success(context.getResources().getString(R.string.save_success)));
        else
            saveResponse.postValue(new DataState.Error(context.getResources().getString(R.string.save_failure)));
    }

    public LiveData<DataState> getSaveResponse() {

        return saveResponse;
    }

}
