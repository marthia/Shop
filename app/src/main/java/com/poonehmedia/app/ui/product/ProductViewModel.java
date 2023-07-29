package com.poonehmedia.app.ui.product;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.CharacteristicsItem;
import com.poonehmedia.app.data.model.Comment;
import com.poonehmedia.app.data.model.GalleryItem;
import com.poonehmedia.app.data.model.ParameterItem;
import com.poonehmedia.app.data.model.Price;
import com.poonehmedia.app.data.repository.CommentsRepository;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.data.repository.ProductRepository;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.ui.interfaces.OnDataIsReady;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.base.TwinLiveData;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;


@HiltViewModel
public class ProductViewModel extends BaseViewModel {

    private final ProductRepository repository;
    private final CommentsRepository commentsRepository;
    private final DataController dataController;
    private final MutableLiveData<String> title = new MutableLiveData<>();
    private final MutableLiveData<Float> voteInfo = new MutableLiveData<>();
    private final MutableLiveData<String> voteCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> variantVisible = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> similarProductsVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> commentSectionVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> newCommentSectionVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> addToWaitList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addToCartButtonVisible = new MutableLiveData<>();
    private final MutableLiveData<Boolean> favoriteVisibility = new MutableLiveData<>();
    private final MutableLiveData<Boolean> othersBoughtVisibility = new MutableLiveData<>();
    private final MutableLiveData<Boolean> priceHistoryVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> manufacturerVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> productsSpecsVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> chartVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> categoriesVisibitliy = new MutableLiveData<>(false);
    private final MutableLiveData<String> commentsCount = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> commentsReadMore = new MutableLiveData<>();
    private final MutableLiveData<String> addCommentText = new MutableLiveData<>();
    private final MutableLiveData<String> addCommentSubtitle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAddCommentLocked = new MutableLiveData<>();
    private final MutableLiveData<List<Price>> price = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> selectedVariant = new MutableLiveData<>(null);
    private final MutableLiveData<JsonArray> productCfs = new MutableLiveData<>();
    private final MutableLiveData<String> productCode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isInStock = new MutableLiveData<>(true);
    private final MutableLiveData<List<GalleryItem>> gallery = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> data = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> categories = new MutableLiveData<>();
    private final MutableLiveData<Integer> cartBadgeCount = new MutableLiveData<>(0);
    private final MutableLiveData<JsonObject> cartInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> addToCartInfo = new MutableLiveData<>();
    private final MutableLiveData<String> selectedVariantId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFavorite = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> similarProducts = new MutableLiveData<>();
    private final MutableLiveData<String> waitListResponse = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> priceHistory = new MutableLiveData<>();
    private final MutableLiveData<List<Comment>> commentsData = new MutableLiveData<>();
    private final MutableLiveData<JsonArray> priceHistoryPreview = new MutableLiveData<>();
    private final MutableLiveData<JsonObject> manufacturer = new MutableLiveData<>();
    private final MutableLiveData<String> chart = new MutableLiveData<>();
    private final JsonObject currentVariant = new JsonObject();
    private final MutableLiveData<String> totalPoint = new MutableLiveData<>();
    private final Context context;
    private final SavedStateHandle savedStateHandle;
    private final PreferenceManager preferenceManager;
    private final Map<Integer, Integer> parameterItemSelectionHolder = new HashMap<>();
    private JsonObject returnAction;
    private boolean hasCart = true;
    private JsonArray allCfs;
    private String path;
    private JsonElement rawData = null;
    private boolean isCartEnabled = true;
    private JsonObject addCommentObj = null;
    private String pageStartKey;

    @Inject
    public ProductViewModel(ProductRepository repository,
                            CommentsRepository commentsRepository,
                            DataController dataController,
                            SavedStateHandle savedStateHandle,
                            RoutePersistence routePersistence,
                            PreferenceManager preferenceManager,
                            @ApplicationContext Context context) {

        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.commentsRepository = commentsRepository;
        this.dataController = dataController;
        this.savedStateHandle = savedStateHandle;
        this.preferenceManager = preferenceManager;
        this.context = context;
    }

    public boolean isCartEnabled() {
        return isCartEnabled;
    }

    public void resolveData(OnDataIsReady callback, boolean shouldUseUpdatedVersion) {

        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);
        JsonObject object = null;
        if (navigationArgs != null && navigationArgs.getData() != null) {
            path = navigationArgs.getLink();
            pageStartKey = navigationArgs.getPagingStartKey();
            object = (JsonObject) navigationArgs.getData();
        }

        if (shouldUseUpdatedVersion)
            object = (JsonObject) rawData;

        extractResult(object, callback);
    }


    private void extractResult(JsonElement body, OnDataIsReady callback) {
        try {
            this.rawData = body;
            JsonArray array = dataController.extractDataItemsAsJsonArray(body);
            JsonObject dataItem = new JsonObject();

            if (JsonHelper.isNotEmptyNorNull(array.get(0)))
                dataItem = array.get(0).getAsJsonObject();

            if (dataItem.size() > 0) {
                data.setValue(dataItem);

                // similar products
                JsonObject similarProducts = dataController.extractModule(body, "ShopProducts2");
                if (similarProducts != null) {
                    similarProductsVisibility.postValue(true);
                    this.similarProducts.postValue(similarProducts);
                } else similarProductsVisibility.postValue(false);

                // cart info
                JsonObject cartInfo = dataController.extractModule(body, "shopMiniCart");
                if (cartInfo != null) {

                    cartBadgeCount.postValue(cartInfo.get("content").getAsJsonObject()
                            .get("checkout_text").getAsInt()
                    );

                    cartInfoLiveData.setValue(cartInfo);
                } else hasCart = false;

                // miscellaneous
                returnAction = dataController.extractInfo(body, "return_action");
                String s = extractTotalPointOrMinusOne(dataItem);
                totalPoint.postValue(s);
                callback.handle();

                // chart
                if (JsonHelper.has(dataItem, "price_chart")) {
                    this.chartVisibility.postValue(true);
                    this.chart.postValue(dataItem.get("price_chart").getAsString());
                } else this.chartVisibility.postValue(false);

                // manufacturer
                if (JsonHelper.has(dataItem, "product_manufacturer")) {
                    manufacturerVisibility.postValue(true);
                    manufacturer.postValue(dataItem.get("product_manufacturer").getAsJsonObject());
                } else manufacturerVisibility.postValue(false);

                // price history
                if (JsonHelper.has(dataItem, "price_history")) {
                    this.priceHistoryVisibility.postValue(true);
                    JsonObject priceHistory = dataItem.get("price_history").getAsJsonObject();

                    JsonArray normalData = priceHistory.get("data").getAsJsonArray();
                    JsonArray reversedData = dataController.reverse(normalData);

                    this.priceHistoryPreview.postValue(dataController.reduceBy(reversedData, 5));
                    this.priceHistory.postValue(reversedData);
                } else
                    this.priceHistoryVisibility.postValue(false);

                // category
                JsonObject categories = dataController.extractModule(body, "ShopCategories2");
                if (categories != null) {
                    this.categoriesVisibitliy.postValue(true);
                    this.categories.postValue(categories);
                } else this.categoriesVisibitliy.postValue(false);


                // comments
                JsonObject commentModule = dataController.extractModule(body, "JComments");
                if (commentModule != null) {
                    JsonObject commentsReadMore = commentModule.get("params").getAsJsonObject().get("readmore").getAsJsonObject();
                    this.commentsReadMore.postValue(commentsReadMore);

                    JsonArray content = commentModule.get("content").getAsJsonArray();
                    List<Comment> comments = dataController.bindCommentsJsonToObject(content);
                    if (comments.size() > 0) {
                        commentsCount.postValue(commentModule.get("info").getAsJsonObject().get("total").getAsString());
                        commentSectionVisibility.setValue(true);
                        commentsData.postValue(comments);
                    } else {
                        addCommentObj = commentModule.get("info").getAsJsonObject().get("addComment").getAsJsonObject();
                        commentsRepository.setAddCommentObj(addCommentObj);

                        if (addCommentObj == null) {
                            JsonObject lockedObj = commentModule.get("info").getAsJsonObject().get("commentsLocked").getAsJsonObject();
                            isAddCommentLocked.postValue(true);
                            addCommentSubtitle.postValue(lockedObj.get("text").getAsString());
                        } else {
                            addCommentText.postValue(addCommentObj.get("text").getAsString());
                            addCommentSubtitle.postValue(context.getString(R.string.comments_subtitle));
                            isAddCommentLocked.postValue(false);
                        }
                        newCommentSectionVisibility.postValue(true);
                    }

                }
            }
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("could not resolve data in (ProductViewModel). data: " + data, e));
        }
    }

    public LiveData<JsonObject> getCommentsReadMore() {
        return commentsReadMore;
    }

    public void fetchData(OnDataIsReady callback) {

        requestData(repository.getData(path), response -> {

            dataController.onSuccess(response);
            if (response.isSuccessful()) {
                extractResult(response.body(), callback);
            }
        }, dataController::onFailure);

    }

    public LiveData<String> getAddCommentText() {
        return addCommentText;
    }

    public LiveData<String> getAddCommentSubtitle() {
        return addCommentSubtitle;
    }

    public LiveData<Boolean> getAddCommentLocked() {
        return isAddCommentLocked;
    }

    public LiveData<List<Comment>> getCommentsData() {
        return commentsData;
    }

    public LiveData<Boolean> getCategoriesVisibility() {
        return categoriesVisibitliy;
    }

    public LiveData<JsonObject> getCategories() {
        return categories;
    }

    private String extractTotalPointOrMinusOne(JsonObject data) {
        try {
            return data.get("total_point").getAsString();
        } catch (Exception e) {
            Log.e("totalPoint", "NOT PROVIDED");
        }
        return "-1";
    }

    public LiveData<JsonObject> getManufacturer() {
        return manufacturer;
    }

    public LiveData<Boolean> getPriceHistoryVisibility() {
        return priceHistoryVisibility;
    }

    public LiveData<JsonArray> getPriceHistoryPreview() {
        return priceHistoryPreview;
    }

    public LiveData<JsonArray> getPriceHistory() {
        return priceHistory;
    }

    public LiveData<String> getChartData() {
        return chart;
    }

    public LiveData<Boolean> getChartVisibility() {
        return chartVisibility;
    }

    public LiveData<Boolean> getManufacturerVisibility() {
        return manufacturerVisibility;
    }

    public JsonObject getReturnAction() {
        return returnAction;
    }

    public LiveData<JsonObject> getData() {
        return data;
    }

    public LiveData<List<Price>> getPrice() {
        return price;
    }

    public LiveData<String> getTitle() {

        return Transformations.switchMap(selectedVariant, input -> {
            if (input != null)
                title.postValue(input.get("title").getAsString());

            else title.postValue(data.getValue().get("title").getAsString());

            return title;
        });

    }

    public void setTitle(Map<Integer, CharacteristicsItem> titles) {

        String rawTitle = data.getValue().get("title").getAsString();
        StringBuilder builder = new StringBuilder();
        builder.append(rawTitle);
        for (int i = 0; i < titles.values().size(); i++) {
            if (i == 0)
                builder.append(":");

            builder.append(" ");
            builder.append(titles.get(i).getTitle());
        }
        this.title.postValue(builder.toString());
    }

    public LiveData<String> getTotalPoint() {
        return totalPoint;
    }

    public LiveData<Float> getVoteInfo() {
        return voteInfo;
    }

    public void setVoteInfoFromData() {
        try {
            JsonObject vote = data.getValue().get("vote").getAsJsonObject();
            String max = vote.get("max").getAsString();
            String rate = vote.get("rate").getAsString();
            String count = vote.get("count").getAsString();
            setVoteCount(count);
            this.voteInfo.postValue(Float.parseFloat(rate));
        } catch (Exception e) {
            Log.e("voteInfo", "NO VOTE PROVIDED");
        }
    }

    public LiveData<String> getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(String voteCount) {
        this.voteCount.postValue(String.format("(%s)", voteCount));
    }

    public LiveData<Boolean> getSimilarProductsVisibility() {
        return similarProductsVisibility;
    }

    public LiveData<String> getCommentsCount() {
        return commentsCount;
    }

    public LiveData<Boolean> getOthersBoughtVisibility() {
        return othersBoughtVisibility;
    }

    public LiveData<List<GalleryItem>> getGallery() {
        return Transformations.switchMap(selectedVariant, input -> {
            JsonArray images = null;
            JsonArray videos = null;

            if (input != null) {
                images = input.get("gallery").getAsJsonArray();
                if (input.has("videos"))
                    videos = input.get("videos").getAsJsonArray();
            } else {
                images = data.getValue().get("gallery").getAsJsonArray();

                if (data.getValue().has("videos"))
                    videos = data.getValue().get("videos").getAsJsonArray();
            }

            List<GalleryItem> galleryItems = unifyImagesAndVideos(images, videos);

            this.gallery.postValue(galleryItems);

            return this.gallery;
        });
    }

    private List<GalleryItem> unifyImagesAndVideos(JsonArray images, JsonArray videos) {
        try {
            List<GalleryItem> result = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {
                GalleryItem item = new GalleryItem();
                item.setImageUrlOrVideoThumb(images.get(i).getAsString());
                result.add(item);
            }

            if (videos != null)
                for (int i = 0; i < videos.size(); i++) {
                    GalleryItem item = new GalleryItem();
                    item.setImageUrlOrVideoThumb(videos.get(i).getAsJsonObject().get("thumb").getAsString());
                    item.setVideoLink(videos.get(i).getAsJsonObject().get("link").getAsString());
                    result.add(item);
                }

            return result;
        } catch (Exception e) {
            Log.e("galleryBlend", e.getMessage());
        }
        return new ArrayList<>();
    }

    public LiveData<JsonObject> getSimilarProducts() {
        return similarProducts;
    }

    public ArrayList<ParameterItem> getUpdatedVariantsData(JsonObject item) {
        currentVariant.addProperty(item.get("parent_pos").getAsString(), item.get("value").getAsString());
        return getVariantsData();
    }

    public ArrayList<ParameterItem> getVariantsData() {

        ArrayList<ParameterItem> list = new ArrayList<>();
        if (!data.getValue().has("variants") || data.getValue().get("variants").getAsJsonObject().size() == 0) {
            setVariantVisible(false);
            return null;
        }

        if (!data.getValue().has("characteristics") || data.getValue().get("characteristics").getAsJsonObject().size() == 0) {
            setVariantVisible(false);
            return null;
        }
        JsonObject characteristics = data.getValue().get("characteristics").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : characteristics.entrySet()) {
            JsonObject ch = entry.getValue().getAsJsonObject();
            if (!currentVariant.has(entry.getKey()))
                currentVariant.addProperty(entry.getKey(), ch.get("finalSelectedValue").getAsString());

            list.add(makeCharacteristicsSelector(entry.getKey()));
        }

        return list;
    }

    public ParameterItem makeCharacteristicsSelector(String key) {

        ParameterItem parameterItem = new ParameterItem();

        JsonObject characteristics = data.getValue().get("characteristics").getAsJsonObject();
        JsonObject ch = characteristics.get(key).getAsJsonObject();
        String before_index = ch.get("before_index").getAsString();

        JsonArray preparedArr = new JsonArray();
        for (Map.Entry<String, JsonElement> entry : ch.get("finalValues").getAsJsonObject().entrySet()) {
            JsonObject item = entry.getValue().getAsJsonObject();
            item.addProperty("parent_pos", key);
            item.addProperty("status", currentVariant.has(key) && item.getAsJsonObject().get("value").getAsString().equals(currentVariant.get(key).getAsString()));

            JsonObject validValues;

            if (before_index.equals("0")) {
                preparedArr.add(item);
                continue;
            } else if (!JsonHelper.has(currentVariant, before_index))
                continue;
            else if (!JsonHelper.has(characteristics, before_index))
                continue;
            else {
                validValues = characteristics
                        .get(before_index).getAsJsonObject()
                        .get("finalValues").getAsJsonObject()
                        .get(currentVariant.get(before_index).getAsString()).getAsJsonObject()
                        .get("valid_values").getAsJsonObject();

                if (!JsonHelper.has(validValues, key))
                    continue;
                else if (JsonHelper.isEmptyOrNull(validValues.get(key)))
                    continue;
            }

            JsonElement value = JsonParser.parseString("\"" + item.getAsJsonObject().get("value").getAsString() + "\"");
            JsonArray validArray = validValues.get(key).getAsJsonArray();

            if (!validArray.contains(value))
                continue;

            preparedArr.add(item);
        }
        parameterItem.setTitle(ch.get("display_text").getAsString() + ": ");

        if (preparedArr.size() != 0)
            parameterItem.setElement(preparedArr);

        parameterItem.setCharacteristicsSize(characteristics.size());

        parameterItem.setVisible(ch.get("before_index").getAsString().equals("0"));

        parameterItem.setDefaultSelectedItem(String.valueOf(ch.get("finalSelectedValue").getAsInt()));

        return parameterItem;
    }

    public void setVariantVisible(boolean variantVisible) {
        this.variantVisible.postValue(variantVisible);
    }

    public void setPriceData() {
        JsonObject item = data.getValue();
        if (selectedVariant.getValue() != null)
            item = selectedVariant.getValue();

        Object[] p = dataController.extractPriceAndMetadata(item, null);

        // if no_price_text has been used we should not show add to cart button
        this.isCartEnabled = !((Boolean) p[0]);

        price.postValue((List<Price>) p[1]);

    }

    public void clearPriceData() {
        price.postValue(new ArrayList<>());
    }

    public void setSelectedVariant(Map<Integer, CharacteristicsItem> charInfo) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < charInfo.size(); i++) {
            builder.append(charInfo.get(i).getValue());
            builder.append("_");
        }
        builder.deleteCharAt(builder.length() - 1);

        JsonObject variant = findVariantMatch(builder.toString());
        selectedVariant.setValue(variant);

    }

    private JsonObject findVariantMatch(String variantMatchId) {
        JsonObject variantsMap = data.getValue().get("variants_map").getAsJsonObject();
        String selectedVariantId = variantsMap.get(variantMatchId).getAsString();
        return data.getValue().get("variants").getAsJsonObject().get(selectedVariantId).getAsJsonObject();
    }

    public void clearSelectedVariant() {
        selectedVariant.setValue(null);
    }

    public LiveData<Boolean> getAddToCartButtonVisibility() {
        return addToCartButtonVisible;
    }

    public void setAddToCartButtonVisibility(Boolean visibility) {
        addToCartButtonVisible.postValue(visibility);
    }

    public LiveData<Boolean> getAddToWaitList() {

        TwinLiveData<JsonObject, JsonObject> twinLiveData = new TwinLiveData<>(data, selectedVariant);

        return Transformations.switchMap(twinLiveData, input -> {
            JsonObject item = input.second;

            if (item == null) {
                item = input.first;
            }
            try {
                boolean isInStock = item.get("instock").getAsBoolean();

                this.addToWaitList.postValue(
                        !isInStock && item.get("product_actions").getAsJsonObject()
                                .has("addmetowaitlist")
                );
            } catch (Exception e) {
                Log.e("addToCart", e.getMessage());
                this.addToWaitList.postValue(false);
            }
            return addToWaitList;
        });
    }

    public LiveData<JsonArray> getProductCfs() {

        return Transformations.switchMap(selectedVariant, input -> {
            JsonArray cfs = null;
            if (input != null && JsonHelper.has(input, "cf") &&
                    JsonHelper.isNotEmptyNorNull(input.get("cf"))
            ) {
                cfs = input.get("cf").getAsJsonArray();

            } else if (data.getValue() != null &&
                    JsonHelper.has(data.getValue(), "cf") &&
                    JsonHelper.isNotEmptyNorNull(data.getValue().get("cf"))
            ) {
                cfs = data.getValue().get("cf").getAsJsonArray();
            }

            this.allCfs = cfs;

            if (cfs != null) {
                productsSpecsVisibility.postValue(true);
                cfs = dataController.reduceBy(cfs, 3);
                productCfs.postValue(cfs);
            } else productsSpecsVisibility.postValue(false);

            return productCfs;
        });

    }

    public LiveData<Boolean> getProductsSpecsVisibility() {
        return productsSpecsVisibility;
    }

    public LiveData<String> getProductCode() {

        return Transformations.switchMap(selectedVariant, variantInput -> {
            if (variantInput != null)
                productCode.postValue(variantInput.get("product_code").getAsString());

            else
                productCode.postValue(data.getValue().get("product_code").getAsString());
            return productCode;
        });

    }

    public LiveData<Boolean> getIsInStock() {

        TwinLiveData<JsonObject, JsonObject> twinLiveData = new TwinLiveData<>(data, selectedVariant);

        return Transformations.switchMap(twinLiveData, input -> {
            JsonObject main = input.first;
            JsonObject variant = input.second;
            try {
                boolean isInStock = false;

                if (variant != null) {

                    boolean hasWaitList =
                            variant.get("product_actions").getAsJsonObject()
                                    .has("addmetowaitlist");
                    isInStock = variant.get("instock").getAsBoolean() && !hasWaitList;

                } else if (main != null) {

                    boolean hasWaitList = main.get("product_actions").getAsJsonObject()
                            .has("addmetowaitlist");

                    isInStock = main.get("instock").getAsBoolean() &&
                            !hasWaitList;
                }
                this.isInStock.postValue(isInStock);
            } catch (Exception e) {
                Log.e("addToCart", e.getMessage());
                this.isInStock.postValue(false);
            }
            return isInStock;
        });

    }

    public LiveData<Boolean> getCommentSectionVisibility() {
        return commentSectionVisibility;
    }

    public LiveData<Boolean> getNewCommentSectionVisibility() {
        return newCommentSectionVisibility;
    }

    public LiveData<Integer> getCartBadgeCount() {
        return cartBadgeCount;
    }

    public LiveData<JsonObject> getCartInfo() {
        return cartInfoLiveData;
    }

    public LiveData<Integer> getCurrentVariantCartNumber() {

        TwinLiveData<JsonObject, JsonObject> twinLiveData = new TwinLiveData<>(data, selectedVariant);

        return Transformations.map(twinLiveData, input -> {
            JsonObject item = input.second;

            if (item == null) {
                item = input.first;
            }

            String id = item.get("id").getAsString();

            int count = dataController.getCountForProductId(cartInfoLiveData.getValue(), id);

            selectedVariantId.postValue(id); // keep the id updated to update product quantity in add to cart response

            return count;
        });
    }

    public LiveData<String> getSelectedVariantId() {
        return selectedVariantId;
    }

    public LiveData<JsonObject> getAddToCartInfo() {
        return Transformations.switchMap(selectedVariant, input -> {
            JsonObject items = null;

            try {
                if (input != null) {
                    if (input.has("product_actions") && input.get("product_actions").getAsJsonObject().has("updatecart")) {
                        items = input.get("product_actions").getAsJsonObject().get("updatecart").getAsJsonObject();
                    }
                } else {
                    if (data.getValue().has("product_actions") && data.getValue().get("product_actions").getAsJsonObject().has("updatecart"))
                        items = data.getValue().get("product_actions").getAsJsonObject().get("updatecart").getAsJsonObject();
                }
            } catch (Exception e) {
                Log.e("addToCart", e.getMessage());
            }
            this.addToCartInfo.postValue(items);

            return this.addToCartInfo;
        });
    }

    public void updateBadgeCount(Integer count) {
        cartBadgeCount.postValue(count);
    }

    public void postCartInfo(JsonObject jsonObject) {
        cartInfoLiveData.postValue(jsonObject);
    }

    public LiveData<Boolean> hasAddToWishList() {
        TwinLiveData<JsonObject, JsonObject> twinLiveData = new TwinLiveData<>(data, selectedVariant);

        return Transformations.switchMap(twinLiveData, input -> {
            JsonObject item = input.second;

            if (item == null) {
                item = input.first;
            }
            try {
                this.favoriteVisibility.postValue(item
                        .get("product_actions").getAsJsonObject()
                        .has("addtowishlist")
                );
            } catch (Exception e) {
                Log.e("addToCart", e.getMessage());
                this.favoriteVisibility.postValue(false);
            }
            return favoriteVisibility;
        });
    }

    public void addToWishList() {

        JsonObject selectedVariantValue = selectedVariant.getValue();

        JsonObject item = data.getValue();
        if (selectedVariantValue != null) {
            item = selectedVariantValue;
        }

        JsonObject addToWish = item
                .get("product_actions").getAsJsonObject()
                .get("addtowishlist").getAsJsonObject();

        requestData(
                repository.addToWishCart(addToWish.get("link").getAsString(), addToWish.get("params").getAsJsonObject())
                , response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        rawData = response.body();
                        JsonArray object = dataController.extractDataItemsAsJsonArray(response);
                        data.setValue(object.get(0).getAsJsonObject());
                        notifySelectedVariant();
                    }
                },
                dataController::onFailure
        );
    }

    private void notifySelectedVariant() {

        JsonObject selectedVariantValue = selectedVariant.getValue();
        if (selectedVariantValue == null) return;

        String id = selectedVariantValue.get("id").getAsString();
        JsonObject newVariant = data.getValue().get("variants").getAsJsonObject().get(id).getAsJsonObject();
        selectedVariant.setValue(newVariant);
    }

    public LiveData<Boolean> isFavorite() {

        TwinLiveData<JsonObject, JsonObject> twinLiveData = new TwinLiveData<>(data, selectedVariant);

        return Transformations.switchMap(twinLiveData, input -> {
            JsonObject item = input.second;

            if (item == null) {
                item = input.first;
            }
            try {
                isFavorite.postValue(item.get("is_in_wishlist").getAsBoolean());
            } catch (Exception e) {
                Log.e("addToWish", e.getMessage());
                isFavorite.postValue(false);
            }
            return isFavorite;
        });
    }

    public LiveData<String> getDescription() {

        TwinLiveData<JsonObject, JsonObject> twinLiveData = new TwinLiveData<>(data, selectedVariant);

        return Transformations.map(twinLiveData, input -> {
            try {
                if (input.second != null && JsonHelper.has(input.second, "text") && JsonHelper.isNotEmptyNorNull(input.second.get("text")))
                    return input.second.get("text").getAsString();

                else if (input.first != null && JsonHelper.has(input.first, "text") && JsonHelper.isNotEmptyNorNull(input.first.get("text")))
                    return input.first.get("text").getAsString();

                else return "";

            } catch (Exception e) {
                Log.e("description", e.getMessage());
                return "";
            }
        });
    }

    public boolean isLogin() {
        return repository.isLogin();
    }

    public void addToWaitList() {
        JsonObject selectedVariantValue = selectedVariant.getValue();

        JsonObject item = data.getValue();
        if (selectedVariantValue != null) {
            item = selectedVariantValue;
        }

        JsonObject addToWaitListAction = item
                .get("product_actions").getAsJsonObject()
                .get("addmetowaitlist").getAsJsonObject();

        requestData(
                repository.addToWaitList(addToWaitListAction.get("link").getAsString(), addToWaitListAction.get("params").getAsJsonObject())
                , response -> {
                    dataController.onSuccess(response);
                    if (response.isSuccessful()) {
                        String message = dataController.getMessageOrEmpty(response.body());
                        waitListResponse.postValue(message);
                    } else waitListResponse.postValue("");
                },
                throwable -> {
                    dataController.onFailure(throwable);
                    waitListResponse.postValue("");
                }
        );
    }

    public LiveData<String> getAddToWaitListResponse() {
        return waitListResponse;
    }

    public boolean hasCart() {
        return hasCart;
    }

    public String getLanguage() {
        return repository.getLanguage();
    }

    public JsonArray getAllCfs() {
        return allCfs;
    }

    public void saveReturnAction(JsonObject returnAction) {
        repository.saveReturnAction(returnAction);
    }

    public JsonElement getRawData() {
        return rawData;
    }

    public void setRawData(JsonObject object) {
        this.rawData = object;
    }

    public void setParameterItemSelectionHolder(int outerPosition, int innerPosition) {
        this.parameterItemSelectionHolder.put(outerPosition, innerPosition);
    }

    public Map<Integer, Integer> getParameterItemSelectionHolder() {
        return parameterItemSelectionHolder;
    }

    public JsonObject getAddCommentObj() {
        return addCommentObj;
    }

    public void saveCurrentPageAsReturn() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("link", path);
        preferenceManager.saveReturn(jsonObject);
    }
}
