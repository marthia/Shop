package com.poonehmedia.app.modules.navigation;

import com.google.gson.JsonObject;

public class NavigationArgs {
    private String title;
    private String link;
    private JsonObject action;
    private int pageSize;
    private String pagingStartKey;
    private Object data;

    public NavigationArgs(String title, String link, JsonObject action, int pageSize, String pagingStartKey, Object data) {
        this.title = title;
        this.link = link;
        this.action = action;
        this.pageSize = pageSize;
        this.pagingStartKey = pagingStartKey;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public JsonObject getAction() {
        return action;
    }

    public void setAction(JsonObject action) {
        this.action = action;
    }

    public Object getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }


    @Override
    public String toString() {
        return "NavigationArgs{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", action=" + action +
                ", pageSize=" + pageSize +
                ", pagingStartKey='" + pagingStartKey + '\'' +
                ", data=" + data +
                '}';
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getPagingStartKey() {
        return pagingStartKey;
    }

    public void setPagingStartKey(String pagingStartKey) {
        this.pagingStartKey = pagingStartKey;
    }
}
