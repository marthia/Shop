package com.poonehmedia.app.ui.modules;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.databinding.ListItemTwoPaneProductsBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;

import java.util.List;

public class TwoPaneProductsAdapter extends RecyclerView.Adapter<TwoPaneProductsAdapter.ViewHolder> {

    private final DataController dataController;
    private JsonArray items;
    private ClickProvider callback;

    public TwoPaneProductsAdapter(DataController dataController) {
        this.dataController = dataController;
    }

    public void subscribeCallbacks(ClickProvider callback) {
        this.callback = callback;
    }

    public void submitList(JsonArray items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ListItemTwoPaneProductsBinding bindingMain = ListItemTwoPaneProductsBinding.inflate(layoutInflater, parent, false);

        init(bindingMain);
        return new ViewHolder(bindingMain);
    }

    private void init(ListItemTwoPaneProductsBinding binding) {
        GenericListAdapterImp badgesAdapter = new GenericListAdapterImp();

        badgesAdapter.setLayoutRes(R.layout.list_item_badge);

        binding.badges.setNestedScrollingEnabled(false);
        binding.badges.setAdapter(badgesAdapter);
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

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final ListItemTwoPaneProductsBinding binding;

        public ViewHolder(@NonNull ListItemTwoPaneProductsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);

            List<Price> prices = dataController.extractPrice(item, null);
            if (prices.size() > 0) binding.setPriceItem(prices.get(0));

            if (JsonHelper.has(item, "badges") && JsonHelper.isNotEmptyNorNull(item.get("badges"))) {
                binding.badges.setVisibility(View.VISIBLE);
                ((GenericListAdapterImp) binding.badges.getAdapter())
                        .submitList(item.get("badges").getAsJsonArray());
            } else binding.badges.setVisibility(View.GONE);


            itemView.setOnClickListener(v ->
                    callback.onClick(
                            items.get(getAbsoluteAdapterPosition()).getAsJsonObject(),
                            getAbsoluteAdapterPosition()
                    )
            );
            // binding.executePendingBindings();
        }
    }

}
