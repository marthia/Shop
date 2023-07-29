package com.poonehmedia.app.ui.modules;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.databinding.ListItemModuleCardProductsContentBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.poonehmedia.app.util.base.DataController;

import java.util.List;

public class ProductsCardModuleAdapter extends RecyclerView.Adapter<ProductsCardModuleAdapter.ViewHolder> {

    private final DataController dataController;
    JsonArray items;
    private ClickProvider callback;

    public ProductsCardModuleAdapter(DataController dataController) {
        this.dataController = dataController;
    }

    public void subscribeCallbacks(ClickProvider callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemModuleCardProductsContentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = items.get(position).getAsJsonObject();
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void submitList(JsonArray content) {
        items = content;
        notifyDataSetChanged();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemModuleCardProductsContentBinding binding;

        public ViewHolder(ListItemModuleCardProductsContentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);
            List<Price> prices = dataController.extractPrice(item, null);
            if (prices.size() > 0) binding.setPriceItem(prices.get(0));
            itemView.setOnClickListener(v -> callback.onClick(
                    items.get(getAbsoluteAdapterPosition()).getAsJsonObject(),
                    getAbsoluteAdapterPosition()
            ));
        }
    }
}
