package com.poonehmedia.app.components.webview;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poonehmedia.app.ui.interfaces.WebViewFileChooser;
import com.poonehmedia.app.ui.interfaces.WebViewInternalNavigation;

public class FeaturefulWebView extends WebView {

    private WebViewFileChooser fileChooser;
    private WebViewInternalNavigation internalNavigation;
    private JavaScriptInterface jsBridge;

    public FeaturefulWebView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FeaturefulWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FeaturefulWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FeaturefulWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        // disable text selection
        setOnLongClickListener(v -> true);

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        setWebChromeClient(new MyWebChromeClient());
        setWebViewClient(new MyWebClient());
        WebSettings settings = getSettings();
        // override settings
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(false);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        jsBridge = new JavaScriptInterface();
        addJavascriptInterface(jsBridge, "Mediator");
    }

    /**
     * interface to control the file chooser requests
     */
    public void subscribeOpenFileChooser(WebViewFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    /**
     * interface to handle all url clicks in any html dom.
     */
    public void subscribeInternalNavigation(WebViewInternalNavigation internalNavigation) {
        this.internalNavigation = internalNavigation;
    }

    public void subscribeNavigation(JavaScriptInterface.NavigationInterface navigationInterface) {
        jsBridge.subscribeNavigation(navigationInterface);
    }

    public void subscribeShowForm(JavaScriptInterface.ShowFormInterface showFormInterface) {

        jsBridge.subscribeShowForm(showFormInterface);
    }

    public void subscribeFileChooserCommunication(JavaScriptInterface.FileChooserCommunication fileChooserCommunication) {

        jsBridge.subscribeFileChooserCommunication(fileChooserCommunication);
    }

    public void subscribeProcessData(JavaScriptInterface.ProcessFormData processFormData) {

        jsBridge.subscribeProcessData(processFormData);
    }

    class MyWebChromeClient extends WebChromeClient {

        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            Log.d("JavaScript", String.format("%s @ %d: %s",
                    cm.message(), cm.lineNumber(), cm.sourceId()));
            return true;
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (fileChooser != null)
                fileChooser.open(filePathCallback);

            return true;
        }
    }

    class MyWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (internalNavigation != null) {
                internalNavigation.navigate(request.getUrl() == null ? "" : request.getUrl().toString());
                return true;
            }
            return false;
        }
    }
}
