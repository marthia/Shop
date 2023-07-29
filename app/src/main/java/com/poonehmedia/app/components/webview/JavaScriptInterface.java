package com.poonehmedia.app.components.webview;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JavaScriptInterface {
    private NavigationInterface navigationInterface;
    private ShowFormInterface showFormInterface;
    private FileChooserCommunication fileChooserCommunication;
    private ProcessFormData processFormData;

    @JavascriptInterface
    public void showForm() {
        showFormInterface.reload();
    }

    @JavascriptInterface
    public void onBeforeOpenFileChooser(String key, String extensions, String size, int multiple) {
        fileChooserCommunication.handle(multiple == 1, key);
    }

    @JavascriptInterface
    public void handleClickOnUrl(String link) {
        navigationInterface.navigate(link);
    }

    @JavascriptInterface
    public void processFormData(String formData, int isWithFile) {
        Log.i("JavaScriptInterface", "processFormData: " + formData);
        processFormData.process(JsonParser.parseString(formData).getAsJsonObject(), isWithFile == 1);
    }

    public void subscribeNavigation(NavigationInterface navigationInterface) {

        this.navigationInterface = navigationInterface;
    }

    public void subscribeShowForm(ShowFormInterface showFormInterface) {

        this.showFormInterface = showFormInterface;
    }

    public void subscribeFileChooserCommunication(FileChooserCommunication fileChooserCommunication) {
        this.fileChooserCommunication = fileChooserCommunication;
    }

    public void subscribeProcessData(ProcessFormData processFormData) {
        this.processFormData = processFormData;
    }

    public interface NavigationInterface {
        void navigate(String link);
    }

    public interface ShowFormInterface {
        void reload();
    }

    public interface FileChooserCommunication {
        void handle(boolean isMultiple, String currentKey);
    }

    public interface ProcessFormData {
        void process(JsonObject data, boolean hasFile);
    }
}
