package com.poonehmedia.app.data.model;

import com.google.gson.JsonArray;

public class TripleSnapPage {
    private final JsonArray data;

    public TripleSnapPage(JsonArray data) {
        this.data = data;
    }

    public JsonArray getData() {
        return data;
    }
}
