package com.poonehmedia.app.ui.product;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.DynamicMenu;
import com.poonehmedia.app.data.model.GalleryItem;
import com.poonehmedia.app.data.model.ParameterItem;
import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.databinding.FragmentProductWithShimmerBinding;
import com.poonehmedia.app.databinding.FrgamentProductBbWithShimmerBinding;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.ui.adapter.CommentModuleAdapter;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.DividerDecor;
import com.poonehmedia.app.util.ui.ItemSpaceDecoration;
import com.poonehmedia.app.util.ui.UiComponents;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProductFragment extends BaseFragment {

    public static final int MENU_CART_ID = 100;
    public static final int MENU_LIKE_ID = 200;
    @Inject
    public PriceAdapter priceAdapter;
    @Inject
    public DataController dataController;
    @Inject
    public SimilarProductsAdapter similarProductsAdapter;
    private GenericListAdapterImp<JsonElement> productSpecsPreviewAdapter;
    private GenericListAdapterImp<JsonElement> priceHistoryAdapter;
    private GenericListAdapterImp<JsonElement> categoriesAdapter;
    private FragmentProductWithShimmerBinding binding;
    private ProductViewModel viewModel;
    private ProductParameterItemsAdapter paramsAdapter;
    private ViewStub stub;
    private View bottomBar;
    private Menu actionBarMenu;
    private ProductCartViewModel productCartViewModel;
    private FrgamentProductBbWithShimmerBinding bottomBarBinding;
    private GallerySliderAdapter galleryAdapter;
    private String addToCartLink;
    private String selectedVariantId;
    private Boolean isInStock;
    private boolean addToWishListClick = false;
    private Boolean isMinQuantity = false;
    private int badgeCount = 0;
    private JsonObject similarProducts;
    private Integer currentQuantity;
    private Boolean hasAddToWishList = null;
    private CommentModuleAdapter commentsAdapter;
    private boolean shouldRefresh = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productCartViewModel = new ViewModelProvider(this).get(ProductCartViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        shouldRefresh = binding != null;

        LayoutInflater from = LayoutInflater.from(requireActivity());
        binding = FragmentProductWithShimmerBinding.inflate(from, container, false);
        binding.setLifecycleOwner(this);
        setHasOptionsMenu(true);

        binding.main.scrollView2.setVisibility(View.INVISIBLE);
        binding.shimmer.startShimmer();

        checkBottomBarVisibility();
        bottomBarBinding.main.bottomBar.setVisibility(View.INVISIBLE);
        bottomBarBinding.shimmer.setVisibility(View.VISIBLE);
        bottomBarBinding.shimmer.startShimmer();

        binding.main.setViewModel(viewModel);

        init();

        viewModel.resolveData(this::subscribeUi, shouldRefresh);

        return binding.getRoot();
    }

    private void init() {

        binding.main.setOnShowDetailedSpecs(v -> {
            if (viewModel.getAllCfs() != null) {
                navigator.intrinsicNavigate(requireActivity(), ProductFragmentDirections.actionGoDetail(viewModel.getAllCfs().toString()), false);
                hideBottomBar();
            }
        });

        binding.main.setOnNewComment(v -> {
            if (preferenceManager.getUser().equals("")) {
                viewModel.saveCurrentPageAsReturn();
                navigator.navigate(requireActivity(), "index.php?option=com_users&view=login", this::handleDefaultNavigationState);
            } else if (viewModel.getAddCommentObj() != null)
                navigator.intrinsicNavigate(requireActivity(),
                        ProductFragmentDirections.actionAddEdit(""),
                        false);
        });

        binding.main.setOnShowProductDescription(view -> {

            NavigationArgs args = new NavigationArgs(
                    getString(R.string.product_description_label),
                    getLink(), // just in case of reload
                    null,
                    -1,
                    "",
                    viewModel.getRawData()

            );
            String accessingKey = UUID.randomUUID().toString();
            routePersistence.addRoute(accessingKey, args);

            navigator.intrinsicNavigate(
                    requireActivity(),
                    ProductFragmentDirections.actionGoDescription(accessingKey, "true"),
                    false
            );
        });

        binding.main.setOnShowPriceHistory(view -> {
            navigator.intrinsicNavigate(
                    requireActivity(),
                    ProductFragmentDirections.actionGoToPriceHistory(viewModel.getPriceHistory().getValue().toString()),
                    false
            );
        });


        // categories
        categoriesAdapter = new GenericListAdapterImp<>();
        categoriesAdapter.setLayoutRes(R.layout.list_item_products_categories);
        categoriesAdapter.subscribeCallbacks((item, position) -> {

            navigator.navigate(
                    requireActivity(),
                    ((JsonObject) item).get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });

        ItemSpaceDecoration categoriesSpaceDecoration = new ItemSpaceDecoration(
                (int) AndroidUtils.getPixels(8, getContext())
        );
        binding.main.rvCategories.addItemDecoration(categoriesSpaceDecoration);
        binding.main.rvCategories.setAdapter(categoriesAdapter);

        // comments Adapter
        commentsAdapter = new CommentModuleAdapter();
        commentsAdapter.subscribeClick((item, position) -> {
            JsonObject value = viewModel.getCommentsReadMore().getValue();
            if (value != null)
                navigator.navigate(requireActivity(), value.get("link").getAsString(), this::handleDefaultNavigationState);
        });
        ItemSpaceDecoration commentsSpaceDecoration = new ItemSpaceDecoration(getContext(), 8);
        binding.main.rvComments.addItemDecoration(commentsSpaceDecoration);
        binding.main.rvComments.setAdapter(commentsAdapter);


        // item preview specs
        productSpecsPreviewAdapter = new GenericListAdapterImp<>();
        productSpecsPreviewAdapter.setLayoutRes(R.layout.list_item_product_details_preview);
        binding.main.rvProductSpecsPreview.setAdapter(productSpecsPreviewAdapter);


        binding.main.rvSimilarProducts.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.HORIZONTAL));


        similarProductsAdapter.subscribeCallbacks((item, position) -> navigator.navigate(
                requireActivity(),
                ((JsonObject) item).get("link").getAsString(),
                this::handleDefaultNavigationState
        ));
        binding.main.rvSimilarProducts.setAdapter(similarProductsAdapter);
        binding.main.rvSimilarProducts.setItemViewCacheSize(20);


        // item parameters adapter
        paramsAdapter = new ProductParameterItemsAdapter(viewModel, shouldRefresh);
        binding.main.rvParameterItems.setAdapter(paramsAdapter);

        galleryAdapter = new GallerySliderAdapter((item, position) ->
                onImageSlidShowClick((List<GalleryItem>) item, position)
        );
        binding.main.imageView.setSliderAdapter(galleryAdapter);
        binding.main.imageView.setIndicatorAnimation(IndicatorAnimationType.WORM);

        binding.main.priceRecycler.setAdapter(priceAdapter);

        priceHistoryAdapter = new GenericListAdapterImp<>();
        priceHistoryAdapter.setLayoutRes(R.layout.list_item_price_history);
        priceHistoryAdapter.setAlternateBackgroundColor(Color.parseColor("#F8F8F8"));
        binding.main.priceHistory.setAdapter(priceHistoryAdapter);

        binding.main.chart.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
    }

    private void subscribeUi() {

        viewModel.getSelectedVariantId().observe(this, s -> {
            if (s != null)
                selectedVariantId = s;
        });

        if (viewModel.hasCart())
            subscribeCart();
        else {
            hideBottomBar();
        }

        viewModel.getTitle().observe(this, s -> {
            if (s != null)
                binding.main.productTitle.setText(s);
        });

        viewModel.getManufacturer().observe(this, jsonObject -> {
            binding.main.manufacturer.setText(jsonObject.get("text").getAsString());
            binding.main.manufacturer.setOnClickListener(v -> {

                navigator.navigate(
                        requireActivity(),
                        jsonObject.get("link").getAsString(),
                        this::handleDefaultNavigationState
                );
            });
        });

        viewModel.setVoteInfoFromData();

        viewModel.getProductCode().observe(this, s -> {
            if (s != null) binding.main.productCode.setText(s);
        });


        viewModel.getGallery().observe(this, array -> {
            if (array.size() > 0) {
                galleryAdapter.submitList(array);
            }
        });

        viewModel.getTotalPoint().observe(this, s -> {
            if (s.equals("-1"))
                binding.main.totalPointSection.setVisibility(View.GONE);
            else {
                binding.main.totalPointSection.setVisibility(View.VISIBLE);
                binding.main.totalPoint.setText(s);
            }
        });

        viewModel.getSimilarProducts().observe(this, jsonObject -> {
            similarProducts = jsonObject;
            similarProductsAdapter.submitList(similarProducts.get("content").getAsJsonArray());
        });

        viewModel.getCategories().observe(this, category -> {
            categoriesAdapter.submitList(category.get("content").getAsJsonArray());
        });

        viewModel.getProductCfs().observe(this, array -> {
            productSpecsPreviewAdapter.submitList(array);
        });


        ArrayList<ParameterItem> variantsData = viewModel.getVariantsData();
        if (variantsData != null && variantsData.size() != 0) {
            binding.main.parametersDivider.setVisibility(View.VISIBLE);
            paramsAdapter.submitList(variantsData);
        } else {
            binding.main.parametersDivider.setVisibility(View.GONE);
            viewModel.setPriceData();
        }

        viewModel.hasAddToWishList().observe(this, aBoolean -> {
            MenuItem item = actionBarMenu.findItem(MENU_LIKE_ID);
            hasAddToWishList = aBoolean;
            if (item != null)
                item.setVisible(aBoolean);
        });
        viewModel.isFavorite().observe(this, aBoolean -> {
            if (isAdded()) changeHearIcon(aBoolean);
        });

        viewModel.getDescription().observe(this, s -> {
            if (s == null || s.isEmpty()) binding.main.descriptionLayout.setVisibility(View.GONE);
        });

        viewModel.getPrice().observe(this, s -> {
            if (s != null && s.size() > 0) {
                priceAdapter.submitList(s);
                binding.main.pricesSection.setVisibility(s.size() >= 2 ? View.VISIBLE : View.GONE);
                bottomBarBinding.main.setItem(s.get(0));
            }
        });

        viewModel.getAddToWaitList().observe(this, aBoolean -> {
            bottomBarBinding.main.setWaitList(aBoolean);
        });


        viewModel.getPriceHistoryPreview().observe(this, array -> {
            priceHistoryAdapter.submitList(array);
        });

        viewModel.getChartData().observe(this, data -> {
            String htmlContent = dataController.generateHtmlContent(
                    dataController.getWebViewStyles(getContext()),
                    dataController.getWebViewJs(true),
                    data,
                    viewModel.getLanguage()
            );
            binding.main.chart.loadDataWithBaseURL(
                    "",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    ""
            );
        });

        viewModel.getCommentsData().observe(this, comments -> {
            commentsAdapter.submitList(comments);
        });

        binding.shimmer.stopShimmer();
        binding.shimmer.setVisibility(View.GONE);
        binding.main.scrollView2.setVisibility(View.VISIBLE);

        bottomBarBinding.shimmer.stopShimmer();
        bottomBarBinding.shimmer.setVisibility(View.GONE);
        bottomBarBinding.main.bottomBar.setVisibility(View.VISIBLE);


    }

    private void hideBottomBar() {
        if (stub != null)
            stub.setVisibility(View.GONE);
        else bottomBar.setVisibility(View.GONE);
    }

    private void subscribeCart() {
        viewModel.getCartBadgeCount().observe(this, count -> {
            // update the icon badge
            badgeCount = count;
            updateIconBadge();

        });

        viewModel.getAddToCartInfo().observe(this, info -> {
            if (info != null) {
                addToCartLink = info.get("link").getAsString();
            }
        });

        productCartViewModel.getCartInfo().observe(this, jsonObject -> {
            if (jsonObject != null) {
                viewModel.postCartInfo(jsonObject);
            }
        });

        productCartViewModel.getRawData().observe(this, object -> {
            viewModel.setRawData(object);
        });

        viewModel.getCurrentVariantCartNumber().observe(this, integer -> {
            productCartViewModel.updateProductQuantity(integer);
        });


        viewModel.getIsInStock().observe(this, aBoolean -> {
            bottomBarBinding.main.setInStock(aBoolean);
            isInStock = aBoolean;
            if (currentQuantity != null)
                updateVisibility(isInStock, currentQuantity);
        });

        productCartViewModel.getProductQuantity().observe(this, quantity -> {
            currentQuantity = quantity;
            if (isInStock) {
                updateVisibility(true, quantity);
            }
        });

        productCartViewModel.isMaxQuantity(viewModel.getData().getValue().getAsJsonObject())
                .observe(this, aBoolean -> {
                    if (aBoolean) {
                        bottomBarBinding.main.additionButton.setEnabled(false);
                        bottomBarBinding.main.maxTxt.setVisibility(View.VISIBLE);
                    } else {
                        bottomBarBinding.main.additionButton.setEnabled(true);
                        bottomBarBinding.main.maxTxt.setVisibility(View.GONE);
                    }
                });

        productCartViewModel.isMinQuantity(viewModel.getData().getValue().getAsJsonObject())
                .observe(this, aBoolean -> {
                    isMinQuantity = aBoolean;
                });


        productCartViewModel.getCartCount().observe(this, count -> {
            if (count > 0)
                viewModel.updateBadgeCount(count);
        });
    }

    private void updateVisibility(Boolean isInStock, Integer quantity) {
        if (viewModel.isCartEnabled()) {
            bottomBarBinding.main.setIsCartEnabled(true);
            bottomBarBinding.main.price.setVisibility(View.VISIBLE);
            if (isInStock) {
                if (quantity == 0)
                    viewModel.setAddToCartButtonVisibility(true);
                else if (quantity == 1) {
                    viewModel.setAddToCartButtonVisibility(false);
                    String trashIcon = "&#xf2ed;";
                    productCartViewModel.setQuantityLeftIcon(
                            getDrawable(
                                    trashIcon,
                                    22,
                                    getResources().getColor(R.color.red_color_badge),
                                    false
                            )
                    );
                } else {
                    viewModel.setAddToCartButtonVisibility(false);
                    String minusIcon = "&#xf068;";
                    productCartViewModel.setQuantityLeftIcon(
                            getDrawable(
                                    minusIcon,
                                    22,
                                    Color.parseColor("#80000000"),
                                    false
                            )
                    );
                }

                bottomBarBinding.main.quantityTxt.setText(String.valueOf(quantity));
            } else {
                bottomBarBinding.main.addToCartSection.setVisibility(View.GONE);
                bottomBarBinding.main.buttonAddToCart.setVisibility(View.GONE);
            }
        } else {
            viewModel.setAddToCartButtonVisibility(false);
            bottomBarBinding.main.setIsCartEnabled(false);
            bottomBarBinding.main.textNotInStock.setVisibility(View.GONE);
            List<Price> value = viewModel.getPrice().getValue();
            if (value != null && value.size() > 0) {
                bottomBarBinding.main.callForPrice.setText(value.get(0).getPrice());
                bottomBarBinding.main.price.setVisibility(View.GONE);
            }

        }
    }

    private void updateIconBadge() {
        if (badgeCount > 0 && actionBarMenu != null) {
            MenuItem item = actionBarMenu.findItem(MENU_CART_ID);
            if (item == null)
                return;
            item.setVisible(true);
            View actionView = item.getActionView();
            TextView cartBadgeView = actionView.findViewById(R.id.badge);
            cartBadgeView.setVisibility(View.VISIBLE);
            cartBadgeView.setText(String.valueOf(badgeCount));
        }
    }

    private void changeHearIcon(Boolean aBoolean) {
        MenuItem menuItem = actionBarMenu.findItem(MENU_LIKE_ID);

        if (menuItem == null)
            return;

        if (aBoolean) {
            menuItem.setIcon(getDrawable(
                    "&#xf004;",
                    22,
                    getResources().getColor(R.color.red_color_badge),
                    true
                    )
            );
            if (addToWishListClick) {
                UiComponents.showSnack(requireActivity(), getString(R.string.product_added_to_wish));
                addToWishListClick = false;
            }
        } else {
            menuItem.setIcon(getDrawable(
                    "&#xf004;",
                    22,
                    Color.BLACK,
                    false // outlined equivalent is the same code but `regular`
                    )
            );
            if (addToWishListClick) {
                UiComponents.showSnack(requireActivity(), getString(R.string.product_removed_from_wish));
                addToWishListClick = false;
            }
        }
    }

    private void checkBottomBarVisibility() {
        stub = requireActivity().findViewById(R.id.product_layout_stub);
        if (stub != null) {
            stub.setLayoutResource(R.layout.frgament_product_bb_with_shimmer);
            stub.setOnInflateListener((stub1, inflated) -> {
                setUpBottomBar(inflated);
            });
            stub.inflate();

        } else {
            bottomBar = requireActivity().findViewById(R.id.bottom_bar_inflated);
            if (bottomBar != null) {
                bottomBar.setVisibility(View.VISIBLE);
                setUpBottomBar(bottomBar);
            }
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private void setUpBottomBar(View inflatedView) {

        bottomBarBinding = DataBindingUtil.bind(inflatedView);

        if (bottomBarBinding == null)
            return;

        bottomBarBinding.setLifecycleOwner(this);
        bottomBarBinding.main.setViewModel(viewModel);
        bottomBarBinding.main.setCartViewModel(productCartViewModel);

        bottomBarBinding.main.additionButton.setOnClickListener(v -> {
            productCartViewModel.setProgressUpdatingCart(true);
            productCartViewModel.incrementProductQuantity();
            productCartViewModel.updateCart(addToCartLink, selectedVariantId);
        });

        bottomBarBinding.main.subtractButton.setOnClickListener(v -> {
            productCartViewModel.setProgressUpdatingCart(true);

            // check if should drop to zero or normal count subtraction
            if (isMinQuantity)
                productCartViewModel.updateProductQuantity(0);
            else
                productCartViewModel.subtractProductQuantity();

            productCartViewModel.updateCart(addToCartLink, selectedVariantId);
        });

        bottomBarBinding.main.setOnAddProductToCartClick(v -> {
            productCartViewModel.setProgressUpdatingCart(true);
            viewModel.setAddToCartButtonVisibility(false);
            productCartViewModel.incrementProductQuantity();
            productCartViewModel.updateCart(addToCartLink, selectedVariantId);
        });

        bottomBarBinding.main.setOnAddToWaitList(v -> {
            if (viewModel.isLogin()) {
                bottomBarBinding.main.waitListButton.setEnabled(false);
                viewModel.addToWaitList();
            } else {
                if (viewModel.getReturnAction() != null) {
                    registerReturnAndNavigateToLogin();
                }
            }
        });

        viewModel.getAddToWaitListResponse().observe(this, response -> {
            bottomBarBinding.main.waitListButton.setEnabled(true);
            if (!response.isEmpty())
                UiComponents.showSnack(requireActivity(), response);
        });
    }

    private void registerReturnAndNavigateToLogin() {

        viewModel.saveReturnAction(viewModel.getReturnAction());

        navigator.navigate(
                requireActivity(),
                "index.php?option=com_users&view=login",
                this::handleDefaultNavigationState);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideBottomBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null && viewModel.hasCart())
            checkBottomBarVisibility();
    }

    private void handleProductCartClick() {
        if (viewModel.getCartInfo().getValue() != null) {

            boolean hasAction = viewModel.getCartInfo().getValue()
                    .get("content").getAsJsonObject()
                    .has("checkout_link");

            if (hasAction) {
                JsonObject args = viewModel.getCartInfo().getValue()
                        .get("content").getAsJsonObject()
                        .get("checkout_link").getAsJsonObject();

                navigator.navigate(
                        requireActivity(),
                        args.get("link").getAsString(),
                        this::handleDefaultNavigationState
                );
            } else
                UiComponents.showSnack(requireActivity(), getString(R.string.empty_cart));
        }


    }

    private void onImageSlidShowClick(List<GalleryItem> gallery, int position) {
        NavigationArgs args = new NavigationArgs("تصاویر", "", null, -1, "", gallery);
        Bundle bundle = new Bundle();
        String accessingKey = UUID.randomUUID().toString();
        routePersistence.addRoute(accessingKey, args);
        bundle.putString("key", accessingKey);

        navigator.intrinsicNavigate(
                requireActivity(),
                ProductFragmentDirections.actionGoToGallery(accessingKey, position),
                true
        );
    }


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {

        menu.clear();

        List<DynamicMenu> menuList = new ArrayList<>();

        menuList.add(new DynamicMenu(MENU_LIKE_ID, "Like", "&#xf004;", false, 22, Color.BLACK));
        menuList.add(new DynamicMenu(MENU_CART_ID, "Cart", "&#xf07a;", false, 22, Color.BLACK));

        for (int i = 0; i < menuList.size(); i++) {
            DynamicMenu dynamicMenu = menuList.get(i);

            MenuItem menuItem = menu.add(
                    Menu.NONE,
                    dynamicMenu.getId(),
                    Menu.NONE,
                    dynamicMenu.getTitle()
            ); // key is the same as title

            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menuItem.setIcon(getDrawable(
                    dynamicMenu.getIcon(),
                    dynamicMenu.getTextSize(),
                    dynamicMenu.getIconColor(),
                    dynamicMenu.isSolidIcon()
                    )
            );

            // add cart action layout
            if (dynamicMenu.getId() == MENU_CART_ID) {
                menuItem.setActionView(R.layout.menu_product_cart_action_layout);
            }
        }

        actionBarMenu = menu;
        MenuItem item = actionBarMenu.findItem(MENU_CART_ID);
        if (item == null)
            return;
        item.setVisible(false);
        View actionView = item.getActionView();
        actionView.setOnClickListener(v -> handleProductCartClick());
        updateIconBadge();

        if (hasAddToWishList != null) {
            MenuItem likeItem = actionBarMenu.findItem(MENU_LIKE_ID);
            if (likeItem != null)
                likeItem.setVisible(hasAddToWishList);
        }

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == MENU_LIKE_ID) {

            if (viewModel.isLogin()) {
                viewModel.addToWishList();
                addToWishListClick = true;
            } else {

                if (viewModel.getReturnAction() != null) {

                    registerReturnAndNavigateToLogin();
                }
            }
            return true;
        } else return handleSearchSelection(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
        productCartViewModel.disposeAll();
    }
}