package com.poonehmedia.app.ui.modules;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.TripleSnapPage;
import com.poonehmedia.app.databinding.ListItemPageTripleSnapBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.interfaces.ClickProvider;

import java.util.List;

public class TripleSnapPageAdapter extends RecyclerView.Adapter<TripleSnapPageAdapter.ViewHolder> {

    private List<TripleSnapPage> items;
    private ClickProvider callback;

    public void subscribeCallbacks(ClickProvider callback) {
        this.callback = callback;
    }

    public void submitList(List<TripleSnapPage> items) {
        this.items = items;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ListItemPageTripleSnapBinding binding = ListItemPageTripleSnapBinding.inflate(layoutInflater, parent, false);

        init(binding);
        return new ViewHolder(binding);
    }

    private void init(ListItemPageTripleSnapBinding binding) {
        GenericListAdapterImp innerRecycler = new GenericListAdapterImp();

        innerRecycler.setLayoutRes(R.layout.list_item_module_triple_snap_products);
        innerRecycler.subscribeCallbacks(callback);

        binding.recycler.setNestedScrollingEnabled(false);
        binding.recycler.setAdapter(innerRecycler);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripleSnapPage item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final ListItemPageTripleSnapBinding binding;

        public ViewHolder(@NonNull ListItemPageTripleSnapBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TripleSnapPage item) {

            GenericListAdapterImp adapter = (GenericListAdapterImp) binding.recycler.getAdapter();
            adapter.submitList(item.getData());
            // binding.executePendingBindings();
        }
    }

}