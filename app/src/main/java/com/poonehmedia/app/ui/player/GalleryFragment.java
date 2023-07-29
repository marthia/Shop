package com.poonehmedia.app.ui.player;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.poonehmedia.app.data.model.GalleryItem;
import com.poonehmedia.app.data.model.ThumbItem;
import com.poonehmedia.app.databinding.FragmentGalleryBinding;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class GalleryFragment extends BaseFragment {

    @Inject
    public ThumbAdapter thumbsAdapter;
    @Inject
    public GalleryAdapter galleryPagerAdapter;
    @Inject
    public RoutePersistence routePersistence;
    private FragmentGalleryBinding binding;
    private List<GalleryItem> images;
    private int selectedPosition;
    private String dataKey = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);

        dataKey = GalleryFragmentArgs.fromBundle(getArguments()).getList();
        images = (List<GalleryItem>) routePersistence.getRoute(dataKey).getData();
        selectedPosition = GalleryFragmentArgs.fromBundle(getArguments()).getSelectedPosition();
        init();
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        // thumbs
        thumbsAdapter.subscribeCallbacks((item, position) -> {// update the pager
            binding.images.setCurrentItem(position, true);
        });
        binding.thumbs.setAdapter(thumbsAdapter);
        List<ThumbItem> thumbItems = bindImagesToThumb(images, selectedPosition);
        thumbsAdapter.submitList(thumbItems);

        // pager
        binding.subtitle.setText(1 + "/" + images.size());

        binding.images.setAdapter(galleryPagerAdapter);
        binding.images.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                galleryPagerAdapter.notifyPageChanged();
                binding.subtitle.setText((position + 1) + "/" + images.size());
                thumbsAdapter.notifySelection(position);
                binding.thumbs.post(() -> binding.thumbs.smoothScrollToPosition(position));
            }
        });
        galleryPagerAdapter.submitList(images);

        binding.images.setCurrentItem(selectedPosition, true);
    }

    private List<ThumbItem> bindImagesToThumb(List<GalleryItem> images, int selectedPosition) {
        List<ThumbItem> result = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            ThumbItem item = new ThumbItem();
            item.setThumbUrl(images.get(i).getImageUrlOrVideoThumb());
            item.setSelected(i == selectedPosition);
            result.add(item);
        }

        return result;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        routePersistence.dispose(dataKey);
        if (galleryPagerAdapter != null)
            galleryPagerAdapter.notifyOnDestroy();
    }
}
