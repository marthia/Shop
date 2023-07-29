package com.poonehmedia.app.ui.modules;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.BR;
import com.poonehmedia.app.BuildConfig;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.ModuleMetadata;
import com.poonehmedia.app.data.model.TripleSnapPage;
import com.poonehmedia.app.databinding.ListItemEmptyBinding;
import com.poonehmedia.app.databinding.ListItemGridModuleBinding;
import com.poonehmedia.app.databinding.ListItemHtmlModuleBinding;
import com.poonehmedia.app.databinding.ListItemModuleCardProductBinding;
import com.poonehmedia.app.databinding.ListItemModuleHeadlineSliderBinding;
import com.poonehmedia.app.databinding.ListItemModulesBinding;
import com.poonehmedia.app.databinding.ListItemSliderBinding;
import com.poonehmedia.app.databinding.ListItemSuggestionBinding;
import com.poonehmedia.app.databinding.ListItemTrendingBinding;
import com.poonehmedia.app.databinding.ListItemTwoColumnModuleBinding;
import com.poonehmedia.app.ui.adapter.EmptyViewHolder;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.adapter.SliderAdapter;
import com.poonehmedia.app.ui.interfaces.ClickProvider;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.DividerDecor;
import com.poonehmedia.app.util.ui.ItemSpaceDecoration;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderPager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ModulesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int EMPTY = -1;
    private final int SLIDER = 0;
    private final int CATEGORY = 1;
    private final int TRENDING = 2;
    private final int SUGGESTION = 3;
    private final int PRODUCTS = 4;
    private final int HTML = 5;
    private final int ARTICLE = 6;
    private final int SCROLL = 7;
    private final int CARD_PRODUCT = 8;
    private final int HEADLINE_NEWS = 9;
    private final int GRID_PRODUCTS = 10;
    private final int TWO_PANE = 11;
    private final int TRIPLE_SNAP_PAGE = 12;
    private final DataController dataController;
    private ClickProvider callback;
    private String lang; // used for article module(`CustomHtml`) for choosing css styling files
    private JsonArray items;

    @Inject
    public ModulesAdapter(DataController dataController) {
        this.dataController = dataController;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void subscribeCallbacks(ClickProvider callback) {
        this.callback = callback;
    }

    public void submitList(JsonArray items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == SLIDER) {
            ListItemSliderBinding slider = ListItemSliderBinding.inflate(layoutInflater, parent, false);
            initSlider(slider, parent.getContext());
            return new SliderVH(slider);
        } else if (viewType == CATEGORY) {
            ListItemModulesBinding categories = ListItemModulesBinding.inflate(layoutInflater, parent, false);
            initCategories(categories);
            return new CategoriesVH(categories);
        } else if (viewType == TRENDING) {
            ListItemTrendingBinding trending = ListItemTrendingBinding.inflate(layoutInflater, parent, false);
            initTrending(trending);
            return new TrendingVH(trending);
        } else if (viewType == SUGGESTION) {
            ListItemSuggestionBinding suggestion = ListItemSuggestionBinding.inflate(layoutInflater, parent, false);
            initSuggestion(suggestion);
            return new SuggestionVH(suggestion);
        } else if (viewType == PRODUCTS) {
            ListItemModulesBinding products = ListItemModulesBinding.inflate(layoutInflater, parent, false);
            initProducts(products, parent.getContext());
            return new ProductsVH(products);
        } else if (viewType == HTML) {
            ListItemHtmlModuleBinding htmlModule = ListItemHtmlModuleBinding.inflate(layoutInflater, parent, false);
            initHtmlModule(htmlModule);
            return new HtmlModuleVH(htmlModule);
        } else if (viewType == ARTICLE) {
            ListItemModulesBinding articleModule = ListItemModulesBinding.inflate(layoutInflater, parent, false);
            initArticleModule(articleModule);
            return new ArticleVH(articleModule);
        } else if (viewType == SCROLL) {
            ListItemModulesBinding scrollModule = ListItemModulesBinding.inflate(layoutInflater, parent, false);
            initScrollModule(scrollModule, parent.getContext());
            return new ScrollVH(scrollModule);
        } else if (viewType == CARD_PRODUCT) {
            ListItemModuleCardProductBinding cardProduct = ListItemModuleCardProductBinding.inflate(layoutInflater, parent, false);
            return new CardProductVH(cardProduct);
        } else if (viewType == HEADLINE_NEWS) {
            ListItemModuleHeadlineSliderBinding slider = ListItemModuleHeadlineSliderBinding.inflate(layoutInflater, parent, false);
            initHeadlineNews(slider);
            return new HeadlineNewsVH(slider);
        } else if (viewType == TRIPLE_SNAP_PAGE) {
            ListItemModulesBinding binding = ListItemModulesBinding.inflate(layoutInflater, parent, false);
            initTripleSnapPage(binding);
            return new TripleSnapPagerVH(binding);
        } else if (viewType == TWO_PANE) {
            ListItemTwoColumnModuleBinding binding = ListItemTwoColumnModuleBinding.inflate(layoutInflater, parent, false);
            initTwoPane(binding);
            return new TwoPaneProductsVH(binding);
        } else if (viewType == GRID_PRODUCTS) {
            ListItemGridModuleBinding binding = ListItemGridModuleBinding.inflate(layoutInflater, parent, false);
            iniGridModule(binding);
            return new GridModuleVH(binding);
        } else if (viewType == EMPTY) {
            ListItemEmptyBinding empty = ListItemEmptyBinding.inflate(layoutInflater, parent, false);
            return new EmptyViewHolder(empty);
        } else throw new RuntimeException("expected a defined view type");
    }

    private void iniGridModule(ListItemGridModuleBinding binding) {
        GenericListAdapterImp<JsonElement> adapter = new GenericListAdapterImp<>();
        adapter.setLayoutRes(R.layout.list_item_module_grid_products);
        adapter.subscribeCallbacks(callback);
        binding.recycler.addItemDecoration(
                new DividerDecor(binding.recycler.getContext(), 16, DividerDecor.HORIZONTAL)
        );
        binding.recycler.addItemDecoration(
                new DividerDecor(binding.recycler.getContext(), 16, DividerDecor.VERTICAL)
        );
        binding.recycler.setAdapter(adapter);
    }

    private void initTwoPane(ListItemTwoColumnModuleBinding binding) {
        TwoPaneProductsAdapter adapter = new TwoPaneProductsAdapter(dataController);
        adapter.subscribeCallbacks(callback);
        binding.recycler.addItemDecoration(
                new DividerDecor(binding.recycler.getContext(), 16, DividerDecor.HORIZONTAL)
        );
        binding.recycler.addItemDecoration(
                new DividerDecor(binding.recycler.getContext(), 16, DividerDecor.VERTICAL)
        );
        binding.recycler.setAdapter(adapter);
        binding.recycler.setItemViewCacheSize(20);
    }

    private void initTripleSnapPage(ListItemModulesBinding binding) {
        TripleSnapPageAdapter adapter = new TripleSnapPageAdapter();
        adapter.subscribeCallbacks(callback);
        binding.recycler.setAdapter(adapter);
    }

    private void initHeadlineNews(ListItemModuleHeadlineSliderBinding binding) {
        HeadlineNewsModuleAdapter headlineNewsModuleAdapter = new HeadlineNewsModuleAdapter();
        headlineNewsModuleAdapter.subscribeCallbacks(callback);
        binding.imageSlider.setSliderAdapter(headlineNewsModuleAdapter);

        int gap = binding.getRoot().getContext().getResources().getDimensionPixelSize(R.dimen.plane_16);
        int halfGap = gap / 2;
        SliderPager tilePager = binding.imageSlider.getSliderPager();

        tilePager.setPadding(gap, 0, gap, halfGap);
        tilePager.setClipToPadding(false);
        tilePager.setClipChildren(false);
        tilePager.setPageMargin(halfGap);
    }

    private void initScrollModule(ListItemModulesBinding binding, Context context) {
        ScrollModuleAdapter scrollModuleAdapter = new ScrollModuleAdapter();
        scrollModuleAdapter.subscribeCallbacks(callback);
        binding.recycler.addItemDecoration(new DividerDecor(context, 16, DividerDecor.HORIZONTAL));
        binding.recycler.setAdapter(scrollModuleAdapter);
    }

    private void initArticleModule(ListItemModulesBinding binding) {
        GenericListAdapterImp<JsonElement> articleAdapter = new GenericListAdapterImp<>();
        ItemSpaceDecoration spaceDecoration = new ItemSpaceDecoration(16);
        articleAdapter.setLayoutRes(R.layout.list_item_article);
        articleAdapter.subscribeCallbacks(callback);
        binding.recycler.setAdapter(articleAdapter);
        binding.recycler.addItemDecoration(spaceDecoration);
    }

    private void initHtmlModule(ListItemHtmlModuleBinding htmlModule) {
        htmlModule.webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
    }

    private void initTrending(ListItemTrendingBinding trending) {
        TrendingModuleAdapter trendingModuleAdapter = new TrendingModuleAdapter();
        trendingModuleAdapter.subscribeCallbacks(callback);
        trending.recycler.setAdapter(trendingModuleAdapter);
    }

    private void initProducts(ListItemModulesBinding products, Context context) {
        ProductsModuleAdapter productsAdapter = new ProductsModuleAdapter(dataController);
        productsAdapter.subscribeCallbacks(callback);
        products.recycler.addItemDecoration(new DividerDecor(context, 16, DividerDecor.HORIZONTAL));
        products.recycler.setAdapter(productsAdapter);
        products.recycler.setItemViewCacheSize(20);
    }

    private void initSuggestion(ListItemSuggestionBinding suggestion) {
        SuggestionsModuleAdapter suggestionsModuleAdapter = new SuggestionsModuleAdapter(dataController);
        suggestionsModuleAdapter.subscribeCallbacks(callback);
        ItemSpaceDecoration spaceDecoration = new ItemSpaceDecoration(16);
        suggestion.suggestionsRecycler.setAdapter(suggestionsModuleAdapter);
        suggestion.suggestionsRecycler.addItemDecoration(spaceDecoration);
        suggestion.suggestionsRecycler.setItemViewCacheSize(20);

    }

    private void initCategories(ListItemModulesBinding categories) {
        CategoriesModuleAdapter categoriesModuleAdapter = new CategoriesModuleAdapter();
        categoriesModuleAdapter.subscribeCallbacks(callback);
        ItemSpaceDecoration spaceDecoration = new ItemSpaceDecoration(16);
        categories.recycler.setAdapter(categoriesModuleAdapter);
        categories.recycler.addItemDecoration(spaceDecoration);
    }

    private void initSlider(ListItemSliderBinding slider, Context context) {

        SliderAdapter sliderAdapter = new SliderAdapter();
        sliderAdapter.subscribeCallbacks(callback);
        slider.imageSlider.setSliderAdapter(sliderAdapter);

        int defaultGap = context.getResources().getDimensionPixelSize(R.dimen.plane_16);
        slider.imageSlider.getLayoutParams().height = (int) (((context.getResources().getDisplayMetrics().widthPixels - defaultGap) / 1.777778f));
        slider.imageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM);
        SliderPager tilePager = slider.imageSlider.getSliderPager();

        int halfGap = defaultGap / 2;
        tilePager.setPadding(defaultGap, 0, defaultGap, 0);
        tilePager.setClipToPadding(false);
        tilePager.setClipChildren(false);
        tilePager.setPageMargin(halfGap);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        JsonObject item = items.get(position).getAsJsonObject();

        if (holder instanceof SliderVH) ((SliderVH) holder).bind(item);
        else if (holder instanceof CategoriesVH) ((CategoriesVH) holder).bind(item);
        else if (holder instanceof TrendingVH) ((TrendingVH) holder).bind(item);
        else if (holder instanceof SuggestionVH) ((SuggestionVH) holder).bind(item);
        else if (holder instanceof ProductsVH) ((ProductsVH) holder).bind(item);
        else if (holder instanceof HtmlModuleVH) ((HtmlModuleVH) holder).bind(item);
        else if (holder instanceof ArticleVH) ((ArticleVH) holder).bind(item);
        else if (holder instanceof ScrollVH) ((ScrollVH) holder).bind(item);
        else if (holder instanceof CardProductVH) ((CardProductVH) holder).bind(item);
        else if (holder instanceof HeadlineNewsVH) ((HeadlineNewsVH) holder).bind(item);
        else if (holder instanceof GridModuleVH) ((GridModuleVH) holder).bind(item);
        else if (holder instanceof TripleSnapPagerVH) ((TripleSnapPagerVH) holder).bind(item);
        else if (holder instanceof TwoPaneProductsVH) ((TwoPaneProductsVH) holder).bind(item);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            String widgetType = items.get(position).getAsJsonObject().get("type").getAsString();

            if ("SlideModule".equals(widgetType)) return SLIDER;
            else if ("HeadlineNewsSlide".equals(widgetType)) return HEADLINE_NEWS;
            else if ("ShopCategories2".equals(widgetType)) return CATEGORY;
            else if ("ItemsModule".equals(widgetType)) return TRENDING;
            else if ("ShopProducts1".equals(widgetType)) return SUGGESTION;
            else if ("ShopProducts2".equals(widgetType)) return PRODUCTS;
            else if ("ShopProducts3".equals(widgetType)) return CARD_PRODUCT;
            else if ("CustomHtml".equals(widgetType)) return HTML;
            else if ("ArticleCategories".equals(widgetType)) return ARTICLE;
            else if ("ScrollModule".equals(widgetType)) return SCROLL;
            else if ("SnapModule".equals(widgetType)) return TRIPLE_SNAP_PAGE;
            else if ("gridModule".equals(widgetType)) return GRID_PRODUCTS;
            else if ("columnModule".equals(widgetType)) return TWO_PANE;
        } catch (Exception e) {
            Log.e("HomeModules", "NO TYPE PROVIDED FOR POSITION " + position);
        }
        return EMPTY;
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    private class SliderVH extends BaseViewHolder {

        private final ListItemSliderBinding binding;

        public SliderVH(@NonNull ListItemSliderBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            SliderAdapter adapter = (SliderAdapter) binding.imageSlider.getSliderAdapter();
            adapter.submitList(item.get("content").getAsJsonArray());
            binding.imageSlider.startAutoCycle();
            ModuleMetadata metadata = bindTitleAndShowAll(item);
            adapter.setMetadata(metadata);


            binding.executePendingBindings();
        }
    }

    private class CategoriesVH extends BaseViewHolder {

        private final ListItemModulesBinding binding;

        public CategoriesVH(@NonNull ListItemModulesBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            bindTitleAndShowAll(item);
            CategoriesModuleAdapter adapter = (CategoriesModuleAdapter) binding.recycler.getAdapter();
            adapter.submitList(item.get("content").getAsJsonArray());

            binding.executePendingBindings();
        }
    }

    private class TrendingVH extends BaseViewHolder {

        private final ListItemTrendingBinding binding;

        public TrendingVH(@NonNull ListItemTrendingBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            TrendingModuleAdapter adapter = (TrendingModuleAdapter) binding.recycler.getAdapter();
            ModuleMetadata metadata = bindTitleAndShowAll(item);

            adapter.setMetadata(metadata);
            ItemSpaceDecoration itemSpaceDecoration = new ItemSpaceDecoration(itemView.getContext(), 8);
            itemSpaceDecoration.excludeFace(ItemSpaceDecoration.Face.BOTTOM);
            binding.recycler.addItemDecoration(itemSpaceDecoration);
            ((GridLayoutManager) binding.recycler.getLayoutManager()).setSpanCount(metadata.getColumns());

            adapter.submitList(item.get("content").getAsJsonArray());
        }
    }

    private class SuggestionVH extends BaseViewHolder {

        private final ListItemSuggestionBinding binding;

        public SuggestionVH(@NonNull ListItemSuggestionBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            SuggestionsModuleAdapter adapter = (SuggestionsModuleAdapter) binding.suggestionsRecycler.getAdapter();
            JsonArray content = item.get("content").getAsJsonArray();

            ModuleMetadata metadata = bindTitleAndShowAll(item);

            adapter.submitList(content);
            adapter.submitMetaData(metadata);
            if (!metadata.getBackgroundColor().isEmpty())
                binding.suggestionsRecyclerLayout.setBackgroundColor(Color.parseColor(metadata.getBackgroundColor()));

            binding.executePendingBindings();
        }
    }

    private class ProductsVH extends BaseViewHolder {

        private final ListItemModulesBinding binding;

        public ProductsVH(@NonNull ListItemModulesBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            ProductsModuleAdapter adapter = (ProductsModuleAdapter) binding.recycler.getAdapter();

            ModuleMetadata metadata = bindTitleAndShowAll(item);
            adapter.submitMetaData(metadata);
            adapter.submitList(item.get("content").getAsJsonArray());


            binding.executePendingBindings();
        }
    }

    private class HtmlModuleVH extends BaseViewHolder {

        private final ListItemHtmlModuleBinding binding;

        public HtmlModuleVH(@NonNull ListItemHtmlModuleBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {

            bindTitleAndShowAll(item);
            String htmlContent = dataController.generateHtmlContent(
                    dataController.getWebViewStyles(itemView.getContext()),
                    dataController.getWebViewJs(false),
                    item.get("content").getAsString(),
                    lang
            );
            binding.webView.loadDataWithBaseURL(
                    BuildConfig.baseUrl,
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    ""
            );

            binding.webView.subscribeNavigation(this::navigate);

            binding.webView.subscribeInternalNavigation(this::navigate);

            binding.executePendingBindings();
        }

        private void navigate(String url) {
            JsonObject obj = new JsonObject();
            obj.addProperty("link", url);
            callback.onClick(obj, getAbsoluteAdapterPosition());
        }

    }

    private class ArticleVH extends BaseViewHolder {

        private final ListItemModulesBinding binding;

        public ArticleVH(@NonNull ListItemModulesBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            GenericListAdapterImp<JsonElement> adapter = (GenericListAdapterImp<JsonElement>) binding.recycler.getAdapter();

            ModuleMetadata metadata = bindTitleAndShowAll(item);
            adapter.submitList(item.get("content").getAsJsonArray());
            if (!metadata.getBackgroundColor().isEmpty())
                binding.container.setBackgroundColor(Color.parseColor(metadata.getBackgroundColor()));

            binding.executePendingBindings();
        }
    }

    private class ScrollVH extends BaseViewHolder {

        private final ListItemModulesBinding binding;

        public ScrollVH(@NonNull ListItemModulesBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            ScrollModuleAdapter adapter = (ScrollModuleAdapter) binding.recycler.getAdapter();

            ModuleMetadata metadata = bindTitleAndShowAll(item);
            adapter.setParams(metadata);
            adapter.submitList(item.get("content").getAsJsonArray());

            binding.executePendingBindings();
        }
    }

    private class CardProductVH extends BaseViewHolder {
        private final ListItemModuleCardProductBinding binding;

        public CardProductVH(ListItemModuleCardProductBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            bindTitleAndShowAll(item);

            ProductsCardModuleAdapter productsCardModuleAdapter = new ProductsCardModuleAdapter(dataController);
            binding.recycler.setAdapter(productsCardModuleAdapter);
            productsCardModuleAdapter.subscribeCallbacks(callback);
            productsCardModuleAdapter.submitList(item.get("content").getAsJsonArray());

            binding.executePendingBindings();
        }
    }

    private class HeadlineNewsVH extends BaseViewHolder {
        private final ListItemModuleHeadlineSliderBinding binding;

        public HeadlineNewsVH(ListItemModuleHeadlineSliderBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            HeadlineNewsModuleAdapter adapter = (HeadlineNewsModuleAdapter) binding.imageSlider.getSliderAdapter();
            bindTitleAndShowAll(item);
            adapter.submitList(item.get("content").getAsJsonArray());

            binding.executePendingBindings();
        }
    }

    private class TripleSnapPagerVH extends BaseViewHolder {

        private final ListItemModulesBinding binding;

        public TripleSnapPagerVH(@NonNull ListItemModulesBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            TripleSnapPageAdapter adapter = (TripleSnapPageAdapter) binding.recycler.getAdapter();
            bindTitleAndShowAll(item);
            adapter.submitList(extractTripleSnapData(item.get("content").getAsJsonArray()));

            binding.executePendingBindings();

        }

        private List<TripleSnapPage> extractTripleSnapData(JsonArray content) {
            List<TripleSnapPage> result = new ArrayList<>();

            for (int i = 0; i < content.size(); i += 3) {
                JsonArray array = new JsonArray();
                for (int j = i; j < i + 3; j++) {
                    JsonObject data = content.get(j).getAsJsonObject();
                    data.addProperty("index", dataController.getPersianNumber(j + 1));
                    array.add(data);
                }
                result.add(new TripleSnapPage(array));
            }
            return result;
        }
    }

    private class TwoPaneProductsVH extends BaseViewHolder {

        private final ListItemTwoColumnModuleBinding binding;

        public TwoPaneProductsVH(@NonNull ListItemTwoColumnModuleBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            TwoPaneProductsAdapter adapter = (TwoPaneProductsAdapter) binding.recycler.getAdapter();
            bindTitleAndShowAll(item);
            adapter.submitList(item.get("content").getAsJsonArray());

            binding.executePendingBindings();
        }
    }

    private class GridModuleVH extends BaseViewHolder {

        private final ListItemGridModuleBinding binding;

        public GridModuleVH(@NonNull ListItemGridModuleBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public void bind(JsonObject item) {
            GenericListAdapterImp<JsonElement> adapter = (GenericListAdapterImp<JsonElement>) binding.recycler.getAdapter();
            ModuleMetadata metadata = bindTitleAndShowAll(item);
            // only span counts of 2 or 3 are supported.
            int min = Math.max(2, metadata.getColumns());
            int max = Math.min(3, min);

            ((GridLayoutManager) binding.recycler.getLayoutManager()).setSpanCount(max);
            adapter.submitList(item.get("content").getAsJsonArray());

            binding.executePendingBindings();
        }
    }

    private class BaseViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding binding;

        public BaseViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        protected ModuleMetadata bindTitleAndShowAll(JsonObject item) {
            if (!JsonHelper.has(item, "params"))
                return null;

            JsonObject params = item.get("params").getAsJsonObject();
            ModuleMetadata metadata = new ModuleMetadata();

            metadata.setTitle(JsonHelper.tryGetString(params, "title"));
            metadata.setSubtitle(JsonHelper.tryGetString(params, "subtitle"));
            metadata.setIconLink(JsonHelper.tryGetString(params, "icon"));
            try {
                metadata.setColumns(Integer.parseInt(JsonHelper.tryGetString(params, "cols")));
            } catch (Exception e) {
                // ignore
            }
            metadata.setBackgroundColor(JsonHelper.tryGetString(params, "background_color"));
            metadata.setBackgroundImage(JsonHelper.tryGetString(params, "background_image"));
            metadata.setThumbWidth(JsonHelper.tryGetString(params, "thumb_width"));
            metadata.setThumbHeight(JsonHelper.tryGetString(params, "thumb_height"));
            metadata.setThumbRadius(JsonHelper.tryGetString(params, "thumb_radius"));
            metadata.setTitleColor(JsonHelper.tryGetString(params, "title_color"));
            metadata.setReadMoreTextColor(JsonHelper.tryGetString(params, "readmore_color"));
            metadata.setImagePosition(JsonHelper.tryGetString(params, "image_position"));
            metadata.setShowPrice(JsonHelper.tryGetString(params, "show_price").equals("1"));
            metadata.setShowTitle(JsonHelper.tryGetString(params, "show_title").equals("1"));
            metadata.setShowDate(JsonHelper.tryGetString(params, "show_text").equals("1"));
            metadata.setShowText(JsonHelper.tryGetString(params, "show_date").equals("1"));
            metadata.setShowImage(JsonHelper.tryGetString(params, "show_image").equals("1"));
            metadata.setClickable(JsonHelper.tryGetString(params, "clickable").equals("1"));

            if (JsonHelper.has(params, "readmore")) {
                JsonObject readMore = params.get("readmore").getAsJsonObject();
                metadata.setReadMoreText(readMore.get("text").getAsString());
                metadata.setReadMoreActionObject(readMore);

                metadata.setReadMoreAction(() -> callback.onClick(readMore, getAbsoluteAdapterPosition()));
            }

            binding.setVariable(BR.item, metadata);

            return metadata;
        }
    }

}

