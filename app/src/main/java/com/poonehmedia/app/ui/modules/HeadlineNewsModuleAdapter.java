package com.poonehmedia.app.ui.modules;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.databinding.ListItemHeadlineNewsBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.smarteist.autoimageslider.SliderViewAdapter;

public class HeadlineNewsModuleAdapter extends SliderViewAdapter<HeadlineNewsModuleAdapter.SliderAdapterVH> {

    private ClickProvider callback;
    private JsonArray items;

    public void subscribeCallbacks(ClickProvider callback) {
        this.callback = callback;
    }

    public void submitList(JsonArray sliderItems) {
        this.items = sliderItems;
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemHeadlineNewsBinding binding = ListItemHeadlineNewsBinding.inflate(layoutInflater, parent, false);
        return new SliderAdapterVH(binding);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {

        JsonObject item = items.get(position).getAsJsonObject();
        viewHolder.bind(item);

        viewHolder.itemView.setOnClickListener(v -> callback.onClick(item, position));
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        private final ListItemHeadlineNewsBinding binding;

        public SliderAdapterVH(ListItemHeadlineNewsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);
        }

    }


}