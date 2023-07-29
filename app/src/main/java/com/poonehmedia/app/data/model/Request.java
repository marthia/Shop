package com.poonehmedia.app.data.model;

import com.google.gson.JsonObject;

public class Request {
    private String link;
    private boolean isPost = false;
    private JsonObject params;
    private boolean isPaged = false;
    private String pageStart;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        isPost = post;
    }

    public JsonObject getParams() {
        return params;
    }

    public void setParams(JsonObject params) {
        this.params = params;
    }

    public boolean isPaged() {
        return isPaged;
    }

    public void setPaged(boolean paged) {
        isPaged = paged;
    }

    public String getPageStart() {
        return pageStart;
    }

    public void setPageStart(String start) {
        this.pageStart = start;
    }
}
