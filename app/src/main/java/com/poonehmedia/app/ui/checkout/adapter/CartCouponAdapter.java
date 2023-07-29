package com.poonehmedia.app.ui.checkout.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.databinding.CartStepsListItemCouponBinding;
import com.poonehmedia.app.ui.interfaces.CouponAction;

public class CartCouponAdapter extends RecyclerView.Adapter<CartCouponAdapter.ViewHolder> {

    private final CouponAction actionCallback;
    private JsonArray items;

    public CartCouponAdapter(CouponAction actionCallback) {
        this.actionCallback = actionCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                CartStepsListItemCouponBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
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

    public void submitList(JsonArray content) {
        items = content;
        notifyDataSetChanged();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final CartStepsListItemCouponBinding binding;

        public ViewHolder(@NonNull CartStepsListItemCouponBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);

            binding.deleteBtn.setEnabled(true);

            binding.deleteBtn.setOnClickListener(v -> {
                binding.deleteBtn.setEnabled(false);
                actionCallback.onClick(item, null);
            });

            binding.submit.setEnabled(true);

            binding.submit.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(binding.editText.getText())) {
                    binding.submit.setEnabled(false);
                    actionCallback.onClick(item, binding.editText.getText().toString());
                }
            });
        }
    }
}
