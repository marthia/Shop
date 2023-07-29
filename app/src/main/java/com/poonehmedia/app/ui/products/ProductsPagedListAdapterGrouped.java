package com.poonehmedia.app.ui.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.GroupedList;
import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.databinding.ListItemEmptyBinding;
import com.poonehmedia.app.databinding.ListItemProductsBinding;
import com.poonehmedia.app.databinding.ListItemProductsCategoriesHeaderBinding;
import com.poonehmedia.app.databinding.ListItemProductsHeaderRoundedBinding;
import com.poonehmedia.app.ui.adapter.EmptyViewHolder;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.ItemSpaceDecoration;
import com.poonehmedia.app.util.ui.RecyclerItemTouchHelper;
import com.poonehmedia.app.util.ui.StickyHeaderRecyclerItemDecoration;

import java.util.List;

import javax.inject.Inject;

import static com.poonehmedia.app.util.base.JsonHelper.isNotEmptyNorNull;

public class ProductsPagedListAdapterGrouped extends PagingDataAdapter<GroupedList, RecyclerView.ViewHolder> implements StickyHeaderRecyclerItemDecoration.StickyHeaderInterface {

    private final int HEADER = 0;
    private final int CONTENT = 1;
    private final int CATEGORY = 2;
    private final int EMPTY = 3;
    @Inject
    public DataController dataController;
    private GenericListAdapterImp<JsonElement> categoriesAdapter;
    private OnParamValueChanged onClickListener;
    private ClickProvider onCategoryClick;
    private boolean hasHeader = false;

    @Inject
    public ProductsPagedListAdapterGrouped() {
        super(new GroupedList.MyDiffUtil());
    }

    public void subscribeCallbacks(OnParamValueChanged onClickListener, ClickProvider onCategoryClick) {
        this.onClickListener = onClickListener;
        this.onCategoryClick = onCategoryClick;
    }

    @Override
    public final int getItemViewType(int position) {
        GroupedList.ViewType type = getItem(position).getViewType();

        if (type == GroupedList.ViewType.HEADER) {
            hasHeader = true;
            return HEADER;

        } else if (type == GroupedList.ViewType.CATEGORY) {

            JsonObject category = getItem(position).getCategory();
            if (isNotEmptyNorNull(category) && isNotEmptyNorNull(category.get("content")))
                return CATEGORY;

            return EMPTY;

        } else if (type == GroupedList.ViewType.CONTENT)
            return CONTENT;

        else return EMPTY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == HEADER)
            return new HeaderViewHolder(ListItemProductsHeaderRoundedBinding.inflate(layoutInflater, parent, false));
        else if (viewType == CONTENT)
            return new ContentViewHolder(ListItemProductsBinding.inflate(layoutInflater, parent, false));

        else if (viewType == CATEGORY) {
            ListItemProductsCategoriesHeaderBinding b = ListItemProductsCategoriesHeaderBinding
                    .inflate(layoutInflater, parent, false);
            intCategory(b, parent.getContext());
            return new CategoriesViewHolder(b);
        } else if (viewType == EMPTY) {
            ListItemEmptyBinding empty = ListItemEmptyBinding.inflate(layoutInflater, parent, false);
            return new EmptyViewHolder(empty);

        } else throw new RuntimeException("expected a defined view type");
    }

    private void intCategory(ListItemProductsCategoriesHeaderBinding binding, Context context) {
        categoriesAdapter = new GenericListAdapterImp<>();
        categoriesAdapter.setLayoutRes(R.layout.list_item_products_categories);
        categoriesAdapter.subscribeCallbacks(onCategoryClick);

        ItemSpaceDecoration itemSpaceDecoration = new ItemSpaceDecoration(
                (int) AndroidUtils.getPixels(8, context)
        );
        binding.rvCategories.addItemDecoration(itemSpaceDecoration);
        binding.rvCategories.setAdapter(categoriesAdapter);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContentViewHolder)
            ((ContentViewHolder) holder).bind(getItem(position).getData());
        else if (holder instanceof HeaderViewHolder)
            ((HeaderViewHolder) holder).bind(getItem(position).getHeaderTitle());
        else if (holder instanceof CategoriesViewHolder) {
            JsonObject category = getItem(position).getCategory();
            if (isNotEmptyNorNull(category) && isNotEmptyNorNull(category.get("content")))
                ((CategoriesViewHolder) holder).bind(category);
        }
    }

    @Override
    public boolean isHeader(int itemPosition) {
        return getItem(itemPosition).getViewType() == GroupedList.ViewType.HEADER;
    }

    @Override
    public boolean shouldOffset() {
        // category is always drawn in position 0 if provided
        return getItemCount() > 0 && isNotEmptyNorNull(getItem(0).getCategory()) && isNotEmptyNorNull(getItem(0).getCategory().get("content"));
    }

    @Override
    public boolean hasHeader() {
        return hasHeader;
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return R.layout.list_item_products_header;
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
        TextView headerTitle = header.findViewById(R.id.text);
        headerTitle.setText(getItem(headerPosition).getHeaderTitle());
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
        private final ListItemProductsBinding binding;

        public ContentViewHolder(@NonNull ListItemProductsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public View getForeground() {
            return itemView.findViewById(R.id.foreground);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
            Toast.makeText(itemView.getContext(), "آیتم به مقایسه اضافه شد.", Toast.LENGTH_SHORT).show();
        }

        public void bind(JsonObject item) {
            binding.setItem(item);
            List<Price> prices = dataController.extractPrice(item, null);
            if (prices.size() > 0) binding.setPriceItem(prices.get(0));

            itemView.setOnClickListener(v ->
                    onClickListener.onChanged(
                            getItem(getAbsoluteAdapterPosition()).getData(),
                            getAbsoluteAdapterPosition())
            );

            if (JsonHelper.has(item, "badges") && JsonHelper.isNotEmptyNorNull(item.get("badges"))) {
                GenericListAdapterImp badgesAdapter = new GenericListAdapterImp();
                badgesAdapter.setLayoutRes(R.layout.list_item_badge);
                binding.badges.setVisibility(View.VISIBLE);
                binding.badges.setAdapter(badgesAdapter);
                badgesAdapter.submitList(item.get("badges").getAsJsonArray());

            } else binding.badges.setVisibility(View.GONE);

            // binding.executePendingBindings();
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ListItemProductsHeaderRoundedBinding binding;

        public HeaderViewHolder(@NonNull ListItemProductsHeaderRoundedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String item) {
            binding.setItem(item);
        }
    }

    private class CategoriesViewHolder extends RecyclerView.ViewHolder {

        public CategoriesViewHolder(@NonNull ListItemProductsCategoriesHeaderBinding binding) {
            super(binding.getRoot());
        }

        public void bind(JsonObject item) {
            if (item.get("content").getAsJsonArray().size() > 0)
                categoriesAdapter.submitList(item.get("content").getAsJsonArray());

            // binding.executePendingBindings();
        }
    }
}
