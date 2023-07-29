package com.poonehmedia.app.ui.checkout.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.poonehmedia.app.databinding.CartStepsListItemStatusBinding;

public class CartStatusAdapter extends RecyclerView.Adapter<CartStatusAdapter.ViewHolder> {


    private JsonArray items;

    public void submitList(JsonArray array) {

        items = array;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CartStepsListItemStatusBinding binding = CartStepsListItemStatusBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = items.get(position).getAsString();
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final com.poonehmedia.app.databinding.CartStepsListItemStatusBinding binding;

        public ViewHolder(@NonNull CartStepsListItemStatusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String item) {
            binding.title.setText(item);
        }
    }
}
