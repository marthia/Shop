package com.poonehmedia.app.data.model;

import com.google.gson.JsonObject;

public class Response {
    private final boolean isError;
    private Object result;
    private String mgs;

    public Response(JsonObject result) {
        isError = false;
        this.result = result;
    }

    public Response(String mgs) {
        isError = true;
        this.mgs = mgs;
    }

    public boolean isError() {
        return isError;
    }

    public Object getResult() {
        return result;
    }

    public String getMgs() {
        return mgs;
    }
}