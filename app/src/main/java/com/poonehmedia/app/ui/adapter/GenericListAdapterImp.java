package com.poonehmedia.app.ui.adapter;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.DividerData;
import com.poonehmedia.app.ui.interfaces.ClickProvider;

import java.lang.reflect.InvocationTargetException;

public class GenericListAdapterImp<T> extends GenericListAdapter {
    private int layoutRes;
    private ClickProvider listener;
    private boolean provideSelection = false;
    private Iterable<T> list;
    private int selectedPos = -1;
    private int alternateBackgroundColor = -1;
    private boolean isFresh = true;

    public void setLayoutRes(int layoutRes) {
        this.layoutRes = layoutRes;
    }

    public void subscribeCallbacks(ClickProvider listener) {
        this.listener = listener;
    }

    public void hasSelection(boolean provideSelection) {
        this.provideSelection = provideSelection;
    }

    public void setAlternateBackgroundColor(int color) {
        this.alternateBackgroundColor = color;
    }

    public Iterable<T> getList() {
        return list;
    }

    public void submitList(Iterable<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int layoutRes() {
        return layoutRes;
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {

        Object item = getItem(position);

        holder.bind(item, new DividerData(position, getItemCount()));

        handleSelection(holder, position);

        // on demand listener
        if (listener != null) {
            holder.itemView.setOnClickListener(view -> {
                listener.onClick(item, holder.getAbsoluteAdapterPosition());
                selectedPos = holder.getAbsoluteAdapterPosition();
            });
        }
        // alternate background
        if (alternateBackgroundColor != -1) {
            holder.itemView.setBackgroundColor(
                    position % 2 == 0 ? Color.parseColor("#ffffff") : alternateBackgroundColor
            );
        }
    }

    public Object getItem(int position) {
        Object item = null;
        try {
            item = list.getClass().getMethod("get", int.class).invoke(list, position);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return item;
    }

    public void setSelectedItem(int position) {
        this.selectedPos = position;
    }

    private void handleSelection(GenericViewHolder holder, int position) {
        if (provideSelection) {
            holder.handleSelection(selectedPos == position);
        }
    }

    public void submitChange(JsonObject item, int pos) {
        this.selectedPos = pos;
        listener.onClick(item, pos);
    }

    public void setIsFresh(boolean b) {
        isFresh = b;
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        else {
            try {
                return (int) list.getClass().getMethod("size").invoke(list);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public boolean isFresh() {
        return isFresh;
    }
}
