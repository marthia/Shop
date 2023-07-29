package com.poonehmedia.app.ui.checkout.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.components.fontAwesome.FontDrawable;
import com.poonehmedia.app.data.model.CartListItem;
import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.databinding.CartStepsListItemCartBinding;
import com.poonehmedia.app.databinding.ListItemCartSummaryBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.poonehmedia.app.ui.interfaces.UpdateCartCallback;
import com.poonehmedia.app.ui.product.ProductCartViewModel;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;

import org.acra.ACRA;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CartListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ProductCartViewModel viewModel;
    private final UpdateCartCallback updateCartCallback;
    private final ClickProvider callback;
    private final int PRODUCTS = 0;
    private final LifecycleOwner lifecycleOwner;
    private final DataController dataController;
    private List<CartListItem> items;


    public CartListAdapter(LifecycleOwner lifecycleOwner, ProductCartViewModel viewModel,
                           DataController dataController,
                           UpdateCartCallback updateCartCallback,
                           ClickProvider navigateCallback
    ) {
        this.lifecycleOwner = lifecycleOwner;
        this.viewModel = viewModel;
        this.dataController = dataController;
        this.updateCartCallback = updateCartCallback;
        this.callback = navigateCallback;
    }

    public void submitList(List<CartListItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        CartStepsListItemCartBinding binding = CartStepsListItemCartBinding.inflate(layoutInflater, parent, false);
        @NotNull ListItemCartSummaryBinding summary = ListItemCartSummaryBinding.inflate(layoutInflater, parent, false);

        if (viewType == PRODUCTS) {
            return new ViewHolder(binding);
        } else {
            return new SummaryViewHolder(summary);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CartListItem item = items.get(position);

        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).bind(item);
        } else if (holder instanceof SummaryViewHolder) {
            ((SummaryViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        boolean isProducts = items.get(position).getData().has("image");
        if (isProducts)
            return PRODUCTS;
        else return 1;
    }

    public int getVisibility(boolean a) {
        return a ? View.VISIBLE : View.GONE;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final CartStepsListItemCartBinding binding;

        public ViewHolder(@NonNull CartStepsListItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartListItem item) {
            binding.setViewModel(viewModel);
            binding.setLifecycleOwner(lifecycleOwner);
            binding.setItem(item.getData());

            int quantity = item.getData().get("cart_product_quantity").getAsInt();
            String minusIcon = "&#xf068;";
            String trashIcon = "&#xf2ed;";
            if (quantity == 1) viewModel.setQuantityLeftIcon(
                    getDrawable(
                            trashIcon,
                            22,
                            itemView.getContext().getResources().getColor(R.color.red_color_badge),
                            false
                    )
            );
            else viewModel.setQuantityLeftIcon(
                    getDrawable(
                            minusIcon,
                            22,
                            Color.parseColor("#80000000"),
                            false
                    )
            );

            List<Price> pricesUnit = dataController.extractPrice(item.getData(), "unit_price");
            if (pricesUnit.size() > 0) binding.setUnitPriceItem(pricesUnit.get(0));

            List<Price> prices = dataController.extractPrice(item.getData(), "total_price");
            if (prices.size() > 0) binding.setPriceItem(prices.get(0));


            String updateCartLink = extractCartProductAction(item.getData());
            binding.setSectionVisibility(!updateCartLink.isEmpty());

            if (!updateCartLink.isEmpty()) {
                JsonObject queryParams = extractQueryParams(item.getData());

                binding.additionButton.setOnClickListener(v -> {
                    updateCartCallback.onAction(
                            getAbsoluteAdapterPosition(),
                            quantity + 1,
                            updateCartLink,
                            queryParams
                    );
                    item.setUpdating(true);
                    notifyItemChanged(getAbsoluteAdapterPosition());
                });

                binding.subtractButton.setOnClickListener(v -> {
                    updateCartCallback.onAction(
                            getAbsoluteAdapterPosition(),
                            quantity - 1,
                            updateCartLink,
                            queryParams
                    );
                    item.setUpdating(true);
                    notifyItemChanged(getAbsoluteAdapterPosition());
                });

                binding.setOnContextClick((view, item1) -> {
                    BottomSheetDialog dialog = new BottomSheetDialog(view.getContext());
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.bottom_sheet_cart_list_item);
                    dialog.findViewById(R.id.delete).setOnClickListener(v -> {
                                updateCartCallback.onAction(
                                        getAbsoluteAdapterPosition(),
                                        0,
                                        updateCartLink,
                                        queryParams
                                );
                                dialog.dismiss();
                            }
                    );
                    dialog.show();
                });
            }

            bindLoading(item.isUpdating());
            binding.image.setOnClickListener(v ->
                    callback.onClick(extractActionLink(item.getData()), getAbsoluteAdapterPosition()));
//            binding.executePendingBindings();
        }

        protected FontDrawable getDrawable(String icon, int iconSize, int iconColor, boolean isSolid) {
            FontDrawable drawable = new FontDrawable(itemView.getContext(), icon, isSolid, false);

            drawable.setTextColor(iconColor);
            drawable.setTextSize(iconSize);

            return drawable;
        }

        public String extractCartProductAction(JsonObject item) {
            try {
                if (!JsonHelper.has(item, "product_actions"))
                    return "";

                JsonObject productActions = item.get("product_actions").getAsJsonObject();
                if (!JsonHelper.has(productActions, "update_quantity"))
                    return "";

                JsonObject updateQuantity = productActions.get("update_quantity").getAsJsonObject();
                if (!JsonHelper.has(updateQuantity, "link"))
                    return "";

                return updateQuantity.get("link").getAsString();

            } catch (Exception e) {
                Log.e("cartProductAction", e.getMessage());
                ACRA.getErrorReporter().handleException(new CrashReportException("extractCartProductAction", e));
                return "";
            }
        }

        private JsonObject extractQueryParams(JsonObject item) {
            try {
                return item.get("product_actions").getAsJsonObject().get("update_quantity").getAsJsonObject().get("params").getAsJsonObject();

            } catch (Exception e) {
                Log.e("cartProductAction", e.getMessage());
                ACRA.getErrorReporter().handleException(new CrashReportException("Error while Updating cart extractQueryParams in (CartListAdapter).", e));
                return null;
            }
        }

        private String extractActionLink(JsonObject item) {
            try {
                return item.get("product_actions").getAsJsonObject().get("product_link").getAsJsonObject().get("link").getAsString();

            } catch (Exception e) {
                Log.e("cartActionLink", e.getMessage());
                return "";
            }
        }

        private void bindLoading(boolean updating) {


            binding.additionButton.setVisibility(getVisibility(!updating));
            binding.subtractButton.setVisibility(getVisibility(!updating));
            binding.quantityTxt.setVisibility(getVisibility(!updating));
            binding.progress.setVisibility(getVisibility(updating));
        }
    }

    protected class SummaryViewHolder extends RecyclerView.ViewHolder {

        private final ListItemCartSummaryBinding binding;

        public SummaryViewHolder(@NonNull ListItemCartSummaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartListItem item) {

            JsonArray array = extractSummaryItems(item);
            JsonObject lastItem = array.get(array.size() - 1).getAsJsonObject();
            array.remove(array.size() - 1);
            GenericListAdapterImp summaryAdapter = new GenericListAdapterImp();
            summaryAdapter.setLayoutRes(R.layout.list_item_summary_price);
            binding.listView.setAdapter(summaryAdapter);

            summaryAdapter.submitList(array);

            binding.title.setText(lastItem.get("title").getAsString());
            binding.amount.setText(lastItem.get("amount").getAsString());

//            binding.executePendingBindings();
        }

        private JsonArray extractSummaryItems(CartListItem item) {
            JsonArray array = new JsonArray();
            for (String key : item.getData().keySet()) {
                if (item.getData().get(key).isJsonObject()) {
                    JsonObject obj = item.getData().get(key).getAsJsonObject();

                    if (obj.has("amount")) {
                        array.add(obj);
                    }
                }
            }
            return array;
        }

    }

}