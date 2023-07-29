package com.poonehmedia.app.ui.address;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.ListItemAddressBinding;
import com.poonehmedia.app.ui.interfaces.IPopupMenuClick;

import org.jetbrains.annotations.NotNull;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    private IPopupMenuClick onEdit;
    private IPopupMenuClick onDelete;
    private JsonArray items;

    public void subscribeEditCallback(IPopupMenuClick click) {
        this.onEdit = click;
    }

    public void subscribeDeleteCallback(IPopupMenuClick onPopupMenu) {
        this.onDelete = onPopupMenu;
    }

    public void submitList(JsonArray array) {
        this.items = array;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListItemAddressBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = items.get(position).getAsJsonObject();
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemAddressBinding binding;

        public ViewHolder(@NonNull @NotNull ListItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            binding.setItem(item);

            binding.edit.setOnClickListener(v -> {
                onEdit.onClick(item);
            });

            binding.contextMenu.setOnClickListener(v -> {
                onPopupMenu(v, item);
            });
//            binding.executePendingBindings();
        }


        private void onPopupMenu(View v, JsonObject item) {

            BottomSheetDialog dialog = new BottomSheetDialog(v.getContext());
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.bottom_sheet_address);
            dialog.findViewById(R.id.delete).setOnClickListener(view -> {
                        onDelete.onClick(item);
                        dialog.dismiss();
                    }

            );
            dialog.show();
        }
    }
}
