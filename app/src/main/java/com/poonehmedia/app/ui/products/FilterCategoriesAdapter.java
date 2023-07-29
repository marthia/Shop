package com.poonehmedia.app.ui.products;

import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.FilterCategories;
import com.poonehmedia.app.data.model.FilterData;
import com.poonehmedia.app.databinding.ListItemFilterCategoryBinding;
import com.poonehmedia.app.databinding.ListItemProductsFilterEditTextBinding;
import com.poonehmedia.app.databinding.ListItemProductsFilterSwitchBinding;
import com.poonehmedia.app.ui.interfaces.OnFilterChangedListener;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class FilterCategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int CATEGORY = 1;
    private final int EDIT_TEXT = 2;
    private final int SWITCH = 3;

    private ArrayList<FilterCategories> list;
    private OnParamValueChanged callback;
    private OnFilterChangedListener onFilterChangedListener;
    private Map<String, FilterData> selectedValues;

    @Inject
    public FilterCategoriesAdapter() {
    }


    @Override
    public int getItemViewType(int position) {
        String type = list.get(position).getType();

        if ("text".equals(type)) return EDIT_TEXT;
        else if ("switch".equals(type)) return SWITCH;
        else if ("cursor".equals(type) || "radio".equals(type) || "checkbox".equals(type))
            return CATEGORY;
        else return -1;
    }

    public void subscribeCallbacks(OnParamValueChanged callback) {
        this.callback = callback;
    }

    public void subscribeFilterChanged(OnFilterChangedListener callback) {
        this.onFilterChangedListener = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemProductsFilterSwitchBinding switchViewHolder = ListItemProductsFilterSwitchBinding.inflate(layoutInflater, parent, false);
        ListItemProductsFilterEditTextBinding editTextBinding = ListItemProductsFilterEditTextBinding.inflate(layoutInflater, parent, false);
        ListItemFilterCategoryBinding binding = ListItemFilterCategoryBinding.inflate(layoutInflater, parent, false);

        if (viewType == SWITCH) {
            return new SwitchViewHolder(switchViewHolder);
        } else if (viewType == EDIT_TEXT)
            return new EditTextViewHolder(editTextBinding);
        else if (viewType == CATEGORY)
            return new CategoryViewHolder(binding);

        else throw new RuntimeException("Only a set of predefined types are supported!");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FilterCategories item = list.get(position);

        if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).bind(item);
            holder.itemView.setOnClickListener(((CategoryViewHolder) holder));
        } else if (holder instanceof SwitchViewHolder)
            ((SwitchViewHolder) holder).bind(item);
        else if (holder instanceof EditTextViewHolder)
            ((EditTextViewHolder) holder).bind(item);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void submitList(ArrayList<FilterCategories> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public ArrayList<FilterCategories> getCurrentList() {
        return list;
    }

    public void setSelectedValues(Map<String, FilterData> selectedValues) {
        this.selectedValues = selectedValues;
    }

    private class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ListItemFilterCategoryBinding binding;

        public CategoryViewHolder(@NonNull ListItemFilterCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FilterCategories item) {
            binding.setItem(item);
            // binding.executePendingBindings();
        }

        @Override
        public void onClick(View v) {
            FilterCategories obj = list.get(getAbsoluteAdapterPosition());
            callback.onChanged(obj, getAbsoluteAdapterPosition());
        }
    }

    private class SwitchViewHolder extends RecyclerView.ViewHolder {

        private final ListItemProductsFilterSwitchBinding binding;

        public SwitchViewHolder(@NonNull ListItemProductsFilterSwitchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FilterCategories filterCategories) {
            JsonObject item = filterCategories.getMetadata();

            String id = item.get("filter_id").getAsString();
            String x = item.get("filter_namekey").getAsString();
            JsonObject checkBoxItem = item.get("filter_option").getAsJsonArray().get(0).getAsJsonObject();
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
                onFilterChangedListener.handle("checkBox",
                        isChecked,
                        (String) buttonView.getTag(),
                        x,
                        buttonView.getText().toString(),
                        item,
                        getAbsoluteAdapterPosition());
            });
        }
    }

    private class EditTextViewHolder extends RecyclerView.ViewHolder {

        private final ListItemProductsFilterEditTextBinding binding;

        public EditTextViewHolder(@NonNull ListItemProductsFilterEditTextBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FilterCategories filterCategories) {
            JsonObject item = filterCategories.getMetadata();

            String x = item.get("filter_namekey").getAsString();
            String id = item.get("filter_id").getAsString();

//            binding.editText.setHint(filterCategories.getTitle());

//            if (selectedValues.size() > 0 && selectedValues.get(id) != null) {
//
//                String value = selectedValues.get(id).getSelectedValues().get(x);
//
//                // prevent resetting the value which would lead to vicious loop
//                if (TextUtils.isEmpty(binding.editText.getText()) || !binding.editText.getText().toString().equals(value))
//                    binding.editText.setText(value);
//
//            } else if (!TextUtils.isEmpty(binding.editText.getText()))
//                binding.editText.setText("");

            // selection from previously selected items
            if (selectedValues.size() > 0 && selectedValues.get(id) != null)
                binding.editText.setText(selectedValues.get(id).getSelectedValues().get(x));
            else binding.editText.setText("");

            binding.buttonSearch.setOnClickListener(v -> {
                Editable editable = binding.editText.getText();
                if (TextUtils.isEmpty(editable)) {
                    onFilterChangedListener.handle("text", false, null, x, null, item, getAbsoluteAdapterPosition());
                } else if (editable.toString().length() > 2) {
                    onFilterChangedListener.handle("text", true, editable.toString(), x, editable.toString(), item, getAbsoluteAdapterPosition());
                }
            });

//            binding.editText.addTextChangedListener(new DelayedTextWatcher(s -> {
//                        if (s.isEmpty())
//                            onFilterChangedListener.handle(
//                                    "text",
//                                    false,
//                                    null,
//                                    x,
//                                    s,
//                                    item,
//                                    getAbsoluteAdapterPosition()
//                            );
//                        else if (s.length() > 2)
//                            onFilterChangedListener.handle(
//                                    "text",
//                                    true,
//                                    s,
//                                    x,
//                                    s,
//                                    item,
//                                    getAbsoluteAdapterPosition()
//                            );
//                    }, 1500)
//            );
        }
    }
}
