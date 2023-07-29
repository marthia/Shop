package com.poonehmedia.app.ui.interfaces;

import com.google.gson.JsonObject;

public interface OnFilterChangedListener {
    void handle(String type, boolean isChecked, String value, String key, String title, JsonObject item, int position);
}
