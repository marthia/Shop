package com.poonehmedia.app.ui.modules;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.ListItemCategoriesBinding;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.poonehmedia.app.util.base.GlideHelper;

public class CategoriesModuleAdapter extends RecyclerView.Adapter<CategoriesModuleAdapter.ViewHolder> {
    private JsonArray items;
    private ClickProvider callback;

    public void subscribeCallbacks(ClickProvider callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemCategoriesBinding binding = ListItemCategoriesBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = items.get(position).getAsJsonObject();
        holder.bind(item);
    }

    public void submitList(JsonArray items) {
        this.items = items;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }


    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final ListItemCategoriesBinding binding;

        public ViewHolder(@NonNull ListItemCategoriesBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.title.setText(item.get("title").getAsString());
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(
                    itemView.getContext().getResources().getDimensionPixelSize(R.dimen.large_component_corner_radius)
            ));
            GlideHelper.setImage(binding.image, item.get("image").getAsString(), requestOptions);
            itemView.setOnClickListener(v ->
                    callback.onClick(item, getAbsoluteAdapterPosition())
            );
        }
    }
}
