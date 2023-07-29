package com.poonehmedia.app.data.model;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.google.gson.JsonObject;

public class MyDiffUtil extends DiffUtil.ItemCallback<JsonObject> {

    @Override
    public boolean areItemsTheSame(@NonNull JsonObject oldItem, @NonNull JsonObject newItem) {
        return false;
    }

    @Override
    public boolean areContentsTheSame(@NonNull JsonObject oldItem, @NonNull JsonObject newItem) {
        return false;
    }
}
