package com.poonehmedia.app.ui.product;


import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.databinding.ListItemModuleProductsBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class SimilarProductsAdapter extends RecyclerView.Adapter<SimilarProductsAdapter.MainViewHolder> {

    private final DataController dataController;
    private JsonArray items;
    private ClickProvider callback;

    @Inject
    public SimilarProductsAdapter(DataController dataController) {
        this.dataController = dataController;
    }

    public void subscribeCallbacks(ClickProvider callback) {
        this.callback = callback;
    }

    public void submitList(JsonArray items) {
        this.items = items;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemModuleProductsBinding binding = ListItemModuleProductsBinding.inflate(layoutInflater, parent, false);
        init(binding);
        return new MainViewHolder(binding);

    }

    private void init(ListItemModuleProductsBinding binding) {
        GenericListAdapterImp<JsonElement> badgesAdapter = new GenericListAdapterImp<>();

        badgesAdapter.setLayoutRes(R.layout.list_item_badge);
        binding.badges.setNestedScrollingEnabled(false);
        binding.badges.setAdapter(badgesAdapter);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        JsonObject item = items.get(position).getAsJsonObject();

        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    protected class MainViewHolder extends RecyclerView.ViewHolder {

        private final ListItemModuleProductsBinding binding;

        public MainViewHolder(@NonNull ListItemModuleProductsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);

            List<Price> prices = dataController.extractPrice(item, null);
            if (prices.size() > 0) binding.setPriceItem(prices.get(0));

            if (JsonHelper.has(item, "badges") && JsonHelper.isNotEmptyNorNull(item.get("badges"))) {
                binding.badges.setVisibility(View.VISIBLE);
                ((GenericListAdapterImp<JsonElement>) binding.badges.getAdapter()).submitList(item.get("badges").getAsJsonArray());
            } else binding.badges.setVisibility(View.GONE);


            itemView.setOnClickListener(v -> callback.onClick(
                    items.get(getAbsoluteAdapterPosition()).getAsJsonObject(),
                    getAbsoluteAdapterPosition()
            ));

            if (JsonHelper.has(item, "timer")) {

                JsonObject timer = item.get("timer").getAsJsonObject();
                startTimer(timer.get("start").getAsLong(), timer.get("end").getAsLong());

            } else binding.offTimer.setVisibility(View.GONE);

            binding.executePendingBindings();
        }

        private void startTimer(long start, long end) {
            long compare = end - System.currentTimeMillis();
            long currentProgress = System.currentTimeMillis() - start;
            binding.offProgress.setMax((int) compare);
            binding.offProgress.setProgress((int) currentProgress);
            new CountDownTimer(compare, 1000) {

                public void onTick(long millisUntilFinished) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(millisUntilFinished);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

                    binding.offTime.setText(dateFormat.format(calendar.getTime()));
                    binding.offProgress.setProgress((int) (System.currentTimeMillis() - start));
                }

                public void onFinish() {
                    binding.offTimer.setVisibility(View.GONE);
                }
            }.start();

        }

    }

}
