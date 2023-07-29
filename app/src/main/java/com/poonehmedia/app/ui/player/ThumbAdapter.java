package com.poonehmedia.app.ui.player;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poonehmedia.app.data.model.ThumbItem;
import com.poonehmedia.app.databinding.ListItemThumbBinding;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;

import java.util.List;

import javax.inject.Inject;

public class ThumbAdapter extends RecyclerView.Adapter<ThumbAdapter.ViewHolder> {
    private OnParamValueChanged onClick;
    private List<ThumbItem> items;

    @Inject
    public ThumbAdapter() {

    }

    public void subscribeCallbacks(OnParamValueChanged onClick) {
        this.onClick = onClick;
    }

    public void submitList(List<ThumbItem> list) {
        this.items = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemThumbBinding binding = ListItemThumbBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThumbItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void notifySelection(int position) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSelected(i == position);
        }
        notifyDataSetChanged();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final com.poonehmedia.app.databinding.ListItemThumbBinding binding;

        public ViewHolder(@NonNull ListItemThumbBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ThumbItem item) {
            binding.setUrl(item.getThumbUrl());
            binding.selected.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);

            // callback to update image in outer pager
            itemView.setOnClickListener(v -> onClick.onChanged(item, getAbsoluteAdapterPosition()));
            // binding.executePendingBindings();
        }
    }
}
