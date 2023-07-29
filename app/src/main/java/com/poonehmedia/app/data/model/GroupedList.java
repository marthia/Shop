package com.poonehmedia.app.data.model;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.google.gson.JsonObject;

import java.util.Objects;

public class GroupedList {
    private String id;
    private String headerTitle;
    private JsonObject data;
    private JsonObject category;
    private ViewType viewType;


    public ViewType getViewType() {
        return viewType;
    }

    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupedList that = (GroupedList) o;
        return getId().equals(that.getId()) &&
                Objects.equals(getHeaderTitle(), that.getHeaderTitle()) &&
                Objects.equals(getData(), that.getData()) &&
                getViewType() == that.getViewType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getHeaderTitle(), getData(), getViewType());
    }

    public JsonObject getCategory() {
        return category;
    }

    public void setCategory(JsonObject category) {
        this.category = category;
    }

    public enum ViewType {HEADER, CONTENT, CATEGORY}

    public static class MyDiffUtil extends DiffUtil.ItemCallback<GroupedList> {

        @Override
        public boolean areItemsTheSame(@NonNull GroupedList oldItem, @NonNull GroupedList newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull GroupedList oldItem, @NonNull GroupedList newItem) {
            return oldItem.equals(newItem);
        }
    }
}
