package com.poonehmedia.app.ui.categories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.MyDiffUtil;
import com.poonehmedia.app.databinding.ListItemShopCategoriesBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.ItemSpaceDecoration;

import org.jetbrains.annotations.NotNull;

public class ShopCategoriesListAdapter extends PagingDataAdapter<JsonObject, ShopCategoriesListAdapter.ViewHolder> {

    private OnParamValueChanged clickCallback;

    public ShopCategoriesListAdapter() {
        super(new MyDiffUtil());
    }

    public void subscribeCallbacks(OnParamValueChanged clickCallback) {
        this.clickCallback = clickCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemShopCategoriesBinding binding = ListItemShopCategoriesBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        initHorizontalRecycler(binding, parent.getContext());

        return new ViewHolder(binding);
    }

    private void initHorizontalRecycler(ListItemShopCategoriesBinding binding, Context context) {

        GenericListAdapterImp<JsonElement> adapter = new GenericListAdapterImp<>();

        adapter.setLayoutRes(R.layout.list_item_shop_categories_horizontal);
        adapter.subscribeCallbacks((item, position) -> clickCallback.onChanged(item, position));

        ItemSpaceDecoration itemSpaceDecoration = new ItemSpaceDecoration(
                (int) AndroidUtils.getPixels(8, context)
        );
        binding.recycler.addItemDecoration(itemSpaceDecoration);
        binding.recycler.setAdapter(adapter);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = getItem(position);
        holder.bind(item);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {


        private final ListItemShopCategoriesBinding binding;

        public ViewHolder(@NonNull @NotNull ListItemShopCategoriesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {

            if (JsonHelper.has(item, "childs"))
                ((GenericListAdapterImp) binding.recycler.getAdapter()).submitList(item.get("childs").getAsJsonArray());

            binding.title.setText(item.get("title").getAsString());

            binding.showAll.setOnClickListener(v -> {
                clickCallback.onChanged(item, getAbsoluteAdapterPosition());
            });
        }
    }
}
