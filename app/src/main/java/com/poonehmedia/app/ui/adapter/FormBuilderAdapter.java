package com.poonehmedia.app.ui.adapter;

import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.FormData;
import com.poonehmedia.app.data.model.SpinnerItem;
import com.poonehmedia.app.databinding.ListItemEmptyBinding;
import com.poonehmedia.app.databinding.ListItemFormBinding;
import com.poonehmedia.app.databinding.ListItemFormSpinnerBinding;
import com.poonehmedia.app.ui.interfaces.OnEditCallback;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.MyTextWatcher;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class FormBuilderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TEXT = 1;
    private final int SPINNER = 2;
    private final int HIDDEN = 3;
    private final Map<Integer, FormData> values = new HashMap<>();
    private final Map<Integer, Boolean> errorController = new HashMap<>();
    private JsonArray items;
    private OnEditCallback callback;
    private OnSubmit onSubmitCallback;

    @Inject
    public FormBuilderAdapter() {
    }

    public void subscribeCallbacks(OnEditCallback callback) {
        this.callback = callback;
    }

    public boolean hasErrors() {
        for (Integer index : errorController.keySet()) {
            if (errorController.get(index))
                return true;
        }
        return false;
    }

    public void submitList(JsonArray array) {
        this.items = array;
        notifyDataSetChanged();
    }

    public Map<Integer, FormData> getValues() {
        return values;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TEXT) {
            ListItemFormBinding binding = ListItemFormBinding.inflate(inflater, parent, false);
            return new EditTextViewHolder(binding);
        } else if (viewType == SPINNER) {
            ListItemFormSpinnerBinding spinnerBinding = ListItemFormSpinnerBinding.inflate(inflater, parent, false);
            return new SpinnerViewHolder(spinnerBinding);
        } else if (viewType == HIDDEN) {
            ListItemEmptyBinding empty = ListItemEmptyBinding.inflate(inflater, parent, false);
            return new HiddenViewHolder(empty);
        } else {
            ListItemEmptyBinding empty = ListItemEmptyBinding.inflate(inflater, parent, false);
            return new EmptyViewHolder(empty);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        JsonObject item = items.get(position).getAsJsonObject();
        if (holder instanceof EditTextViewHolder)
            ((EditTextViewHolder) holder).bind(item);
        else if (holder instanceof SpinnerViewHolder)
            ((SpinnerViewHolder) holder).bind(item);
        else if (holder instanceof HiddenViewHolder) {
            ((HiddenViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public int getItemViewType(int position) {
        JsonObject item = items.get(position).getAsJsonObject();

        String fieldType = item.get("field_type").getAsString();
        switch (fieldType) {
            case "text":
                return TEXT;
            case "list":
            case "zone":
                return SPINNER;
            case "hidden":
                return HIDDEN;
            default:
                return -1;
        }
    }

    public void updateSpinner(String key, JsonObject values) {
        JsonObject item = null;
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getAsJsonObject().get("field_namekey").getAsString().equals(key)) {
                item = items.get(i).getAsJsonObject();
                index = i;
                break;
            }
        }
        if (item != null) {
            item.remove("field_value");
            item.add("field_value", values);
            notifyItemChanged(index);
        }

    }

    public void subscribeOnSubmit(OnSubmit onSubmitCallback) {
        this.onSubmitCallback = onSubmitCallback;
    }

    public interface OnSubmit {
        void handle();
    }

    private class EditTextViewHolder extends RecyclerView.ViewHolder {
        private final ListItemFormBinding binding;

        public EditTextViewHolder(@NonNull ListItemFormBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            if (getAbsoluteAdapterPosition() == getItemCount() - 1) {
                binding.editText.setImeOptions(EditorInfo.IME_ACTION_SEND);
            }
            binding.editText.setOnEditorActionListener((v, actionId, event) -> {

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    onSubmitCallback.handle();
                    return true;
                } else return false;
            });

            JsonObject fieldOptions = item.get("field_options").getAsJsonObject();


            boolean hasMaxLength = JsonHelper.has(fieldOptions, "maxlength");
            if (hasMaxLength && !fieldOptions.get("maxlength").getAsString().equals("0")) {
                String maxLength = fieldOptions.get("maxlength").getAsString();
                binding.editText.setFilters(
                        new InputFilter[]{
                                new InputFilter.LengthFilter(Integer.parseInt(maxLength))
                        }
                );
            }


            String errorMessage = fieldOptions.get("errormessage").getAsString();

            String regex = null;
            boolean shouldUseRegex = JsonHelper.has(fieldOptions, "regex");
            if (shouldUseRegex) {
                regex = fieldOptions.get("regex").getAsString();
            }
            String finalRegex = regex;
            binding.editText.addTextChangedListener(new MyTextWatcher(changedValue -> {
                boolean hasRegex = finalRegex != null && !finalRegex.isEmpty();

                if (item.get("field_required").getAsString().equals("1")) {

                    if (TextUtils.isEmpty(changedValue)) {
                        errorController.put(getAbsoluteAdapterPosition(), true);
                        binding.editTextLayout.setError(errorMessage);
                    } else if (hasRegex) {

                        if (changedValue.matches(finalRegex))
                            saveEditTextValue(item, changedValue);
                        else {
                            errorController.put(getAbsoluteAdapterPosition(), true);
                            binding.editTextLayout.setError(errorMessage);
                        }

                    } else
                        saveEditTextValue(item, changedValue);

                } else if (hasRegex) { // check if matches regex
                    if (changedValue.matches(finalRegex))
                        saveEditTextValue(item, changedValue);
                    else {
                        binding.editTextLayout.setError(errorMessage);
                        errorController.put(getAbsoluteAdapterPosition(), true);
                    }
                } else saveEditTextValue(item, changedValue);
            }));

            if (JsonHelper.has(fieldOptions, "format")) {
                String inputType = fieldOptions.get("format").getAsString();
                switch (inputType) {
                    case "number":
                        binding.editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                        break;
                    case "password":
                        binding.editTextLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                        binding.editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                    case "email":
                        binding.editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        break;
                }
            }

            binding.title.setText(item.get("field_title").getAsString());
            binding.editText.setText(item.get("field_current_value").getAsString());

            boolean hasPlaceholders = JsonHelper.has(fieldOptions, "placeholder");
            if (hasPlaceholders) {
                JsonElement placeholder = fieldOptions.get("placeholder");
                if (placeholder != null)
                    binding.editText.setHint(placeholder.getAsString());
            }
        }

        private void saveEditTextValue(JsonObject item, String s) {
            errorController.put(getAbsoluteAdapterPosition(), false);
            binding.editTextLayout.setErrorEnabled(false);
            values.put(getAbsoluteAdapterPosition(), new FormData(
                    item.get("field_namekey").getAsString(),
                    s,
                    item.get("field_namekey").getAsString())
            );
        }
    }

    private class SpinnerViewHolder extends RecyclerView.ViewHolder {
        private final ListItemFormSpinnerBinding binding;

        public SpinnerViewHolder(@NonNull ListItemFormSpinnerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void bind(JsonObject item) {

            List<SpinnerItem> values = bindFiledValuesToSpinnerItem(item);

            binding.title.setText(item.get("field_title").getAsString());
            ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<>(
                    itemView.getContext(),
                    R.layout.spinner_item,
                    values
            );

            binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    FormBuilderAdapter.this.values.put(
                            getAbsoluteAdapterPosition(),
                            new FormData(
                                    ((SpinnerItem) parent.getItemAtPosition(position)).getKey(),
                                    ((SpinnerItem) parent.getItemAtPosition(position)).getValue(),
                                    items.get(getAbsoluteAdapterPosition()).getAsJsonObject().get("field_namekey").getAsString()
                            ));

                    callback.handle(
                            items.get(getAbsoluteAdapterPosition()),
                            ((SpinnerItem) parent.getItemAtPosition(position)).getKey()
                    );
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            binding.spinner.post(
                    () -> binding.spinner.setSelection(
                            findSelectedPosition(item.get("field_current_value").getAsString(), values)
                    )
            );

            binding.spinner.setAdapter(adapter);
        }

        private List<SpinnerItem> bindFiledValuesToSpinnerItem(JsonObject items) {
            List<SpinnerItem> result = new ArrayList<>();
            try {
                JsonObject fieldValue = items.get("field_value").getAsJsonObject();
                for (String key : fieldValue.keySet()) {

                    SpinnerItem item = new SpinnerItem();
                    String value = fieldValue.get(key).getAsJsonObject().get("value").getAsString();
                    item.setKey(key);
                    item.setValue(value);

                    result.add(item);
                }
            } catch (Exception e) {
                Log.e("fields", e.getMessage());
                ACRA.getErrorReporter().handleException(new CrashReportException("Could not bind Field_Value to Spinner Item: Possible Reason Could be expecting JsonObject but got something else", e));
            }
            return result;
        }

        public int findSelectedPosition(String selectedKey, List<SpinnerItem> values) {
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).getKey().equals(selectedKey))
                    return i;
            }
            return 0;
        }
    }

    private class HiddenViewHolder extends RecyclerView.ViewHolder {

        public HiddenViewHolder(@NonNull ListItemEmptyBinding binding) {
            super(binding.getRoot());
        }

        public void bind(JsonObject item) {
            values.put(getAbsoluteAdapterPosition(), new FormData(
                    item.get("field_namekey").getAsString(),
                    item.get("field_current_value").getAsString(),
                    item.get("field_namekey").getAsString())
            );
        }

    }

}
