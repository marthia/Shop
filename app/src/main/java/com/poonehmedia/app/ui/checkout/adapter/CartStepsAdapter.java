package com.poonehmedia.app.ui.checkout.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.CartListItem;
import com.poonehmedia.app.databinding.BottomBarCartBinding;
import com.poonehmedia.app.databinding.CartStepsAddressBinding;
import com.poonehmedia.app.databinding.CartStepsMainViewHolderBinding;
import com.poonehmedia.app.databinding.ListItemEmptyBinding;
import com.poonehmedia.app.modules.navigation.NavigationHelper;
import com.poonehmedia.app.ui.adapter.EmptyViewHolder;
import com.poonehmedia.app.ui.checkout.CartStepsFragmentDirections;
import com.poonehmedia.app.ui.checkout.CheckoutViewModel;
import com.poonehmedia.app.ui.interfaces.OnParamValueChanged;
import com.poonehmedia.app.ui.product.ProductCartViewModel;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.DividerDecor;

import org.acra.ACRA;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CartStepsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final LifecycleOwner lifecycleOwner;
    private final FragmentActivity activity;
    private final int EMPTY = -1;
    private final int CART = 0;
    private final int SHIPPING = 1;
    private final int PAYMENT = 2;
    private final int ADDRESS = 3;
    private final int STATUS = 4;
    private final int COUPON = 5;
    private final ProductCartViewModel productCartViewModel;
    private final CheckoutViewModel viewModel;
    private final OnParamValueChanged notifyItemsCallback;
    private final DataController dataController;
    private final NavigationHelper navigator;
    private List<CartListItem> products = new ArrayList<>();
    private JsonArray items;
    private ViewStub stub;
    private View bottomBar;
    private BottomBarCartBinding bottomBarBinding;
    private JsonObject nextButtonInfo;
    private boolean isBottomBarInflated = false;
    private Consumer<String> navigationCallback;

    public CartStepsAdapter(NavigationHelper navigationHelper,
                            LifecycleOwner lifecycleOwner,
                            FragmentActivity activity,
                            DataController dataController,
                            ProductCartViewModel productCartViewModel,
                            CheckoutViewModel viewModel,
                            OnParamValueChanged notifyItemsCallback
    ) {
        this.navigator = navigationHelper;
        this.lifecycleOwner = lifecycleOwner;
        this.activity = activity;
        this.dataController = dataController;
        this.productCartViewModel = productCartViewModel;
        this.viewModel = viewModel;
        this.notifyItemsCallback = notifyItemsCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        @NotNull CartStepsMainViewHolderBinding cartBinding = CartStepsMainViewHolderBinding.inflate(layoutInflater, parent, false);
        @NotNull CartStepsMainViewHolderBinding shippingBinding = CartStepsMainViewHolderBinding.inflate(layoutInflater, parent, false);
        @NotNull CartStepsMainViewHolderBinding paymentBinding = CartStepsMainViewHolderBinding.inflate(layoutInflater, parent, false);
        @NotNull CartStepsAddressBinding addressBinding = CartStepsAddressBinding.inflate(layoutInflater, parent, false);
        @NotNull CartStepsMainViewHolderBinding statusBinding = CartStepsMainViewHolderBinding.inflate(layoutInflater, parent, false);
        @NotNull CartStepsMainViewHolderBinding couponBinding = CartStepsMainViewHolderBinding.inflate(layoutInflater, parent, false);
        @NotNull ListItemEmptyBinding empty = ListItemEmptyBinding.inflate(layoutInflater, parent, false);

        if (!isBottomBarInflated)
            intiBottomBar();

        if (viewType == CART) {
            initCartProducts(cartBinding);
            return new CartListViewHolder(cartBinding);
        } else if (viewType == SHIPPING) return new ShippingViewHolder(shippingBinding);
        else if (viewType == PAYMENT) return new PaymentViewHolder(paymentBinding);
        else if (viewType == ADDRESS) return new AddressViewHolder(addressBinding);
        else if (viewType == STATUS) return new StatusViewHolder(statusBinding);
        else if (viewType == COUPON) return new CouponViewHolder(couponBinding);
        else if (viewType == EMPTY) return new EmptyViewHolder(empty);
        else throw new RuntimeException("expected a defined view type");
    }

    private void initCartProducts(CartStepsMainViewHolderBinding binding) {
        CartListAdapter cartListAdapter = new CartListAdapter(
                lifecycleOwner,
                productCartViewModel,
                dataController,
                (position, quantity, updateLink, params) -> { // on Update quantity
                    viewModel.setNextButtonStatus(false);
                    productCartViewModel.updateCartCart(updateLink, quantity, params);

                }, (clickedItem, position) -> navigationCallback.accept((String) clickedItem));

        setupRecycler(binding, cartListAdapter, binding.getRoot().getContext());
        CheckoutMessagesAdapter cartProductsMessagesAdapter = new CheckoutMessagesAdapter();
        binding.messages.setAdapter(cartProductsMessagesAdapter);

        productCartViewModel.getUpdateCartResponse().observe(lifecycleOwner, jsonObject -> {
            binding.recycler.post(() -> {
                viewModel.setNextButtonStatus(false);
                replaceCurrentListCartItem(items, jsonObject);
                notifyDataSetChanged();
                notifyItemsCallback.onChanged(items, 0);
            });
        });
    }

    public void intiBottomBar() {
        stub = activity.findViewById(R.id.cart_layout_stub);

        if (stub != null) {
            stub.setLayoutResource(R.layout.bottom_bar_cart);
            stub.setOnInflateListener((stub1, inflated) -> {
                setUpBottomBar(inflated);
            });
            stub.inflate();
            isBottomBarInflated = true;

        } else {
            bottomBar = activity.findViewById(R.id.bottom_bar_cart);
            bottomBar.setVisibility(View.VISIBLE);
            setUpBottomBar(bottomBar);
        }
    }

    private void setUpBottomBar(View bottomBar) {

        // prevent rebinding
        if (bottomBarBinding != null) return;

        bottomBarBinding = DataBindingUtil.bind(bottomBar);

        if (bottomBarBinding != null) {
            try {
                String text = nextButtonInfo.get("text").getAsString();
                if (text != null)
                    bottomBarBinding.continueBtn.setText(text);
            } catch (Exception e) {
                Log.e("bottomBar", "Couldn't bind");
            }

            bottomBarBinding.continueBtn.setOnClickListener(v -> {

                if (nextButtonInfo.get("action").getAsJsonObject().get("name").getAsString().equals("ShopCheckoutEnd")) {
                    navigator.navigateAndClearBackStack(activity,
                            CartStepsFragmentDirections.actionGoToCheckoutEnd(nextButtonInfo.get("link").getAsString())
                    );
                } else {
                    // save current cart step to come back to after login
                    if (nextButtonInfo.has("return"))
                        viewModel.saveReturn(nextButtonInfo.get("return").getAsJsonObject());

                    navigationCallback.accept(nextButtonInfo.get("link").getAsString());
                }
            });
        }
    }

    private void replaceCurrentListCartItem(JsonArray items, JsonObject jsonObject) {
        try {
            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                if (item.get("type").getAsString().equals("cart")) {
                    items.remove(i);

                    if (jsonObject != null && JsonHelper.has(jsonObject, "type")) {
                        items.add(jsonObject);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("replaceList", e.getMessage());
        }
    }

    private void setupRecycler(CartStepsMainViewHolderBinding binding, Object adapter, Context context) {
        binding.recycler.setAdapter((RecyclerView.Adapter) adapter);

        binding.recycler.addItemDecoration(new DividerDecor(context, 16, DividerDecor.VERTICAL)
        );
    }

    public void clear() {
        items = null;
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        JsonObject item = items.get(position).getAsJsonObject();

        if (holder instanceof CartListViewHolder) {
            ((CartListViewHolder) holder).bind(item);
        } else if (holder instanceof AddressViewHolder) {
            ((AddressViewHolder) holder).bind(item);
        } else if (holder instanceof PaymentViewHolder) {
            ((PaymentViewHolder) holder).bind(item);
        } else if (holder instanceof ShippingViewHolder) {
            ((ShippingViewHolder) holder).bind(item);
        } else if (holder instanceof StatusViewHolder) {
            ((StatusViewHolder) holder).bind(item);
        } else if (holder instanceof CouponViewHolder) {
            ((CouponViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public int getItemViewType(int position) {

        JsonObject obj = items.get(position).getAsJsonObject();
        if (!JsonHelper.has(obj, "type")) return EMPTY;

        String widgetType = obj.get("type").getAsString();

        switch (widgetType) {
            case "shipping":
                return SHIPPING;
            case "payment":
                return PAYMENT;
            case "address":
                return ADDRESS;
            case "cart":
                return CART;
            case "status":
                return STATUS;
            case "coupon":
                return COUPON;
        }

        return EMPTY;
    }

    public void submitList(JsonArray content) {

        items = content;
        notifyDataSetChanged();
    }

    public void submitNextButton(JsonObject info) {

        this.nextButtonInfo = info;
    }

    public void hideBottomBar() {
        if (stub != null)
            stub.setVisibility(View.GONE);
        else if (bottomBar != null) bottomBar.setVisibility(View.GONE);
    }

    public void handleStubStatus(Boolean aBoolean) {
        if (stub != null) {
            View viewById = stub.findViewById(R.id.continue_btn);
            if (viewById != null) viewById.setEnabled(aBoolean);

        } else if (bottomBar != null) {
            View viewById = bottomBar.findViewById(R.id.continue_btn);
            if (viewById != null)
                viewById.setEnabled(aBoolean);

        }
    }

    public void subscribeNavigation(Consumer<String> navigation) {
        this.navigationCallback = navigation;
    }

    private class CartListViewHolder extends RecyclerView.ViewHolder {

        private final CartStepsMainViewHolderBinding binding;

        public CartListViewHolder(@NotNull CartStepsMainViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            try {
                JsonArray content = item.get("content").getAsJsonArray();
                products = bindCartArrayToCartListItems(content);
            } catch (Exception e) {
                Log.e("checkout", e.getMessage());
            }

            if (products.size() == 0) showEmptyListImage(binding);
            else {
                ((CartListAdapter) binding.recycler.getAdapter()).submitList(products);
                if (JsonHelper.has(item, "title") && !item.get("title").getAsString().isEmpty()) {
                    binding.title.setVisibility(View.VISIBLE);
                    binding.title.setText(item.get("title").getAsString());
                }
            }

            if (JsonHelper.has(item, "messages") && JsonHelper.isNotEmptyNorNull(item.get("messages")))
                ((CheckoutMessagesAdapter) binding.messages.getAdapter()).submitList(item.get("messages").getAsJsonArray());

//            binding.executePendingBindings();
        }

        private List<CartListItem> bindCartArrayToCartListItems(JsonArray products) {
            List<CartListItem> result = new ArrayList<>();
            try {

                for (int i = 0; i < products.size(); i++) {
                    JsonObject data = products.get(i).getAsJsonObject();
                    CartListItem item = new CartListItem();

                    item.setData(data);
                    item.setUpdating(false);

                    result.add(item);
                }
                return result;

            } catch (Exception e) {
                Log.e("bindCart", e.getMessage());
                ACRA.getErrorReporter().handleException(new CrashReportException("Could not bind Cart item to CartLisItem in (CartStepsAdapter). data : " + products.toString(), e));
                return result;
            }
        }

        private void showEmptyListImage(CartStepsMainViewHolderBinding binding) {
            binding.emptyText.setVisibility(View.VISIBLE);
            binding.emptyImage.setVisibility(View.VISIBLE);
        }
    }

    private class PaymentViewHolder extends RecyclerView.ViewHolder {

        private final CartStepsMainViewHolderBinding binding;

        public PaymentViewHolder(@NotNull CartStepsMainViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            CartPaymentAdapter cartPaymentAdapter = new CartPaymentAdapter((clickedItem, position) -> {
                viewModel.postPayment(
                        ((JsonObject) clickedItem).get("payment_actions").getAsJsonObject(),
                        ((JsonObject) clickedItem).get("id").getAsString()
                );
            });

            setupRecycler(binding, cartPaymentAdapter, itemView.getContext());
            CheckoutMessagesAdapter cartPaymentMessagesAdapter = new CheckoutMessagesAdapter();
            binding.messages.setAdapter(cartPaymentMessagesAdapter);

            cartPaymentAdapter.submitList(item.get("content").getAsJsonArray());
            if (JsonHelper.has(item, "title") && !item.get("title").getAsString().isEmpty()) {
                binding.title.setVisibility(View.VISIBLE);
                binding.title.setText(item.get("title").getAsString());
            }

            if (JsonHelper.has(item, "messages") && JsonHelper.isNotEmptyNorNull(item.get("messages")))
                cartPaymentMessagesAdapter.submitList(item.get("messages").getAsJsonArray());

//            binding.executePendingBindings();
        }
    }

    private class AddressViewHolder extends RecyclerView.ViewHolder {

        private final CartStepsAddressBinding binding;

        public AddressViewHolder(@NotNull CartStepsAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {

            CartAddressAdapter shippingAddressAdapter = new CartAddressAdapter((clickedItem, position) -> {
                viewModel.postAddress(
                        ((JsonObject) clickedItem).get("address_actions").getAsJsonObject(),
                        ((JsonObject) clickedItem).get("address_id").getAsString()
                );
            }, (clickedItem, key) -> {
                navigationCallback.accept(
                        ((JsonObject) clickedItem).get("address_actions").getAsJsonObject()
                                .get("edit_address").getAsJsonObject()
                                .get("link").getAsString());
                hideBottomBar();
            }

            );
            binding.shippingAddressRecycler.setAdapter(shippingAddressAdapter);

            binding.shippingAddressRecycler.addItemDecoration(new DividerDecor(itemView.getContext(), 16, DividerDecor.VERTICAL)
            );

            CartAddressAdapter billingAddressAdapter = new CartAddressAdapter((clickedItem, position) -> {
                viewModel.postAddress(
                        ((JsonObject) clickedItem).get("address_actions").getAsJsonObject(),
                        ((JsonObject) clickedItem).get("address_id").getAsString()
                );
            }, (clickedItem, key) -> {
                navigationCallback.accept(
                        ((JsonObject) clickedItem).get("address_actions").getAsJsonObject()
                                .get("edit_address").getAsJsonObject()
                                .get("link").getAsString());

                hideBottomBar();
            }
            );
            binding.billingAddressRecycler.setAdapter(billingAddressAdapter);
            binding.billingAddressRecycler.addItemDecoration(new DividerDecor(itemView.getContext(), 16, DividerDecor.VERTICAL)
            );

            CheckoutMessagesAdapter cartAddressMessagesAdapter = new CheckoutMessagesAdapter();
            binding.messages.setAdapter(cartAddressMessagesAdapter);

            JsonObject content = item.get("content").getAsJsonObject();

            if (item.get("show_billing").getAsBoolean()) {
                binding.billingAddNewAddress.setOnClickListener(v -> {
                    JsonObject args = item.get("new_billing_address_action").getAsJsonObject();
                    navigationCallback.accept(args.get("link").getAsString());
                    hideBottomBar();
                });
                billingAddressAdapter.submitList(content.get("billing").getAsJsonArray());
                binding.billingTitle.setText(item.get("billing_address_title").getAsString());

            } else binding.billingAddressLayout.setVisibility(View.GONE);

            if (item.get("show_shipping").getAsBoolean()) {
                shippingAddressAdapter.submitList(content.get("shipping").getAsJsonArray());

                binding.shippingTitle.setText(item.get("shipping_address_title").getAsString());

                binding.shippingAddNewAddress.setOnClickListener(v -> {

                    navigationCallback.accept(item.get("new_shipping_address_action").getAsJsonObject().get("link").getAsString());
                    hideBottomBar();
                });

            } else binding.shippingAddressLayout.setVisibility(View.GONE);

            if (JsonHelper.has(item, "title") && !item.get("title").getAsString().isEmpty()) {
                binding.title.setVisibility(View.VISIBLE);
                binding.title.setText(item.get("title").getAsString());
            }

            if (JsonHelper.has(item, "messages") && JsonHelper.isNotEmptyNorNull(item.get("messages")))
                cartAddressMessagesAdapter.submitList(item.get("messages").getAsJsonArray());

//            binding.executePendingBindings();
        }
    }

    private class ShippingViewHolder extends RecyclerView.ViewHolder {

        private final CartStepsMainViewHolderBinding binding;

        public ShippingViewHolder(@NotNull CartStepsMainViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            CartShippingAdapter cartShippingAdapter = new CartShippingAdapter((clickedItem, position) -> {
                viewModel.postShipping(
                        ((JsonObject) clickedItem).get("shipping_actions").getAsJsonObject(),
                        ((JsonObject) clickedItem).get("id").getAsString()
                );
            });

            setupRecycler(binding, cartShippingAdapter, itemView.getContext());
            CheckoutMessagesAdapter cartShippingMessagesAdapter = new CheckoutMessagesAdapter();
            binding.messages.setAdapter(cartShippingMessagesAdapter);


            cartShippingAdapter.submitList(item.get("content").getAsJsonArray());
            if (JsonHelper.has(item, "title") && !item.get("title").getAsString().isEmpty()) {
                binding.title.setVisibility(View.VISIBLE);
                binding.title.setText(item.get("title").getAsString());
            }

            if (JsonHelper.has(item, "messages") && JsonHelper.isNotEmptyNorNull(item.get("messages")))
                cartShippingMessagesAdapter.submitList(item.get("messages").getAsJsonArray());

//            binding.executePendingBindings();
        }
    }

    private class StatusViewHolder extends RecyclerView.ViewHolder {

        private final CartStepsMainViewHolderBinding binding;

        public StatusViewHolder(@NotNull CartStepsMainViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {

            CartStatusAdapter cartStatusAdapter = new CartStatusAdapter();
            setupRecycler(binding, cartStatusAdapter, itemView.getContext());
            CheckoutMessagesAdapter cartStatusMessagesAdapter = new CheckoutMessagesAdapter();
            binding.messages.setAdapter(cartStatusMessagesAdapter);

            cartStatusAdapter.submitList(item.get("content").getAsJsonArray());
            if (JsonHelper.has(item, "title") && !item.get("title").getAsString().isEmpty()) {
                binding.title.setVisibility(View.VISIBLE);
                binding.title.setText(item.get("title").getAsString());
            }

            if (JsonHelper.has(item, "messages") && JsonHelper.isNotEmptyNorNull(item.get("messages")))
                cartStatusMessagesAdapter.submitList(item.get("messages").getAsJsonArray());

//            binding.executePendingBindings();
        }
    }

    private class CouponViewHolder extends RecyclerView.ViewHolder {

        private final CartStepsMainViewHolderBinding binding;

        public CouponViewHolder(@NonNull CartStepsMainViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(JsonObject item) {

            CartCouponAdapter cartCouponAdapter = new CartCouponAdapter((clickedItem, isEdit) -> {
                viewModel.postCoupon(clickedItem.get("actions").getAsJsonObject(), isEdit);
            });

            binding.recycler.setAdapter(cartCouponAdapter);

            CheckoutMessagesAdapter cartCouponMessagesAdapter = new CheckoutMessagesAdapter();
            binding.messages.setAdapter(cartCouponMessagesAdapter);

            cartCouponAdapter.submitList(item.get("content").getAsJsonArray());
            if (JsonHelper.has(item, "title") && !item.get("title").getAsString().isEmpty()) {
                binding.title.setVisibility(View.VISIBLE);
                binding.title.setText(item.get("title").getAsString());
            }

            if (JsonHelper.has(item, "messages") && JsonHelper.isNotEmptyNorNull(item.get("messages")))
                cartCouponMessagesAdapter.submitList(item.get("messages").getAsJsonArray());
        }
    }
}
