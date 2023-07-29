package com.poonehmedia.app.ui.manufacturer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.MyDiffUtil;
import com.poonehmedia.app.databinding.ListItemManufacturerBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;

public class ManufacturerPagingAdapter extends PagingDataAdapter<JsonObject, ManufacturerPagingAdapter.ViewHolder> {

    private ClickProvider callback;

    protected ManufacturerPagingAdapter() {
        super(new MyDiffUtil());
    }

    public void subscribeCallbacks(ClickProvider clickProvider) {

        callback = clickProvider;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemManufacturerBinding binding = ListItemManufacturerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final ListItemManufacturerBinding binding;

        public ViewHolder(@NonNull ListItemManufacturerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);
            itemView.setOnClickListener(v -> callback.onClick(item, getAbsoluteAdapterPosition()));
            // binding.executePendingBindings();
        }
    }
}
