package com.poonehmedia.app.ui.items;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.MyDiffUtil;
import com.poonehmedia.app.databinding.ListItemItemsBinding;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;

import javax.inject.Inject;

public class ItemsPagedListAdapter extends PagingDataAdapter<JsonObject, ItemsPagedListAdapter.ViewHolder> {

    private OnParamValueChanged onClickListener;

    @Inject
    public ItemsPagedListAdapter() {
        super(new MyDiffUtil());
    }

    public void subscribeCallbacks(OnParamValueChanged onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemItemsBinding binding = ListItemItemsBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = getItem(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> onClickListener.onChanged(getItem(position), position));
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final ListItemItemsBinding binding;

        public ViewHolder(@NonNull ListItemItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);

            binding.executePendingBindings();
        }
    }

}
