package com.poonehmedia.app.data.model;

import com.google.gson.JsonArray;

public class ProductsPagingMetaData {
    private int totalCount;
    private JsonArray filters = new JsonArray();
    private JsonArray sorts = new JsonArray();
    private boolean shouldBindFilters = true;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public JsonArray getFilters() {
        return filters;
    }

    public void setFilters(JsonArray filters) {
        this.filters = filters;
    }

    public JsonArray getSorts() {
        return sorts;
    }

    public void setSorts(JsonArray sorts) {
        this.sorts = sorts;
    }

    public boolean isShouldBindFilters() {
        return shouldBindFilters;
    }

    public void setShouldBindFilters(boolean shouldBindFilters) {
        this.shouldBindFilters = shouldBindFilters;
    }
}
