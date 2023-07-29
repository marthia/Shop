package com.poonehmedia.app.ui.products;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.poonehmedia.app.data.model.FilterData;
import com.poonehmedia.app.databinding.ListItemEmptyBinding;
import com.poonehmedia.app.databinding.ListItemProductsFilterCheckboxBinding;
import com.poonehmedia.app.databinding.ListItemProductsFilterPriceBinding;
import com.poonehmedia.app.databinding.ListItemProductsFilterRadioBinding;
import com.poonehmedia.app.ui.adapter.EmptyViewHolder;
import com.poonehmedia.app.ui.interfaces.OnFilterChangedListener;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class FilterListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int CHECK_BOX = 0;
    private final int RADIO = 1;
    private final int PRICE = 3;
    private OnFilterChangedListener callback;
    private JsonObject item;
    private Map<String, FilterData> selectedValues;

    @Inject
    public FilterListAdapter() {
    }

    public void subscribeCallbacks(OnFilterChangedListener callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ListItemProductsFilterCheckboxBinding checkboxBinding = ListItemProductsFilterCheckboxBinding.inflate(layoutInflater, parent, false);
        ListItemProductsFilterRadioBinding radioBinding = ListItemProductsFilterRadioBinding.inflate(layoutInflater, parent, false);
        ListItemProductsFilterPriceBinding priceBinding = ListItemProductsFilterPriceBinding.inflate(layoutInflater, parent, false);

        if (viewType == CHECK_BOX) return new CheckBoxViewHolder(checkboxBinding);
        else if (viewType == RADIO) return new RadioViewHolder(radioBinding);
        else if (viewType == PRICE) return new PriceViewHolder(priceBinding);

        else {
            ListItemEmptyBinding empty = ListItemEmptyBinding.inflate(layoutInflater, parent, false);
            return new EmptyViewHolder(empty);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof CheckBoxViewHolder)
            ((CheckBoxViewHolder) holder).bind(item.get("filter_option").getAsJsonArray().get(position).getAsJsonObject());
        else if (holder instanceof RadioViewHolder)
            ((RadioViewHolder) holder).bind(item.get("filter_option").getAsJsonArray().get(position).getAsJsonObject());
        else if (holder instanceof PriceViewHolder)
            ((PriceViewHolder) holder).bind(item.get("filter_option").getAsJsonObject());
    }

    @Override
    public int getItemCount() {
        if (item == null) return 0;

        else if (item.get("filter_type").getAsString().equals("cursor")) return 1;

        else return item.get("filter_option").getAsJsonArray().size();
    }

    @Override
    public int getItemViewType(int position) {

        String filterType = item.get("filter_type").getAsString();

        if ("checkbox".equals(filterType)) return CHECK_BOX;
        else if ("radio".equals(filterType) || "list".equals(filterType)) return RADIO;
        else if ("cursor".equals(filterType) && item.get("filter_data").getAsString().equals("price"))
            return PRICE;
        else return -1;
    }

    public void submitList(JsonObject list) {
        this.item = list;
        notifyDataSetChanged();
    }

    public void setSelectedValues(Map<String, FilterData> selectedValues) {
        this.selectedValues = selectedValues;
    }

    private class CheckBoxViewHolder extends RecyclerView.ViewHolder {

        private final ListItemProductsFilterCheckboxBinding binding;

        public CheckBoxViewHolder(@NonNull ListItemProductsFilterCheckboxBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject checkBoxItem) {
            String id = item.get("filter_id").getAsString();
            String x = item.get("filter_namekey").getAsString();
            String checkBoxKey = checkBoxItem.get("key").getAsString();

            // selection from previously selected items
            boolean isPreviouslyChecked = false;
            if (selectedValues.get(id) != null) {
                String s = selectedValues.get(id).getSelectedValues().get(x);
                if (s != null) {
                    List<String> selectedIds = Arrays.asList(s.split("::"));
                    isPreviouslyChecked = selectedIds.contains(checkBoxKey);
                }
            }

            binding.checkBox.setText(checkBoxItem.get("title").getAsString());
            binding.checkBox.setTag(checkBoxKey);
            binding.checkBox.setChecked(selectedValues.size() > 0 && isPreviouslyChecked);

            // listen for selection changes

            binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                callback.handle("checkBox",
                        isChecked,
                        (String) buttonView.getTag(),
                        x,
                        buttonView.getText().toString(),
                        item,
                        getAbsoluteAdapterPosition());
            });
        }
    }

    private class RadioViewHolder extends RecyclerView.ViewHolder {

        private final com.poonehmedia.app.databinding.ListItemProductsFilterRadioBinding binding;
        private int previousCheckedPosition;

        public RadioViewHolder(@NonNull ListItemProductsFilterRadioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject radioItem) {
            String x = item.get("filter_namekey").getAsString();
            String id = item.get("filter_id").getAsString();

            // selection from previously selected items
            binding.radio.setChecked(
                    selectedValues.size() > 0 &&
                            selectedValues.get(id) != null &&
                            selectedValues.get(id).getSelectedValues().get(x) != null &&
                            selectedValues.get(id).getSelectedValues().get(x).equals(radioItem.get("key").getAsString())
            );

            binding.radio.setText(radioItem.get("title").getAsString());
            binding.radio.setTag(radioItem.get("key").getAsString());

            // listen for selection changes
            binding.radio.setOnClickListener(v -> {

                // delete lastCheckedItem selection and select current position item
                callback.handle("radio", true, (String) binding.radio.getTag(), x, binding.radio.getText().toString(), item, getAbsoluteAdapterPosition());
                notifyDataSetChanged();
            });
        }
    }

    private class PriceViewHolder extends RecyclerView.ViewHolder {

        private final ListItemProductsFilterPriceBinding binding;

        public PriceViewHolder(@NonNull ListItemProductsFilterPriceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject priceItem) {
            String x = item.get("filter_namekey").getAsString();

            int min = (int) Math.round(priceItem.get("min").getAsDouble());
            int max = (int) Math.round(priceItem.get("max").getAsDouble());

            binding.slider.setRange(min, max);
            binding.slider.setProgress(min, max);
            binding.slider.setSteps(500);

            String id = item.get("filter_id").getAsString();

            // selection from previously selected items
            if (selectedValues.size() > 0 && selectedValues.get(id) != null) {
                String[] strings = selectedValues.get(id).getSelectedValues().get(x).split(" - ");
                setPriceRange((int) Float.parseFloat(strings[0]), (int) Float.parseFloat(strings[1]));
                int leftHand = (int) Float.parseFloat(strings[0]);
                int rightHand = (int) Float.parseFloat(strings[0]);

                if ((int) Float.parseFloat(strings[1]) > max) rightHand = max;
                if ((int) Float.parseFloat(strings[0]) < min) leftHand = min;
                binding.slider.setProgress(leftHand, rightHand);

            } else {
                setPriceRange(min, max);
            }
            binding.slider.setOnRangeChangedListener(new OnRangeChangedListener() {
                @Override
                public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                    if ((int) Float.parseFloat(String.valueOf(leftValue)) == min && (int) Float.parseFloat(String.valueOf(rightValue)) == max)
                        return;
                    setPriceRange((int) leftValue, (int) rightValue);
                    callback.handle("price", true, (int) leftValue + " - " + (int) rightValue, x, (int) leftValue + " - " + (int) rightValue, item, getAbsoluteAdapterPosition());
                }

                @Override
                public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                }

                @Override
                public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                }
            });
        }

        private void setPriceRange(Object left, Object right) {
            String leftPrice = new DecimalFormat("###,###").format(left);
            String rightPrice = new DecimalFormat("###,###").format(right);
            binding.fromPrice.setText(leftPrice + " تومان");
            binding.toPrice.setText(rightPrice + " تومان");
        }
    }
}