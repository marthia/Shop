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
import com.poonehmedia.app.databinding.ListItemModuleSuggestionsBannerBinding;
import com.poonehmedia.app.databinding.ListItemModuleSuggestionsBinding;
import com.poonehmedia.app.databinding.ListItemModuleSuggestionsShowAllBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class SuggestionsModuleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int BANNER = 0;
    private final int MAIN = 1;
    private final int END = 2;
    private final DataController dataController;
    private JsonArray items;
    private ClickProvider callback;
    private ModuleMetadata metadata;

    public SuggestionsModuleAdapter(DataController dataController) {

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

        ListItemModuleSuggestionsBinding bindingMain = ListItemModuleSuggestionsBinding.inflate(layoutInflater, parent, false);
        ListItemModuleSuggestionsBannerBinding bindingBanner = ListItemModuleSuggestionsBannerBinding.inflate(layoutInflater, parent, false);
        ListItemModuleSuggestionsShowAllBinding bindingEnd = ListItemModuleSuggestionsShowAllBinding.inflate(layoutInflater, parent, false);

        if (viewType == BANNER)
            return new BannerViewHolder(bindingBanner);
        else if (viewType == MAIN) {
            init(bindingMain);
            return new MainViewHolder(bindingMain);
        } else if (viewType == END)
            return new EndViewHolder(bindingEnd);

        else throw new RuntimeException("expected a defined view type");
    }

    private void init(ListItemModuleSuggestionsBinding binding) {
        GenericListAdapterImp badgesAdapter = new GenericListAdapterImp();
        badgesAdapter.setLayoutRes(R.layout.list_item_badge);
        binding.badges.setNestedScrollingEnabled(false);
        binding.badges.setAdapter(badgesAdapter);
        binding.badges.setItemViewCacheSize(0);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MainViewHolder) {
            int index = position - 1;
            JsonObject item = items.get(index).getAsJsonObject();
            ((MainViewHolder) holder).bind(item);
            holder.itemView.setOnClickListener(v -> callback.onClick(
                    items.get(index).getAsJsonObject(),
                    index
            ));
        } else if (holder instanceof BannerViewHolder) {
            ((BannerViewHolder) holder).bind();
        } else if (holder instanceof EndViewHolder) {
            ((EndViewHolder) holder).bind();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return BANNER;
        else if (metadata != null && position == getItemCount() - 1) return END;
        return MAIN;
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size() + 2;
    }

    public void submitMetaData(ModuleMetadata metadata) {
        this.metadata = metadata;
    }

    private class MainViewHolder extends RecyclerView.ViewHolder {

        private final ListItemModuleSuggestionsBinding binding;

        public MainViewHolder(@NonNull ListItemModuleSuggestionsBinding binding) {
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

            // binding.executePendingBindings();
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


    private class BannerViewHolder extends RecyclerView.ViewHolder {

        private final ListItemModuleSuggestionsBannerBinding binding;

        public BannerViewHolder(@NonNull ListItemModuleSuggestionsBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind() {
            binding.setItem(metadata);
            // binding.executePendingBindings();
        }
    }


    private class EndViewHolder extends RecyclerView.ViewHolder {

        private final ListItemModuleSuggestionsShowAllBinding binding;

        public EndViewHolder(@NonNull ListItemModuleSuggestionsShowAllBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind() {
            binding.setItem(metadata);
            // binding.executePendingBindings();
        }
    }

}
