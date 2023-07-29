package com.poonehmedia.app.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.poonehmedia.app.data.model.GalleryItem;
import com.poonehmedia.app.databinding.ListItemProductGalleryBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

public class GallerySliderAdapter extends SliderViewAdapter<GallerySliderAdapter.SliderAdapterVH> {

    private final ClickProvider callback;
    private List<GalleryItem> mSliderItems;

    public GallerySliderAdapter(ClickProvider callback) {
        this.callback = callback;
    }

    public void submitList(List<GalleryItem> sliderItems) {
        this.mSliderItems = sliderItems;
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemProductGalleryBinding inflate = ListItemProductGalleryBinding.inflate(layoutInflater, parent, false);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {

        String image = mSliderItems.get(position).getImageUrlOrVideoThumb();
        viewHolder.bind(image, position);

    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    protected class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        private final ListItemProductGalleryBinding binding;

        public SliderAdapterVH(ListItemProductGalleryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String image, int position) {
            binding.setUrl(image);
            itemView.setOnClickListener(v -> callback.onClick(mSliderItems, position));
            // binding.executePendingBindings();
        }

    }


}
