package com.poonehmedia.app.ui.modules;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.library.baseAdapters.BR;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.ModuleMetadata;
import com.poonehmedia.app.databinding.ListItemScrollModuleBottomBinding;
import com.poonehmedia.app.databinding.ListItemScrollModuleLeftBinding;
import com.poonehmedia.app.databinding.ListItemScrollModuleRightBinding;
import com.poonehmedia.app.databinding.ListItemScrollModuleTopBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;

public class ScrollModuleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
        ListItemScrollModuleTopBinding top = ListItemScrollModuleTopBinding.inflate(inflater, parent, false);
        ListItemScrollModuleBottomBinding bottom = ListItemScrollModuleBottomBinding.inflate(inflater, parent, false);
        ListItemScrollModuleLeftBinding left = ListItemScrollModuleLeftBinding.inflate(inflater, parent, false);
        ListItemScrollModuleRightBinding right = ListItemScrollModuleRightBinding.inflate(inflater, parent, false);

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
            String imagePosition = params.getImagePosition();
            switch (imagePosition) {
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

    public void setParams(ModuleMetadata params) {

        this.params = params;
    }

    private class TopViewHolder extends ScrollModuleViewHolder {
        public TopViewHolder(@NonNull ViewDataBinding binding) {
            super(binding);
        }
    }


    private class BottomViewHolder extends ScrollModuleViewHolder {
        public BottomViewHolder(@NonNull ViewDataBinding binding) {
            super(binding);
        }
    }


    private class LeftViewHolder extends ScrollModuleViewHolder {
        public LeftViewHolder(@NonNull ViewDataBinding binding) {
            super(binding);
        }
    }

    private class RightViewHolder extends ScrollModuleViewHolder {
        public RightViewHolder(@NonNull ViewDataBinding binding) {
            super(binding);
        }
    }

    private class ScrollModuleViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public ScrollModuleViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setVariable(BR.item, item);
            binding.setVariable(BR.params, params);

            if (params.isClickable())
                itemView.setOnClickListener(v ->
                        callback.onClick(item, getAbsoluteAdapterPosition())
                );
        }
    }

}
