package com.poonehmedia.app.ui.modules;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.ModuleMetadata;
import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.databinding.ListItemModuleProductsBinding;
import com.poonehmedia.app.databinding.ListItemModuleProductsShowAllBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ProductsModuleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int MAIN = 1;
    private final int END = 2;
    private final DataController dataController;
    private JsonArray items;
    private ClickProvider callback;
    private ModuleMetadata metadata;

    public ProductsModuleAdapter(DataController dataController) {
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ListItemModuleProductsBinding bindingMain = ListItemModuleProductsBinding.inflate(layoutInflater, parent, false);
        ListItemModuleProductsShowAllBinding bindingEnd = ListItemModuleProductsShowAllBinding.inflate(layoutInflater, parent, false);

        if (viewType == MAIN) {
            init(bindingMain);
            return new MainVH(bindingMain);
        } else if (viewType == END)
            return new EndViewHolder(bindingEnd);

        else throw new RuntimeException("expected a defined view type");
    }

    private void init(ListItemModuleProductsBinding binding) {
        GenericListAdapterImp badgesAdapter = new GenericListAdapterImp();

        badgesAdapter.setLayoutRes(R.layout.list_item_badge);

        binding.badges.setNestedScrollingEnabled(false);
        binding.badges.setAdapter(badgesAdapter);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int i) {
        if (h instanceof MainVH) {
            JsonObject item = items.get(i).getAsJsonObject();
            ((MainVH) h).bind(item);
            h.itemView.setOnClickListener(v ->
                    callback.onClick(items.get(i).getAsJsonObject(), i)
            );
        } else if (h instanceof EndViewHolder) ((EndViewHolder) h).bind();
    }

    @Override
    public int getItemViewType(int position) {
        if (metadata != null && position == getItemCount() - 1) return END;
        return MAIN;
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size() + 1;
    }

    public void submitMetaData(ModuleMetadata metadata) {
        this.metadata = metadata;
    }

    private class MainVH extends RecyclerView.ViewHolder {

        private final ListItemModuleProductsBinding binding;

        public MainVH(@NonNull ListItemModuleProductsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);

            List<Price> prices = dataController.extractPrice(item, null);
            if (prices.size() > 0) binding.setPriceItem(prices.get(0));

            if (JsonHelper.has(item, "badges") && JsonHelper.isNotEmptyNorNull(item.get("badges"))) {
                binding.badges.setVisibility(View.VISIBLE);
                ((GenericListAdapterImp) binding.badges.getAdapter()).submitList(item.get("badges").getAsJsonArray());
            } else binding.badges.setVisibility(View.GONE);

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

    private class EndViewHolder extends RecyclerView.ViewHolder {

        private final ListItemModuleProductsShowAllBinding binding;

        public EndViewHolder(@NonNull ListItemModuleProductsShowAllBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind() {
            binding.setItem(metadata);
            binding.executePendingBindings();
        }
    }

}
