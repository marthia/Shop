package com.poonehmedia.app.ui.interfaces;

public interface OnResponseCallback {
    void onSuccess(Object data);

    void onFailure(String message, Throwable t);
}
