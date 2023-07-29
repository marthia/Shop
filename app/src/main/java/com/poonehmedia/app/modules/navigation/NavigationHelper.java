package com.poonehmedia.app.modules.navigation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.BuildConfig;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.AndroidUtils;

import org.acra.ACRA;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

@Singleton
public class NavigationHelper {

    private final String TAG = getClass().getSimpleName();
    private final Map<String, Integer> destinations = new HashMap<String, Integer>() {{
        put("ShopProducts", R.id.products_graph);
        put("ShopProduct", R.id.product_graph);
        put("ShopCategories", R.id.shop_categories_fragment);
        put("ShopManufacturer", R.id.shop_manufacturer);
        put("ShopUserAddresses", R.id.address_graph);
        put("ShopUserAddress", R.id.address_add_edit_fragment);
        put("ShopUserPanel", R.id.profile_fragment);
        put("ShopUserProfile", R.id.profile_edit_fragment);
        put("ShopUserMobileEdit", R.id.reset_phone_graph);
        put("ShopOrders", R.id.orders_fragment);
        put("ShopOrder", R.id.order_fragment);
        put("ShopCheckout", R.id.shop_checkout_fragment);
        put("ShopAffiliatePanel", R.id.affiliate_graph);
        put("ShopAffiliateClicks", R.id.affiliate_click_fragment);
        put("ShopAffiliateLeads", R.id.affiliate_click_fragment);
        put("ShopAffiliateSales", R.id.affiliate_click_fragment);
        put("ShopCheckoutEnd", R.id.checkout_end);
        put("ShopFavourite", R.id.favourite_fragment);

        put("JComments", R.id.comments_fragment);
        put("Modules", R.id.home_fragment);
        put("Item", R.id.item_fragment);
        put("Items", R.id.items_fragment);
        put("Login", R.id.login_graph);
        put("Register", R.id.fragment_sign_up);
        put("CustomerClubPanel", R.id.customer_club_fragment);

        put("RppSignInUp", R.id.sign_in_up_fragment);
        put("RppSignInUpLogin", R.id.login_register_fragment);
        put("RppSignInUpRegister", R.id.login_register_fragment);
        put("RppSignInUpConfirm", R.id.validation_fragment);
        put("RppEditProfile", R.id.new_edit_profile_fragment);
        put("RppEditProfileConfirm", R.id.validation_fragment);
        put("RppSignInUpReset", R.id.fragment_reset_password);
        put("RppSignInUpComplete", R.id.validation_fragment);
    }};
    private final NavigationApi api;
    private final RoutePersistence routePersistence;
    private final RestUtils restUtils;
    private final DataController dataController;
    private final Context context;
    private Disposable disposable;

    @Inject
    public NavigationHelper(NavigationApi api,
                            DataController dataController,
                            RoutePersistence routePersistence,
                            RestUtils restUtils,
                            @ApplicationContext Context context
    ) {
        this.api = api;
        this.dataController = dataController;
        this.routePersistence = routePersistence;
        this.restUtils = restUtils;
        this.context = context;
    }

    public int find(String destination) {
        Integer integer = destinations.get(destination);
        if (integer != null) return integer;
        else return 0;
    }

    /**
     * @param root  view to retrieve navController from.
     * @param link  the request link
     * @param state callback interface for updating the UI based on navigation state: error, success, loading
     */
    public void navigate(View root, String link, INavigationState state) {
        try {
            NavController navController = Navigation.findNavController(root);
            getDataAndNavigate(navController, link, null, false, state);
        } catch (Exception e) {
            Log.e(TAG, "Could not Naviate: " + e.getMessage());
        }
    }

    /**
     * @param activity depends on activity because we use NavController.findNavController() although
     *                 there is other ways of retrieving navController which could need either fragment or activity
     *                 but we didn't use that: could be a future refactoring
     * @param link     the request link
     * @param state    callback interface for updating the UI based on navigation state: error, success, loading
     */
    public void navigate(FragmentActivity activity, String link, INavigationState state) {
        try {
            NavController navController = Navigation.findNavController(activity, R.id.main_nav_fragment);
            getDataAndNavigate(navController, link, null, false, state);
        } catch (Exception e) {
            Log.e(TAG, "Could not Naviate: " + e.getMessage());
        }
    }

    /**
     * navigate overload to support post requests
     *
     * @param root     current fragment root view
     * @param link     the request link
     * @param postBody JsonObject request body
     * @param state    callback interface for updating the UI based on navigation state: error, success, loading
     */
    public void navigate(View root, String link, JsonObject postBody, INavigationState state) {
        try {
            NavController navController = Navigation.findNavController(root);
            getDataAndNavigate(navController, link, postBody, false, state);
        } catch (Exception e) {
            Log.e(TAG, "Could not Naviate: " + e.getMessage());
        }
    }

    public void popUpTo(View root, String link, INavigationState state) {
        try {
            NavController navController = Navigation.findNavController(root);
            getDataAndNavigate(navController, link, null, true, state);
        } catch (Exception e) {
            Log.e(TAG, "Could not Naviate: " + e.getMessage());
        }
    }

    // TODO PAGED LISTS ARE TREATED LIKE NON-PAGED URLS:
    //  CHECK IF NEED TO ATTACH SPECIAL PARAMS FOR PAGING URLS Like `limit` or `limitstart`
    private void getDataAndNavigate(NavController controller, String link, JsonObject postBody, boolean isPop, INavigationState state) {
        try {
            if (link == null || link.isEmpty())
                return;

            // support full path links
            NavigationUrl navigationUrl = resolveLink(link);

            if (navigationUrl.isExternal()) {
                openUrl(navigationUrl.getLink());
                return;
            }

            Single<Response<JsonElement>> request = resolveRequest(navigationUrl, postBody);

            // start showing progress
            state.onNext(new NavigationState.Loading());

            // resolve url
            disposable = request
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(response -> {
                        dataController.onSuccess(response);

                        NavigationArgs navigationArgs = extractCurrentAction(
                                response.body(),
                                navigationUrl.getLink()
                        );

                        if (navigateTo(controller, navigationArgs, isPop))
                            state.onNext(new NavigationState.Success(navigationArgs));
                        else
                            state.onNext(new NavigationState.Error(context.getString(R.string.error_navigating)));

                    }, throwable -> {
                        dataController.onFailure(throwable);
                        state.onNext(new NavigationState.Error(context.getString(R.string.error_navigating)));
                    });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("An error occurred while fetching next destination data in (NavigationHelper). ", e));
        }
    }

    /**
     * @param response server response body
     * @param link     the requested link to include in NavigationArgs
     * @return NavigationArgs containing `title`, `link`, `action` and `data`
     */
    private NavigationArgs extractCurrentAction(JsonElement response, String link) {
        try {
            if (!response.isJsonObject())
                return null;

            JsonObject body = response.getAsJsonObject();
            if (!JsonHelper.has(body, "data"))
                return null;

            JsonObject data = body.get("data").getAsJsonObject();

            if (!JsonHelper.has(data, "info"))
                return null;

            JsonObject info = data.get("info").getAsJsonObject();
            if (!JsonHelper.has(info, "currentAction"))
                return null;

            JsonObject currentAction;

            if (info.has("currentAction"))
                currentAction = info.get("currentAction").getAsJsonObject();

            else return null; // abort navigating because without currentAction we're lost

            String title = "";
            if (JsonHelper.has(info, "title") && JsonHelper.isNotEmptyNorNull(info.get("title"))) {
                title = info.get("title").getAsString();
            }

            int pageSize = -1;
            String pageStartKey = "limitstart";

            if (JsonHelper.has(info, "limit")) pageSize = info.get("limit").getAsInt();

            if (JsonHelper.has(info, "ls_key")) pageStartKey = info.get("ls_key").getAsString();

            return new NavigationArgs(
                    title,
                    link,
                    currentAction,
                    pageSize,
                    pageStartKey,
                    body
            );

        } catch (Exception e) {
            Log.e("actionInfo", "NO ACTION INFO PROVIDED");
            ACRA.getErrorReporter().handleException(new CrashReportException("An error occurred while extracting currentAction for link: " + link, e));
        }
        return null;
    }

    /**
     * @param link a request url
     * @return {@link NavigationUrl} with replaced absolute urls to relative
     * @author Marthia
     */
    private NavigationUrl resolveLink(String link) {
        NavigationUrl navigationUrl = new NavigationUrl();

        // handle external urls and return
        String forceExternalUrl = "ext.url://";
        if (link.startsWith(forceExternalUrl)) {
            navigationUrl.setLink(link.replace(forceExternalUrl, ""));
            navigationUrl.setExternal(true);
            return navigationUrl;
        } else if (link.startsWith("tel:")) {
            navigationUrl.setLink(link);
            navigationUrl.setPhone(true);
            navigationUrl.setExternal(true);
            return navigationUrl;
        } else if (link.startsWith("mailto:")) {
            navigationUrl.setLink(link);
            navigationUrl.setMail(true);
            navigationUrl.setExternal(true);
            return navigationUrl;
        }

        navigationUrl.setLink(link);
        try {
            URL _URL = new URL(link);
            URL compare = new URL(BuildConfig.baseUrl);

            if (_URL.getHost().replace("www.", "").equals(compare.getHost().replace("www.", "")))
                navigationUrl.setLink(_URL.getPath()); // replace full path
            else {
                navigationUrl.setExternal(true);
            }
        } catch (MalformedURLException e) {
            // ignore
        }

        return navigationUrl;
    }

    /**
     * @param navigationUrl containing url and the type of url either external or internal
     * @param postBody
     * @return RxJava `Single` as retrofit request
     * @author Marthia
     */
    private Single<Response<JsonElement>> resolveRequest(NavigationUrl navigationUrl, JsonObject postBody) {

        Single<Response<JsonElement>> request;

        String url = restUtils.resolveUrl(navigationUrl.getLink());
        if (postBody == null)
            request = api.getFullPath(url);
        else
            request = api.postFullPath(url, postBody);

        return request;
    }


    /**
     * this method is used to navigate between fragments using the Navigation component.
     * the arguments associated with the destination is persisted in a singleton injectable class {@link RoutePersistence}
     *
     * @param navController navController
     * @param args          NavigationArgs
     * @param isPop
     * @return boolean of either success or failure used to show and dismiss the loading dialog
     * @author Marthia
     */
    private boolean navigateTo(NavController navController, NavigationArgs args, boolean isPop) {
        try {
            if (args == null) {
                return false;
            }

            // log all arguments for better debugging
            Log.i("navigator", args.toString());

            JsonObject action = args.getAction();

            int destinationId = find(action.get("name").getAsString());

            Bundle bundle = new Bundle();
            String accessingKey = UUID.randomUUID().toString();
            routePersistence.addRoute(accessingKey, args);
            bundle.putString("key", accessingKey);
            bundle.putString("link", args.getLink()); // used for tracking bottom navigation item selection

            NavOptions.Builder builder = new NavOptions.Builder();
            builder.setEnterAnim(R.anim.slide_in_right);
            builder.setExitAnim(R.anim.slide_out_left);
            builder.setPopEnterAnim(R.anim.slide_in_left);
            builder.setPopExitAnim(R.anim.slide_out_right);
            if (isPop)
                builder.setPopUpTo(destinationId, true);

            navController.navigate(destinationId, bundle, builder.build());
            // make sure it's not null
            if (disposable != null)
                disposable.dispose();

            return true;
        } catch (Exception e) {
            Log.e("navigation", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error while Navigating to : " + args.toString(), e));
            return false;
        }
    }

    /**
     * this method is used to open provided urls in OS's default browser
     *
     * @param link the requested url
     * @author Marthia
     */
    public void openUrl(String link) {
        try {
            Uri uri = Uri.parse(link);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not openUrl in (NavigationHelper) for : " + link, e));
        }
    }

    /**
     * finishes the current activity after all transitions. this is preferred over {@code System.exit(0)}
     * because the latter shuts the app immediately as if something went wrong.
     *
     * @param activity to be finished
     */
    public void closeApp(FragmentActivity activity) {
        activity.finishAfterTransition();
    }

    /**
     * @param activity     used to find navController
     * @param direction    {@link NavDirections} for offline navigations
     * @param shouldOffset used to manually fix the dispositioned layouts
     * @author Marthia
     * @see NavigationHelper#navigate(FragmentActivity, String, INavigationState)
     */
    public void intrinsicNavigate(FragmentActivity activity, NavDirections direction, boolean shouldOffset) {
        try {
            NavController navController = Navigation.findNavController(activity, R.id.main_nav_fragment);

            activity.findViewById(R.id.bottom_nav).setVisibility(View.GONE);
            handleBottomMargin(activity, shouldOffset);

            activity.findViewById(R.id.logo).setVisibility(View.GONE);

            navController.navigate(direction);

        } catch (Exception e) {
            Log.e("navigation", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error navigating to destination " + direction.toString(), e));
        }
    }

    /**
     * @param activity the main activity hosting NavHostFragment
     * @param v        the boolean value stating the visibility of bottomNav
     *                 <p>
     *                 clears the extra empty space in bottom of the screen when the
     *                 bottomNav's visibility is gone and reclaim when visible again
     * @author Marthia
     */
    public void handleBottomMargin(FragmentActivity activity, boolean v) {
        try {
            int dp = 56;

            View view = activity.findViewById(R.id.main_nav_fragment);
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            if (!v)
                lp.setMargins(0, 0, 0, 0);
            else lp.setMargins(0, 0, 0, (int) AndroidUtils.getPixels(dp, activity));
            view.setLayoutParams(lp);
        } catch (Exception e) {
            Log.e("margin", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error Occurred while handling page bottom margins", e));
        }
    }

    /**
     * convenient alternative to {@link NavigationHelper#intrinsicNavigate(FragmentActivity, NavDirections, boolean)}
     * to clear stack after the navigation
     *
     * @param activity  used to find navController
     * @param direction {@link NavDirections} used for finding destination
     * @author Marthia
     */
    public void navigateAndClearBackStack(FragmentActivity activity, NavDirections direction) {
        try {
            NavController navController = Navigation.findNavController(activity, R.id.main_nav_fragment);
            navController.navigate(direction);

        } catch (Exception e) {
            Log.e("navigation", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("navigateAndClearBackStack", e));
        }

    }

    public void clearBackStack(FragmentActivity activity) {
        try {
            NavController navController = Navigation.findNavController(activity, R.id.main_nav_fragment);
            navController.popBackStack(R.id.base_fragment, false);

        } catch (Exception e) {
            Log.e("navigation", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("navigateAndClearBackStack", e));
        }
    }
}
