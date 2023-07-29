package com.poonehmedia.app.ui.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.MyDiffUtil;
import com.poonehmedia.app.databinding.ListItemSearchResultBinding;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;

public class SearchPagedListAdapter extends PagingDataAdapter<JsonObject, SearchPagedListAdapter.ViewHolder> {

    private OnParamValueChanged onClickListener;

    public SearchPagedListAdapter() {
        super(new MyDiffUtil());
    }

    public void subscribeCallbacks(OnParamValueChanged onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemSearchResultBinding binding = ListItemSearchResultBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = getItem(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> onClickListener.onChanged(getItem(position), position));
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final ListItemSearchResultBinding binding;

        public ViewHolder(@NonNull ListItemSearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);

            // binding.executePendingBindings();
        }
    }

}
