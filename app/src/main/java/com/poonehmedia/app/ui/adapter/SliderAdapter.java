package com.poonehmedia.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.ModuleMetadata;
import com.poonehmedia.app.databinding.ListItemSlideShowBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.smarteist.autoimageslider.SliderViewAdapter;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private ClickProvider callback;
    private JsonArray mSliderItems = new JsonArray();
    private ModuleMetadata metadata;

    public void subscribeCallbacks(ClickProvider callback) {
        this.callback = callback;
    }

    public void submitList(JsonArray sliderItems) {
        this.mSliderItems = sliderItems;
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemSlideShowBinding inflate = ListItemSlideShowBinding.inflate(layoutInflater, parent, false);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {

        JsonObject sliderItem = mSliderItems.get(position).getAsJsonObject();
        viewHolder.bind(sliderItem.get("image").getAsString());

        viewHolder.itemView.setOnClickListener(v ->
                callback.onClick(mSliderItems.get(position).getAsJsonObject(), position)
        );
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    public void setMetadata(ModuleMetadata metadata) {

        this.metadata = metadata;
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        private final ListItemSlideShowBinding binding;

        public SliderAdapterVH(ListItemSlideShowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String image) {
            if (metadata != null)
                binding.setParams(metadata);
            binding.setImageUrl(image);
        }

    }


}