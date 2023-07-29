package com.poonehmedia.app.ui.products;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poonehmedia.app.data.model.Filter;
import com.poonehmedia.app.data.model.FilterData;
import com.poonehmedia.app.data.model.SortItem;
import com.poonehmedia.app.databinding.ListItemSortBinding;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class SortListAdapter extends RecyclerView.Adapter<SortListAdapter.ViewHolder> {


    private List<SortItem> items;
    private SharedFilterProductsViewModel sharedViewModel;
    private OnParamValueChanged callback;
    private Map<String, FilterData> selectedValues;
    private int lastSelectedPosition = -2;

    @Inject
    public SortListAdapter() {
    }

    public void setSharedViewModel(SharedFilterProductsViewModel sharedViewModel) {
        this.sharedViewModel = sharedViewModel;
    }

    public void subscribeCallbacks(OnParamValueChanged callback) {
        this.callback = callback;
    }

    public void submitSelectedValues(Map<String, FilterData> selectedValues) {
        this.selectedValues = selectedValues;
    }


    public void submitList(List<SortItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemSortBinding binding = ListItemSortBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SortItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final com.poonehmedia.app.databinding.ListItemSortBinding binding;

        public ViewHolder(@NonNull ListItemSortBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SortItem item) {

            binding.setItem(item);
            String id = item.getData().get("filter_id").getAsString();
            String x = item.getData().get("filter_namekey").getAsString();

            // selection from previously selected items
            if (selectedValues.size() > 0 && selectedValues.get(id) != null && selectedValues.get(id).getParentPosition() == getAbsoluteAdapterPosition()) {
                binding.check.setVisibility(View.VISIBLE);
            } else binding.check.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                boolean isChecked = false;
                if (lastSelectedPosition != getAbsoluteAdapterPosition())
                    isChecked = true;

                Filter filter = new Filter();
                filter.setChecked(isChecked);
                filter.setType("sort");
                filter.setOptionKey(x);
                filter.setValue(item.getAction());
                filter.setSubtitle(item.getTitle());
                filter.setMetadata(item.getData());
                filter.setParentPos(getAbsoluteAdapterPosition());


                sharedViewModel.setFilterValues(filter);

                // avoid double evaluation
                lastSelectedPosition = getAbsoluteAdapterPosition();

                notifyDataSetChanged();
                callback.onChanged(item, getAbsoluteAdapterPosition());
            });


            // binding.executePendingBindings();
        }
    }
}
