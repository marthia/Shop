package com.poonehmedia.app.ui.interfaces;

import android.net.Uri;
import android.webkit.ValueCallback;

public interface WebViewFileChooser {

    void open(ValueCallback<Uri[]> callback);
}
