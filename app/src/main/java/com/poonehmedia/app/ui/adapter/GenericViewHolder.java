package com.poonehmedia.app.ui.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.poonehmedia.app.BR;
import com.poonehmedia.app.data.model.DividerData;

public class GenericViewHolder extends RecyclerView.ViewHolder {
    private final ViewDataBinding binding;

    public GenericViewHolder(@NonNull ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /**
     * @param item    data to bind with xml layout
     * @param divider (optional) only used for layouts that need to control visibility of divider based on adapter position
     * @author Marthia
     */
    public void bind(Object item, DividerData divider) {
        binding.setVariable(BR.item, item);
        binding.setVariable(BR.divider, divider);
        binding.executePendingBindings();
    }

    public void handleSelection(boolean selection) {
        binding.setVariable(BR.selection, selection);
        binding.executePendingBindings();
    }
}