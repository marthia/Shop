package com.poonehmedia.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.poonehmedia.app.BR;
import com.poonehmedia.app.data.model.GroupedList;
import com.poonehmedia.app.util.ui.StickyHeaderRecyclerItemDecoration;

import java.util.List;

import javax.inject.Inject;

public class SectionedRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyHeaderRecyclerItemDecoration.StickyHeaderInterface {

    private final int header = 0;
    private final int content = 1;

    private int layoutRes;
    private int headerLayoutRes;
    private List<GroupedList> items;

    @Inject
    public SectionedRecyclerAdapter() {
    }

    public void setLayoutResources(int header, int content) {

        this.headerLayoutRes = header;
        this.layoutRes = content;
    }

    public void submitList(List<GroupedList> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    @Override
    public final int getItemViewType(int position) {
        return items.get(position).getViewType() == GroupedList.ViewType.HEADER ? header : content;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == header) {
            return new HeaderViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), headerLayoutRes, parent, false)
            );
        } else if (viewType == content)
            return new ContentViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutRes, parent, false)
            );
        else throw new RuntimeException("Cannot inflate requested view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContentViewHolder)
            ((ContentViewHolder) holder).bind(items.get(position).getData());
        else if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(items.get(position).getHeaderTitle());
        }

    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public boolean isHeader(int itemPosition) {
        return items.get(itemPosition).getViewType() == GroupedList.ViewType.HEADER;
    }

    @Override
    public boolean shouldOffset() {
        return false;
    }

    @Override
    public boolean hasHeader() {
        return false;
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return headerLayoutRes;
    }

    @Override
    public int getHeaderPositionForItem(int itemPosition) {
        int headerPosition = 0;
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);
        return headerPosition;
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        // binding our header data here
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public ContentViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setVariable(BR.item, item);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public HeaderViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String item) {
            binding.setVariable(BR.item, item);
        }
    }
}
