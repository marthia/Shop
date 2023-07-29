package com.poonehmedia.app.ui.modules;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.BR;
import com.poonehmedia.app.data.model.ModuleMetadata;
import com.poonehmedia.app.databinding.ListItemTrendingModuleBottomBinding;
import com.poonehmedia.app.databinding.ListItemTrendingModuleLeftBinding;
import com.poonehmedia.app.databinding.ListItemTrendingModuleRightBinding;
import com.poonehmedia.app.databinding.ListItemTrendingModuleTopBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;

public class TrendingModuleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    JsonArray items;
    int TOP = 1;
    int BOTTOM = 2;
    int LEFT = 3;
    int RIGHT = 4;
    private ClickProvider callback;
    private ModuleMetadata params;

    public void subscribeCallbacks(ClickProvider callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemTrendingModuleTopBinding top = ListItemTrendingModuleTopBinding.inflate(inflater, parent, false);
        ListItemTrendingModuleBottomBinding bottom = ListItemTrendingModuleBottomBinding.inflate(inflater, parent, false);
        ListItemTrendingModuleLeftBinding left = ListItemTrendingModuleLeftBinding.inflate(inflater, parent, false);
        ListItemTrendingModuleRightBinding right = ListItemTrendingModuleRightBinding.inflate(inflater, parent, false);

        if (viewType == TOP)
            return new TopViewHolder(top);
        else if (viewType == BOTTOM)
            return new BottomViewHolder(bottom);
        else if (viewType == LEFT)
            return new LeftViewHolder(left);
        else if (viewType == RIGHT)
            return new RightViewHolder(right);

        else throw new RuntimeException("Cannot support provided type");

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        JsonObject item = items.get(position).getAsJsonObject();

        if (holder instanceof TopViewHolder)
            ((TopViewHolder) holder).bind(item);
        else if (holder instanceof BottomViewHolder)
            ((BottomViewHolder) holder).bind(item);
        else if (holder instanceof LeftViewHolder)
            ((LeftViewHolder) holder).bind(item);
        else if (holder instanceof RightViewHolder)
            ((RightViewHolder) holder).bind(item);
    }

    @Override
    public int getItemViewType(int position) {
        if (params != null) {
            switch (params.getImagePosition()) {
                case "t":
                    return TOP;
                case "b":
                    return BOTTOM;
                case "l":
                    return LEFT;
                case "r":
                    return RIGHT;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void submitList(JsonArray items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setMetadata(ModuleMetadata params) {
        this.params = params;
    }

    private class TopViewHolder extends TrendingModuleViewHolder {
        public TopViewHolder(@NonNull ViewDataBinding binding) {
            super(binding);
        }
    }

    private class BottomViewHolder extends TrendingModuleViewHolder {
        public BottomViewHolder(@NonNull ViewDataBinding binding) {
            super(binding);
        }
    }

    private class LeftViewHolder extends TrendingModuleViewHolder {
        public LeftViewHolder(@NonNull ViewDataBinding binding) {
            super(binding);
        }
    }

    private class RightViewHolder extends TrendingModuleViewHolder {
        public RightViewHolder(@NonNull ViewDataBinding binding) {
            super(binding);
        }
    }

    private class TrendingModuleViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public TrendingModuleViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setVariable(BR.item, item);
            binding.setVariable(BR.params, params);

//            try {
//
//                View container = binding.getRoot().findViewById(R.id.container);
//                GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) container.getLayoutParams();
//
//                if (!params.isShowTitle() && !params.isShowDate() && !params.isShowText())
//                    layoutParams.topMargin =
//                            (int) AndroidUtils.getPixels(8, binding.getRoot().getContext());
//                else
//                    layoutParams.topMargin =
//                            (int) AndroidUtils.getPixels(16, binding.getRoot().getContext());
//            } catch (Exception e) {
//                Log.e("change margin", e.getMessage());
//            }

            if (params.isClickable())
                itemView.setOnClickListener(v -> callback.onClick(item, getAbsoluteAdapterPosition()));

//            binding.executePendingBindings();
        }

    }

}