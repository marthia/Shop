package com.poonehmedia.app.ui.interfaces;

import com.google.gson.JsonObject;

public interface UpdateCartCallback {
    void onAction(int position, int quantity, String updateLink, JsonObject params);
}