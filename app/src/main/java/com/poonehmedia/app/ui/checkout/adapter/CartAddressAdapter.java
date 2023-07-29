package com.poonehmedia.app.ui.checkout.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.CartStepsListItemAddressBinding;
import com.poonehmedia.app.ui.interfaces.OnEditCallback;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;
import com.poonehmedia.app.util.ui.AndroidUtils;

public class CartAddressAdapter extends RecyclerView.Adapter<CartAddressAdapter.ViewHolder> {

    private final OnParamValueChanged callback;
    private final OnEditCallback editCallback;
    private JsonArray items;
    private RecyclerView recyclerView;
    private boolean isClickable = true;

    public CartAddressAdapter(OnParamValueChanged callback, OnEditCallback editCallback) {
        this.callback = callback;
        this.editCallback = editCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        CartStepsListItemAddressBinding binding = CartStepsListItemAddressBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = items.get(position).getAsJsonObject();
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void submitList(JsonArray items) {

        this.items = items;
        recyclerView.post(this::notifyDataSetChanged);
        recyclerView.setAlpha(1f);
        isClickable = true;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final CartStepsListItemAddressBinding binding;

        public ViewHolder(@NonNull CartStepsListItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.address.setText(item.get("full_format_address").getAsString());

            binding.edit.setOnClickListener(v -> editCallback.handle(item, ""));


            // handle default checked items
            boolean selected = item.get("selected").getAsBoolean();

            if (selected) {
                binding.getRoot().setAlpha(1);
                binding.check.setImageTintList(
                        ColorStateList.valueOf(AndroidUtils.getAttr(
                                binding.check.getContext(),
                                R.attr.colorSecondary)
                        )
                );
            } else {
                binding.check.setImageTintList(
                        ColorStateList.valueOf(Color.GRAY)
                );
                binding.getRoot().setAlpha(0.4f);
            }


            itemView.setOnClickListener(v -> {
                if (isClickable) {
                    if (item.get("address_actions").getAsJsonObject().has("select")) {
                        recyclerView.setAlpha(.4f);

                        callback.onChanged(item, getAbsoluteAdapterPosition());
                        isClickable = false;
                    }
                }
            });
        }
    }
}
