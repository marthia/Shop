package com.poonehmedia.app.ui.orders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.MyDiffUtil;
import com.poonehmedia.app.databinding.ListItemOrderListBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;

import javax.inject.Inject;

public class OrdersPagedListAdapter extends PagingDataAdapter<JsonObject, OrdersPagedListAdapter.ViewHolder> {

    private ClickProvider clickProvider;

    @Inject
    public OrdersPagedListAdapter() {
        super(new MyDiffUtil());
    }

    public void subscribeCallbacks(ClickProvider onCategoryClick) {
        this.clickProvider = onCategoryClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        return new ViewHolder(ListItemOrderListBinding.inflate(layoutInflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemOrderListBinding binding;

        public ViewHolder(@NonNull ListItemOrderListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);

            itemView.setOnClickListener(v ->
                    clickProvider.onClick(
                            getItem(getAbsoluteAdapterPosition()),
                            getAbsoluteAdapterPosition())
            );

            // binding.executePendingBindings();
        }
    }

}
