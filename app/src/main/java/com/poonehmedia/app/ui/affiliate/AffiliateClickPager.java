package com.poonehmedia.app.ui.affiliate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.poonehmedia.app.BR;
import com.poonehmedia.app.data.model.MyDiffUtil;

import javax.inject.Inject;

public class AffiliateClickPager extends PagingDataAdapter<JsonObject, AffiliateClickPager.ViewHolder> {

    private int layoutRes;

    @Inject
    public AffiliateClickPager() {
        super(new MyDiffUtil());
    }

    public void setLayoutRes(@LayoutRes int layoutRes) {
        this.layoutRes = layoutRes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.getContext()),
                        layoutRes,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = getItem(position);
        holder.bind(item);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {


        private final ViewDataBinding binding;

        public ViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setVariable(BR.item, item);
//            binding.executePendingBindings();
        }
    }
}
