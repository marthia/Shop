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
import com.poonehmedia.app.databinding.CartStepsListItemPaymentBinding;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;
import com.poonehmedia.app.util.ui.AndroidUtils;

public class CartPaymentAdapter extends RecyclerView.Adapter<CartPaymentAdapter.ViewHolder> {

    private final OnParamValueChanged callback;
    private JsonArray items;
    private boolean isClickable = true;
    private RecyclerView recyclerView;

    public CartPaymentAdapter(OnParamValueChanged callback) {
        this.callback = callback;
    }

    public void submitList(JsonArray array) {

        this.items = array;
        recyclerView.post(this::notifyDataSetChanged);
        recyclerView.setAlpha(1f);
        isClickable = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        CartStepsListItemPaymentBinding binding = CartStepsListItemPaymentBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = items.get(position).getAsJsonObject();
        holder.bind(item);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final com.poonehmedia.app.databinding.CartStepsListItemPaymentBinding binding;

        public ViewHolder(@NonNull CartStepsListItemPaymentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.paymentName.setText(item.get("title").getAsString());

            binding.description.setText(item.get("description").getAsString());

            boolean selected = item.get("selected").getAsBoolean();

            if (selected) {
                binding.getRoot().setAlpha(1);
                binding.check.setImageTintList(
                        ColorStateList.valueOf(
                                AndroidUtils.getAttr(
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

                if (!item.get("disabled").getAsBoolean() && isClickable) {

                    if (item.get("payment_actions").getAsJsonObject().has("select")) {
                        recyclerView.setAlpha(.4f);

                        callback.onChanged(item, getAbsoluteAdapterPosition());
                        isClickable = false;
                    }
                }
            });


//            binding.executePendingBindings();
        }
    }


}
