package com.poonehmedia.app.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.databinding.ListItemPriceBinding;

import java.util.List;

import javax.inject.Inject;

public class PriceAdapter extends RecyclerView.Adapter<PriceAdapter.ViewHolder> {

    List<Price> items;

    @Inject
    public PriceAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListItemPriceBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Price item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void submitList(List<Price> s) {
        items = s;
        notifyDataSetChanged();
    }

    public List<Price> getCurrentList() {
        return items;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemPriceBinding binding;

        public ViewHolder(@NonNull ListItemPriceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Price item) {
            binding.setItem(item);

            // binding.executePendingBindings();
        }
    }
}
