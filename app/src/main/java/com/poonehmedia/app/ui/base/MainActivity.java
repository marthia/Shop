package com.poonehmedia.app.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.najva.sdk.Najva;
import com.poonehmedia.app.BuildConfig;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.components.fontAwesome.FontDrawable;
import com.poonehmedia.app.data.model.RequestException;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.databinding.ActivityMainBinding;
import com.poonehmedia.app.modules.navigation.NavigationHelper;
import com.poonehmedia.app.modules.navigation.NavigationState;
import com.poonehmedia.app.ui.ProgressDialog;
import com.poonehmedia.app.ui.forceupdate.UpdateDialog;
import com.poonehmedia.app.ui.modules.ServerNoticesDialog;
import com.poonehmedia.app.util.base.DeviceInfoManager;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.LocaleHelper;
import com.poonehmedia.app.util.ui.UiComponents;

import org.acra.ACRA;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener {
    private static final long SPLASH_DISMISS_DELAY = 1500; // (ms) min splash show time
    private final String TAG = getClass().getSimpleName();
    private final Map<Integer, String> bottomNavItemsDestinationMatches = new HashMap<>();
    private final Map<Integer, String> drawerMenuItemsDestinationMatches = new HashMap<>();
    private final Map<Object, String> menuItemTag = new HashMap<>();

    @Inject
    public NavigationHelper navigator;
    @Inject
    public DeviceInfoManager deviceInfoManager;
    @Inject
    public PreferenceManager preferenceManager;

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private AppBarConfiguration mAppBarConfiguration;
    private JsonObject defaultDestination;
    private NavController navController;
    private boolean isDoubleBackPress = false;
    private ProgressDialog progress;
    private boolean hasUpdate = false;
    private SettingsViewModel settingsViewModel;
    private String deepLink = null;
    private String returnLink = null;
    private Intent currentIntent;
    private boolean isNewIntent = false;
    private boolean hasFollowUpAction = false;
    private boolean hasDrawer = false;
    private boolean wasDeepLink = false;
    private boolean shouldNavigateToTopLevelDestination = false;
    private String currentPageLink;
    private Consumer<Object> backPressOverridden;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentIntent = getIntent();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // fix orientation for devices below Nougat
        checkWindowOrientation();

        // check if coming from a deep link : exclude return action because at this point we know that it
        // must not navigate to return action; it must've been handled or cancelled.
        preferenceManager.clearReturn();
        resolveDeepLinks();

        // init view and Main Activity initializations
        setContent();

        subscribeResponses();

        viewModel.subscribeDefaultResponses();

        // simulate first run splash (show splash dialog)
        initializeApp();
    }

    private void subscribeResponses() {

        settingsViewModel.getLoadingProgress().observe(this, progress -> {
            if (progress == 3) {
                if (!hasUpdate) {
                    dismissSplash();
                    constructAppGraph();
                }
            }
        });

        viewModel.getFailureResponse().observe(this, s -> {
            if (s != null) UiComponents.showSnack(this, s);
        });

        viewModel.getException().observe(this, this::navigateToErrorFragment);

        viewModel.getInvalidateSession().observe(this, aBoolean -> {
            if (!aBoolean) { // logout
                viewModel.logout();
            }
            navigator.clearBackStack(this);
            resolveDeepLinks();
            initializeApp();
            resetOverrideBackPress();
        });
    }

    private void setContent() {
        // progress dialog shown while navigating
        progress = new ProgressDialog(this, R.style.Widget_App_FullScreenDialog);

        // set content
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);
        setSupportActionBar(binding.mainToolbar);

        binding.setSplashText(getString(R.string.splash_branding_text));

        navController = Navigation.findNavController(this, R.id.main_nav_fragment);
    }

    public void resolveDeepLinks() {
        // handle opening a link from webView or external browser in app
        Uri data = currentIntent.getData();
        if (data != null) {
            String url = data.toString();
            Log.i("deepLink", url);
            try {
                // URIs with redirect parameter are openable with app only: because they are not standard URLs
                if (data.getQueryParameter("redirect") != null) {
                    url = data.getQueryParameter("redirect");
                }

                deepLink = viewModel.replaceIfFullPath(url, BuildConfig.baseUrl);

                Log.i("deepLinkVerified", deepLink);
            } catch (Exception e) {
                e.printStackTrace();
                ACRA.getErrorReporter().handleException(new CrashReportException("Error while resolving deepLinks in (MainActivity). intent data: " + data, e));
            }
        } // handle returns (return point where user left the app for login or return from payment site)
        else if (JsonHelper.isNotEmptyNorNull(preferenceManager.getReturn())) {
            returnLink = preferenceManager.getReturn().get("link").getAsString();
            hasFollowUpAction = true;
        }
        // handle notification
        try {
            String json = currentIntent.getStringExtra(Najva.NOTIFICATION_JSON);
            if (json != null) {
                Log.i("DeepLink", json);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                deepLink = obj.get("redirect").getAsString();
            }
        } catch (Exception e) {
            Log.e("Najva metaData", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error while resolving Notification data", e));
        }
    }

    private void checkWindowOrientation() {

        if (Build.VERSION.SDK_INT >= 26)
            return;
        String language = new PreferenceManager(this).getLanguage();
        int orientation = language.equalsIgnoreCase("fa-ir") ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR;
        getWindow().getDecorView().setLayoutDirection(orientation);
    }

    // simulate a splash activity
    public void initializeApp() {
        showSplash();

        if (isConnected()) {
            settingsViewModel.init();
            settingsViewModel.getHasError().observe(this, aBoolean -> {
                if (aBoolean)
                    binding.errorConnection.setVisibility(View.VISIBLE);
            });

            settingsViewModel.getForceUpdate().observe(this, appParams -> {
                hasUpdate = true;
                UpdateDialog updateDialog = new UpdateDialog(
                        this,
                        navigator,
                        deviceInfoManager,
                        appParams
                );

                updateDialog.setOnDismissListener(dialog -> {
                    dismissSplash();
                    constructAppGraph();
                    settingsViewModel.setHasHandled(true);
                });

                updateDialog.show();
            });
        } else {
            binding.loadingProgress.setVisibility(View.GONE);
            binding.errorConnection.setVisibility(View.VISIBLE);
        }
    }

    private void showSplash() {
        binding.splashRoot.setVisibility(View.VISIBLE);
        binding.drawerLayout.setVisibility(View.GONE);
    }

    private void dismissSplash() {
        new Handler().postDelayed(
                () -> {
                    binding.splashRoot.setVisibility(View.GONE);
                    binding.drawerLayout.setVisibility(View.VISIBLE);
                },
                SPLASH_DISMISS_DELAY
        );
    }

    private void constructAppGraph() {
        retrieveMenus();
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void retrieveMenus() {

        JsonArray languageItems = viewModel.getAllLanguages();
        if (languageItems != null && languageItems.size() != 0) {
            if (languageItems.size() > 1) showLanguageSelectDialog(); // TODO SHOULD BE IMPLEMENTED
            else
                viewModel.setLanguage(languageItems.get(0).getAsJsonObject().get("tag").getAsString());
        }

        JsonArray menuItems = viewModel.getAllBottomNavMenus();
        if (menuItems != null && menuItems.size() > 0) {

            defaultDestination = viewModel.findDefaultFragmentFromMenuList(menuItems);

            if (defaultDestination != null) {
                setUpBottomNavigation();
                createBottomNavMenu(menuItems);
            } else {
                String message = getString(R.string.error_connection);
                UiComponents.showSnack(this, message);
            }
        }

//        viewModel.fetchServerNoticesData();
//
//        viewModel.getCustomizedServerNotices().observe(this, content -> {
//            if (JsonHelper.isNotEmptyNorNull(content))
//                showAutoStartDialog(content);
//        });

    }

    private void navigateToErrorFragment(RequestException requestException) {
        NavController navController = Navigation.findNavController(this, R.id.main_nav_fragment);
        binding.bottomNav.setVisibility(View.GONE);
        binding.logo.setVisibility(View.GONE);
        navigator.handleBottomMargin(this, true);
        navController.navigate(BaseFragmentDirections.actionGoToException());
    }

    private void fetchDrawerMenuItems() {
        JsonArray drawerMenus = viewModel.getDrawerMenus();
        if (drawerMenus != null && drawerMenus.size() > 0) {
            setUpNavigationDrawer();
            createDrawerMenu(drawerMenus);
        }
    }

    private void setUpBottomNavigation() {
        // Trigger for onNavDestinationSelected
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        // find the default fragment
//        topLevelDestination = navigator.find(
//                defaultDestination.get("action").getAsJsonObject().get("name").getAsString());

        // navigate to default destination and then obtain the action name for resolving `topLevelDestination` which is essential
        // for setting up NavigationDrawer
        if (deepLink != null || returnLink != null) {
            handleDeepLinks();
            shouldNavigateToTopLevelDestination = true;
        } else {
            navigateNormal(defaultDestination.get("link").getAsString(), this::followNavigationSequence);
        }

        // handle bottom navigation item selection
        binding.bottomNav.setOnNavigationItemSelectedListener(this::onBottomNavItemSelected);
    }

    private void followNavigationSequence() {
        fetchDrawerMenuItems(); // called here because we need topLevelDestination for setting up drawer
    }

    private void showAutoStartDialog(JsonObject content) {

        ServerNoticesDialog dialog = new ServerNoticesDialog(this,
                content,
                viewModel.getDataController(),
                viewModel.getLanguage(),
                link -> navigateNormal(link, null));

        dialog.show();
    }

    /**
     * registered return actions or notifications with specific redirect payload, are handled here
     *
     * @author Marthia
     */
    private void handleDeepLinks() {
        Runnable future = null;
        if (!isNewIntent) {
            future = this::followNavigationSequence;
            isNewIntent = false;
        }
        if (returnLink != null) {
            navigateNormal(returnLink, future);
            viewModel.clearReturn();
            wasDeepLink = true;
        } else {
            if (deepLink != null) {
                navigateNormal(deepLink, future);
                deepLink = null;
                wasDeepLink = true;
            }
        }
    }

    private void navigateNormal(String link, Runnable onSuccessOptional) {
        navigator.navigate(this,
                link,
                state -> {
                    if (state instanceof NavigationState.Loading) navProgress(true);
                    else if (state instanceof NavigationState.Error) navProgress(false);
                    else if (state instanceof NavigationState.Success) {
                        navProgress(false);
                        if (onSuccessOptional != null)
                            onSuccessOptional.run();
                    }
                }
        );
    }

    private void setUpNavigationDrawer() {


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        binding.mainToolbar.setContentInsetStartWithNavigation(0);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mAppBarConfiguration = new AppBarConfiguration.Builder()
                .setOpenableLayout(binding.drawerLayout)
                .build();

        // Show and Manage the Drawer and Back Icon
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        binding.navigationView.setNavigationItemSelectedListener(this::onDrawerMenuItemSelected);

        navController.addOnDestinationChangedListener(this);
    }

    /**
     * Provide bottom navigation menu selection on destination change
     *
     * @param arguments {@link Bundle}
     */
    private void selectBottomNavigationItem(Bundle arguments) {
        Menu menus = binding.bottomNav.getMenu();
        currentPageLink = (String) arguments.get("link");
        for (int i = 0; i < menus.size(); i++) {
            if (bottomNavItemsDestinationMatches.get(i).equals(currentPageLink)) {
                menus.getItem(i).setChecked(true);
                break;
            }
        }
    }

    /**
     * Convenient method to create navigation drawer menu based on the menu list retrieved from api
     */
    private void createDrawerMenu(JsonArray items) {
        Menu menus = binding.navigationView.getMenu();

        // make sure the menus get replaced, not duplicated
        if (menus.hasVisibleItems()) menus.clear();

        for (int i = 0; i < items.size(); i++) {
            JsonObject obj = items.get(i).getAsJsonObject();

            drawerMenuItemsDestinationMatches.put(i, obj.get("link").getAsString());

            String actionName = obj.get("action").getAsJsonObject().get("name").getAsString();

            // exception because it has no destination
            if (actionName.equals("header")) {
                View oldHeader = binding.navigationView.getHeaderView(0);

                if (oldHeader != null) // remove old headers
                    binding.navigationView.removeHeaderView(oldHeader);

                View headerView = LayoutInflater.from(this).inflate(R.layout.nav_header, null);
                binding.navigationView.addHeaderView(headerView);
                continue;
            }

            // exception because it must be taken out of the drawer menu and shown at the bottom.
            if (actionName.equals("copyright")) {
                bindCopyrightMenuItem(obj);
                continue;
            }

            String icon = obj.get("icon").getAsString();
            MenuItem menuItem =
                    menus.add(Menu.NONE, i, Menu.NONE, obj.get("title").getAsString());

            menuItem.setIcon(
                    getMenuDrawable(icon, 22, getResources()
                                    .getColor(R.color.color_on_surface_emphasis_high),
                            true)
            );
            menuItemTag.put(menuItem, actionName);
        }

        // add static items
        MenuItem menuItem =
                menus.add(Menu.CATEGORY_SECONDARY, items.size(), Menu.NONE, getString(R.string.enable_debug));
        menuItem.setActionView(R.layout.drawer_debug_switch_item);
        menuItem.setIcon(
                getMenuDrawable("&#xf188;", 22, getResources()
                                .getColor(R.color.color_on_surface_emphasis_high),
                        false)
        );

        View actionView = menuItem.getActionView();
        if (actionView != null) {
            SwitchMaterial switchDebug = actionView.findViewById(R.id.enable_debug_switch);
            switchDebug.setChecked(preferenceManager.getDebuggingState());
            switchDebug.setOnCheckedChangeListener((buttonView, isChecked) -> {
                preferenceManager.setDebuggingState(isChecked);
            });
        }
        menuItemTag.put(menuItem, "debug");

        // this is a work around to make the last items in navigation drawer visible because of the footer
        MenuItem emptyMenu =
                menus.add(Menu.CATEGORY_SECONDARY, items.size() + 1, Menu.NONE, "");
        menuItemTag.put(emptyMenu, "empty");
    }

    /**
     * @param icon      String value of icon as stated in font awesome docs. (e.g. `&#xf004;` ).
     * @param iconColor color int value of the icon
     * @param iconSize  size of the icon
     * @param isSolid   specifying the category of which the icon belongs to. brand is never used in this project so always passed false.
     *                  false means the icon is regular, true solid.
     *                  Convenient method to create menu drawable.
     * @return This returns a custom drawable {@link FontDrawable} which supports `font-awesome`.
     * @author Marthia
     */
    private FontDrawable getMenuDrawable(String icon, int iconSize, int iconColor, boolean isSolid) {
        FontDrawable drawable = new FontDrawable(this, icon, isSolid, false);

        drawable.setTextColor(iconColor);
        drawable.setTextSize(iconSize);

        return drawable;
    }

    private void bindCopyrightMenuItem(JsonObject object) {
        binding.copyWrite.setVisibility(View.VISIBLE);
        binding.copyWrite.setText(object.get("title").getAsString() + getAppVersionString());
        binding.copyWrite.setOnClickListener(v -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            navigateNormal(object.get("link").getAsString(), null);
        });
    }

    /**
     * dismisses the navigation progress dialog with a minimum delay to avoid initial navigation animation
     * which is mainly a ux strategy.
     * Note that it must be called from ui thread otherwise it crashes.
     *
     * @param b true to make dialog visible , false otherwise
     */
    public void navProgress(boolean b) {
        if (!isDestroyed() || progress.isShowing()) {
            Handler handler = new Handler(Looper.getMainLooper());
            if (b) {
                handler.post(() -> progress.show());
            } else handler.postDelayed(() -> progress.dismiss(), 500);
        }
    }

    private String getAppVersionString() {
        return " ( " + getString(R.string.app_version_lable) + " " + deviceInfoManager.getAppVersion() + " )";
    }

    /**
     * Create bottom navigation menu based on menu items retrieved from api. the jsonArray containing server menus
     * is defied globally for controlling selection. see {@code MainActivity#bottomNavMenuItems} .
     *
     * @param bottomNavMenuItems filtered bottom navigation menus retrieved from server
     */
    private void createBottomNavMenu(JsonArray bottomNavMenuItems) {

        // used to keep track of cart menu to show count badge
        int cartMenuId = -1;

        Menu menu = binding.bottomNav.getMenu();

        // make sure the menus get replaced, not duplicated
        if (menu.hasVisibleItems()) menu.clear();

        ColorStateList colorStateList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            colorStateList = getResources().getColorStateList(R.color.navigation_bar_txt_color, null);
        else {
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_checked}, // checked
                    new int[]{-android.R.attr.state_checked} // unchecked
            };

            int[] colors = new int[]{
                    getResources().getColor(R.color.color_on_surface_emphasis_high),
                    getResources().getColor(R.color.color_on_surface_emphasis_low),
            };

            colorStateList = new ColorStateList(states, colors);

        }

        for (int i = 0; i < bottomNavMenuItems.size(); i++) {
            JsonObject obj = bottomNavMenuItems.get(i).getAsJsonObject();

            bottomNavItemsDestinationMatches.put(i, obj.get("link").getAsString());

            String icon = obj.get("icon").getAsString();

            FontDrawable drawable = new FontDrawable(this, icon, true, false);
            drawable.setTextColor(colorStateList);
            drawable.setTextSize(22);

            MenuItem menuItem = menu.add(Menu.NONE, i, Menu.NONE, obj.get("title").getAsString());
            // save the id of shopping list for setting badge on it
            menuItem.setIcon(drawable);

            String actionName = obj.get("action").getAsJsonObject().get("name").getAsString();
            if (actionName.equals("ShopCheckout")) cartMenuId = i;
        }

        int finalCartMenuId = cartMenuId;

        // subscribe cart badge count
        viewModel.getCartBadgeCount().observe(this, integer -> {
            if (finalCartMenuId != -1) {
                BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(finalCartMenuId);
                badge.setBadgeTextColor(Color.WHITE);
                badge.setNumber(integer);
                badge.setVisible(integer != 0);
            }
        });

        // subscribe force update
        viewModel.getForceUpdate().observe(this, appParams -> {
            UpdateDialog updateDialog = new UpdateDialog(
                    this,
                    navigator,
                    deviceInfoManager,
                    appParams
            );

            updateDialog.setOnDismissListener(dialog -> viewModel.setHasForceUpdateHandled(true));
            updateDialog.show();
        });
    }

    /**
     * menu item ids were assigned in a loop with position index at creation phase. so another loop can simulate xml based id retrieval (e.g: {@code R.id.menu_home}).
     */
    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        String link = bottomNavItemsDestinationMatches.get(item.getItemId());

        if (link.equals(currentPageLink))
            return true;

        navigateNormal(link, null);
        return true;
    }

    private boolean onDrawerMenuItemSelected(@NonNull MenuItem item) {
        if (menuItemTag.get(item).equals("exit")) {
            navigator.closeApp(this);
            return true;
        }
        binding.drawerLayout.closeDrawers();
        String link = drawerMenuItemsDestinationMatches.get(item.getItemId());
        navigateNormal(link, null);
        return true;
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        selectBottomNavigationItem(arguments);
        ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(true, true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (backPressOverridden == null || !handleOverriddenBackAction()) {
            if (item.getItemId() == android.R.id.home) {
                if (hasDrawer) {
                    binding.drawerLayout.open();
                    return true;
                } else if ((wasDeepLink && shouldNavigateToTopLevelDestination) || hasFollowUpAction) {
                    navController.popBackStack();
                    navigateToTopLevelDestinationIfNotInBackStack();
                    shouldNavigateToTopLevelDestination = false;
                    wasDeepLink = false;
                    hasFollowUpAction = false;
                }
                if (!isTopLevelDestination()) {
                    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                            || super.onSupportNavigateUp();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setHasDrawer(boolean h) {
        hasDrawer = h;
    }

    private boolean isTopLevelDestination() {
        try {
            NavBackStackEntry last = navController.getPreviousBackStackEntry();
            boolean isAtLast = last != null && ((FragmentNavigator.Destination) last.getDestination()).getClassName().equals(BaseFragment.class.getName());
            boolean isTopLevelDestination = currentPageLink != null && currentPageLink.replace("/", "").equalsIgnoreCase(defaultDestination.get("link").getAsString().replace("/", ""));
            return isTopLevelDestination || isAtLast;
        } catch (Exception e) {
            return false;
        }
    }

    private void navigateToTopLevelDestinationIfNotInBackStack() {
        for (int id : bottomNavItemsDestinationMatches.keySet()) {
            String link = bottomNavItemsDestinationMatches.get(id);
            if (link.equals(defaultDestination.get("link").getAsString())) {
                binding.bottomNav.setSelectedItemId(id);
            }
        }

    }

    private void showLanguageSelectDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_language_selection, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }

    @Override
    public void onBackPressed() {

        if (backPressOverridden == null || !handleOverriddenBackAction()) {

            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else if (isTopLevelDestination()) {

                // As BaseFragment is the start destination, calling super.onBackPressed() will cause the app to navigate to
                // it even though the desired behaviour is to exit app since topLevelDestination is our real start destination
                if (isDoubleBackPress) navigator.closeApp(this);
                else {
                    this.isDoubleBackPress = true;
                    String message = getString(R.string.double_tap_to_exit);
                    UiComponents.showSnack(this, message);
                    new Handler().postDelayed(() -> isDoubleBackPress = false, 2000);
                }

            } else super.onBackPressed();
        }
    }

    private boolean handleOverriddenBackAction() {
        if (backPressOverridden == null)
            return false;
        try {
            backPressOverridden.accept(true);
            backPressOverridden = null;
            return true;
        } catch (Exception e) {
            backPressOverridden = null;
            return false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "new intent called");
        currentIntent = intent;
        isNewIntent = true;
        resolveDeepLinks();
        handleDeepLinks();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.getLanguageAwareContext(
                newBase, new PreferenceManager(newBase).getLanguage())
        );
    }

    public void overrideBackPress(Consumer<Object> action) {
        this.backPressOverridden = action;
    }

    public void resetOverrideBackPress() {
        this.backPressOverridden = null;
    }
}
