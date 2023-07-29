package com.poonehmedia.app.ui.compare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.poonehmedia.app.components.adaptiveTable.LinkedAdaptiveTableAdapter;
import com.poonehmedia.app.components.adaptiveTable.ViewHolderImpl;
import com.poonehmedia.app.data.model.CompareItem;
import com.poonehmedia.app.databinding.ListItemCompareCfBinding;
import com.poonehmedia.app.databinding.ListItemCompareImageBinding;
import com.poonehmedia.app.databinding.ListItemCompareTitleBinding;
import com.poonehmedia.app.databinding.ListItemCompareTopRightBinding;
import com.poonehmedia.app.util.ui.AndroidUtils;

import java.util.Map;

public class AdaptiveAdapter extends LinkedAdaptiveTableAdapter<ViewHolderImpl> {
    private final Map<Integer, Map<Integer, CompareItem>> items;
    private final int columnCount;
    private final int rowCount;
    private final Context context;
    private final Map<Integer, Integer> rowHeightController;

    public AdaptiveAdapter(Context context, Map<Integer, Map<Integer, CompareItem>> items, Map<Integer, Integer> rowHeightController) {
        this.context = context;
        this.items = items;
        this.rowCount = items.size();
        this.columnCount = items.get(0).size();
        this.rowHeightController = rowHeightController;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @NonNull
    @Override
    public ViewHolderImpl onCreateItemViewHolder(@NonNull ViewGroup parent) {
        return new ValueViewHolder(
                ListItemCompareCfBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @NonNull
    @Override
    public ViewHolderImpl onCreateColumnHeaderViewHolder(@NonNull ViewGroup parent) {
        return new ImageViewHolder(
                ListItemCompareImageBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @NonNull
    @Override
    public ViewHolderImpl onCreateRowHeaderViewHolder(@NonNull ViewGroup parent) {
        return new TitleViewHolder(
                ListItemCompareTitleBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @NonNull
    @Override
    public ViewHolderImpl onCreateLeftTopHeaderViewHolder(@NonNull ViewGroup parent) {
        return new CornerViewHolder(
                ListItemCompareTopRightBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderImpl holder, int row, int column) {
        bind(holder, row, column);
    }

    public void bind(@NonNull ViewHolderImpl holder, int row, int column) {

        CompareItem item = getItem(row, column);
        if (item == null)
            return;

        if (holder instanceof TitleViewHolder)
            ((TitleViewHolder) holder).bind(item);
        else if (holder instanceof ValueViewHolder)
            ((ValueViewHolder) holder).bind(item);
        else if (holder instanceof ImageViewHolder)
            ((ImageViewHolder) holder).bind(item);
    }

    private CompareItem getItem(int row, int column) {
        return items.get(row).get(column);
    }

    @Override
    public void onBindHeaderColumnViewHolder(@NonNull ViewHolderImpl holder, int column) {
        bind(holder, 0, column);
    }

    @Override
    public void onBindHeaderRowViewHolder(@NonNull ViewHolderImpl holder, int row) {
        bind(holder, row, 0);
    }

    @Override
    public void onBindLeftTopHeaderViewHolder(@NonNull ViewHolderImpl holder) {
        bind(holder, 0, 0);
    }

    @Override
    public int getColumnWidth(int column) {
        return (int) AndroidUtils.getPixels(200, context);
    }

    @Override
    public int getHeaderColumnHeight() {
        return (int) AndroidUtils.getPixels(120, context);
    }

    @Override
    public int getRowHeight(int row) {
        Integer integer = rowHeightController.get(row);
        if (integer == null) return (int) AndroidUtils.getPixels(28, context);
        else return integer;
    }

    @Override
    public int getHeaderRowWidth() { // top corner
        return (int) AndroidUtils.getPixels(70, context);
    }

    private static class CornerViewHolder extends ViewHolderImpl {

        public CornerViewHolder(@NonNull ListItemCompareTopRightBinding binding) {
            super(binding.getRoot());
        }
    }

    private static class TitleViewHolder extends ViewHolderImpl {

        private final ListItemCompareTitleBinding binding;

        public TitleViewHolder(@NonNull ListItemCompareTitleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CompareItem item) {
            binding.setItem(item.getTitle());
        }
    }

    private static class ValueViewHolder extends ViewHolderImpl {

        private final ListItemCompareCfBinding binding;

        public ValueViewHolder(@NonNull ListItemCompareCfBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CompareItem item) {
            binding.setItem(item.getValue());
        }
    }

    private static class ImageViewHolder extends ViewHolderImpl {

        private final ListItemCompareImageBinding binding;

        public ImageViewHolder(@NonNull ListItemCompareImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CompareItem item) {
            binding.setItem(item.getValue());
        }
    }
}