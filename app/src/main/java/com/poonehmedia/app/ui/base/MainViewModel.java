package com.poonehmedia.app.ui.base;

import android.content.Context;
import android.util.Log;
import android.util.MalformedJsonException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.AppParams;
import com.poonehmedia.app.data.model.RequestException;
import com.poonehmedia.app.data.repository.MainRepository;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.ui.interfaces.OnResponseCallback;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.DeviceInfoManager;

import java.net.UnknownHostException;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Response;

@HiltViewModel
public class MainViewModel extends BaseViewModel {
    private final MutableLiveData<List<String>> searchSuggestions = new MutableLiveData<>();

    private final Context context;
    private final MainRepository repository;
    private final DataController dataController;
    private final DeviceInfoManager deviceInfoManager;

    private final MutableLiveData<String> failureResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> invalidateSession = new MutableLiveData<>();
    private final MutableLiveData<RequestException> exception = new MutableLiveData<>();
    private final MutableLiveData<AppParams> forceUpdate = new MutableLiveData<>();
    private final MutableLiveData<Integer> cartBadgeCount = new MutableLiveData<>(0);
    private final MutableLiveData<JsonObject> cartInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> customizedServerNotices = new MutableLiveData<>();

    @Inject
    public MainViewModel(@ApplicationContext Context context,
                         MainRepository repository,
                         DataController dataController,
                         DeviceInfoManager deviceInfoManager,
                         SavedStateHandle savedStateHandle,
                         RoutePersistence routePersistence) {

        super(routePersistence, savedStateHandle);
        this.context = context;
        this.repository = repository;
        this.dataController = dataController;
        this.deviceInfoManager = deviceInfoManager;
    }

    public JsonArray getAllLanguages() {
        return repository.getAllLanguages();
    }

    public JsonArray getDrawerMenus() {
        return repository.getDrawerMenus();
    }

    public JsonArray getAllBottomNavMenus() {
        return repository.getAllBottomNavMenus();
    }

    public String getLanguage() {
        return repository.getSelectedLanguage();
    }

    public void setLanguage(String language) {
        repository.saveSelectedLanguage(language);
    }

    public JsonObject findDefaultFragmentFromMenuList(JsonArray items) {
        for (int i = 0; i < items.size(); i++) {
            JsonObject obj = items.get(i).getAsJsonObject();

            if (obj.get("isdefault").getAsString().equals("1")) {
                return obj;
            }
        }
        return null;
    }

    public LiveData<Integer> getCartBadgeCount() {
        return cartBadgeCount;
    }

    public LiveData<AppParams> getForceUpdate() {
        return forceUpdate;
    }


    public LiveData<List<String>> getSearchSuggestions(String path) {

        requestData(
                repository.getSearchSuggestions(path),

                response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        List<String> suggestions = dataController.convertJsonArrayToList(
                                response.body().getAsJsonObject().get("searchData").getAsJsonArray(),
                                String.class
                        );

                        searchSuggestions.postValue(suggestions);
                    }
                },
                throwable -> {
                    dataController.onFailure(throwable);
                    Log.e("searchSuggestions", throwable.getMessage());
                }
        );


        return searchSuggestions;

    }

    public void subscribeDefaultResponses() {

        repository.subscribeBaseCallback(new OnResponseCallback() {
            @Override
            public void onSuccess(Object data) {
                // handle cart info
                handleCartInfoResponse(data);

                // handle force update
                if (!repository.isForceUpdateHandled())
                    handleForceUpdate(data);
            }

            @Override
            public void onFailure(String message, Throwable t) {
                if (t instanceof MalformedJsonException || t instanceof NullPointerException || t instanceof UnknownHostException) {
                    RequestException a = new RequestException(
                            context.getString(R.string.error_connecting_to_server),
                            context.getString(R.string.retry)
                    );
                    exception.postValue(a);
                } else
                    failureResponse.postValue(message);
            }
        });

        repository.subscribeInvalidateSession(invalidateSession::postValue);
    }

    public LiveData<Boolean> getInvalidateSession() {
        return invalidateSession;
    }

    private void handleCartInfoResponse(Object data) {
        try {
            JsonObject cartInfo = dataController.extractModule(((Response<JsonElement>) data).body(), "shopMiniCart");
            if (cartInfo != null) {

                cartBadgeCount.postValue(dataController.getCartTotalCount(cartInfo));

                cartInfoLiveData.postValue(cartInfo);
            }

        } catch (Exception e) {
            Log.e("cartCount", e.getMessage());
        }
    }

    private void handleForceUpdate(Object data) {
        try {
            AppParams forceUpdate = dataController.extractForceUpdate(((Response<JsonElement>) data));
            if (forceUpdate != null) {
                if (forceUpdate.getAppVersion() > Float.parseFloat(
                        deviceInfoManager.getAppVersion()
                )) {
                    this.forceUpdate.postValue(forceUpdate);
                }
            }
        } catch (Exception e) {
            Log.e("forceUpdate", e.getMessage());
        }
    }


    public LiveData<String> getFailureResponse() {
        return failureResponse;
    }

    public JsonObject getReturn() {
        return repository.getReturn();
    }

    public void clearReturn() {
        repository.clearReturn();
    }

    public LiveData<RequestException> getException() {
        return exception;
    }

    public void setHasForceUpdateHandled(boolean b) {
        repository.setHasForceUpdateHandled(b);
    }

    public MutableLiveData<JsonObject> getCustomizedServerNotices() {
        return customizedServerNotices;
    }

    public void fetchServerNoticesData() {
        requestData(
                repository.getServerNotice("android-payments-form"),
                response -> {
                    JsonArray item = dataController.extractDataItemsAsJsonArray(response);
                    customizedServerNotices.postValue(item.get(0).getAsJsonObject());
                },
                throwable -> {
                    Log.e("error", throwable.getMessage());
                });
    }

    public String replaceIfFullPath(String url, String baseUrl) {
        return dataController.replaceFullPathLinksWithPath(url, baseUrl);
    }

    public DataController getDataController() {
        return dataController;
    }

    public void logout() {
        dataController.clearCookies();
    }
}
