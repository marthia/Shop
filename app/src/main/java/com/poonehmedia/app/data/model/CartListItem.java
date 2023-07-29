package com.poonehmedia.app.data.model;

import com.google.gson.JsonObject;

public class CartListItem {
    private boolean isUpdating;
    private JsonObject data;

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setUpdating(boolean updating) {
        isUpdating = updating;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
