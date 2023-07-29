package com.poonehmedia.app.ui.checkout.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.databinding.ListItemCheckoutMessagesBinding;

public class CheckoutMessagesAdapter extends RecyclerView.Adapter<CheckoutMessagesAdapter.ViewHolder> {

    JsonArray items;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListItemCheckoutMessagesBinding.inflate(LayoutInflater.from(parent.getContext()),
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

    public void submitList(JsonArray messages) {
        items = messages;
        notifyDataSetChanged();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final com.poonehmedia.app.databinding.ListItemCheckoutMessagesBinding binding;

        public ViewHolder(@NonNull ListItemCheckoutMessagesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.message.setText(item.get("msg").getAsString());
            binding.setType(item.get("type").getAsString());
        }
    }
}
