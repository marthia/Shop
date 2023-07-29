package com.poonehmedia.app.data.repository;

import androidx.core.util.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.framework.service.SearchApi;
import com.poonehmedia.app.ui.interfaces.OnResponseCallback;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

public class MainRepository {
    private final SettingsRepository settingsRepository;
    private final SearchApi searchApi;
    private final DataController dataController;
    private final PreferenceManager preferenceManager;

    @Inject
    public MainRepository(SettingsRepository settingsRepository,
                          DataController dataController,
                          PreferenceManager preferenceManager,
                          SearchApi searchApi
    ) {
        this.settingsRepository = settingsRepository;
        this.dataController = dataController;
        this.preferenceManager = preferenceManager;
        this.searchApi = searchApi;
    }

    public JsonArray getAllLanguages() {
        return settingsRepository.getLanguageItems();
    }

    public JsonArray getDrawerMenus() {
        JsonArray menuItems = settingsRepository.getMenuItems();
        return extractMenus(menuItems, MenuType.SIDE);
    }

    public JsonArray getAllBottomNavMenus() {
        JsonArray menuItems = settingsRepository.getMenuItems();
        return extractMenus(menuItems, MenuType.BOTTOM);
    }

    private JsonArray extractMenus(JsonArray menuItems, MenuType type) {
        String typeQuery = "bottom";

        if (type == MenuType.SIDE) typeQuery = "default";

        JsonArray result = new JsonArray();

        for (int i = 0; i < menuItems.size(); i++) {
            JsonObject item = menuItems.get(i).getAsJsonObject();
            if (item.get("location").getAsString().equals(typeQuery))
                result.add(item);
        }

        return result;
    }

    public void saveSelectedLanguage(String tag) {
        preferenceManager.saveLanguage(tag);
    }

    public String getSelectedLanguage() {
        return preferenceManager.getLanguage();
    }

    public Single<Response<JsonElement>> getSearchSuggestions(String path) {
        return searchApi.getSuggestions(path);
    }

    public void subscribeBaseCallback(OnResponseCallback onResponseCallback) {
        dataController.setUiMessagesResponseCallback(onResponseCallback);
    }

    public JsonObject getReturn() {
        return preferenceManager.getReturn();
    }

    public void clearReturn() {
        preferenceManager.clearReturn();
    }

    public void onSuccess(Response<JsonElement> response) {
        dataController.onSuccess(response);
    }

    public void onFailure(Throwable throwable) {
        dataController.onFailure(throwable);
    }

    public void setHasForceUpdateHandled(boolean b) {
        settingsRepository.setHasForceUpdateHandled(b);
    }

    public boolean isForceUpdateHandled() {
        return settingsRepository.isForceUpdateHandled();
    }

    public Single<Response<JsonElement>> getServerNotice(String link) {
        return searchApi.getSuggestions(link);
    }

    public void subscribeInvalidateSession(Consumer<Boolean> action) {
        dataController.subscribeInvalidateSession(action);
    }

    private enum MenuType {
        SIDE, BOTTOM
    }

}
