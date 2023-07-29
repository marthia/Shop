package com.poonehmedia.app.ui.affiliate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.databinding.ListItemTabAffiliateStatsBinding;
import com.poonehmedia.app.ui.interfaces.OnCustomClick;

public class AffiliateStatsAdapter extends RecyclerView.Adapter<AffiliateStatsAdapter.ViewHolder> {

    private final OnCustomClick onCustomClick;
    JsonArray items;

    public AffiliateStatsAdapter(OnCustomClick onCustomClick) {
        this.onCustomClick = onCustomClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListItemTabAffiliateStatsBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
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

    public void submitList(JsonArray stats) {
        items = stats;
        notifyDataSetChanged();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final com.poonehmedia.app.databinding.ListItemTabAffiliateStatsBinding binding;

        public ViewHolder(@NonNull ListItemTabAffiliateStatsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);
            binding.setOnDetailsClick(onCustomClick);
        }
    }
}
