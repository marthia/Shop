package com.poonehmedia.app.data.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class CompareItem {
    private int id;
    private String title;
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompareItem that = (CompareItem) o;
        return getId() == that.getId() &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getValue());
    }

    public static class DiffUtil extends androidx.recyclerview.widget.DiffUtil.ItemCallback<CompareItem> {

        @Override
        public boolean areItemsTheSame(@NonNull CompareItem oldItem, @NonNull CompareItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CompareItem oldItem, @NonNull CompareItem newItem) {
            return oldItem.equals(newItem);
        }
    }
}
