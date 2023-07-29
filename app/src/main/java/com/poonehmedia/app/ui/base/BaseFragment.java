package com.poonehmedia.app.ui.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.components.fontAwesome.FontDrawable;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.NavigationHelper;
import com.poonehmedia.app.modules.navigation.NavigationState;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.UiComponents;

import org.acra.ACRA;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BaseFragment extends Fragment {

    @Inject
    public NavigationHelper navigator;
    @Inject
    public RoutePersistence routePersistence;
    @Inject
    public PreferenceManager preferenceManager;

    private String actionName;

    /**
     * current page link. could be used for reload or static internal navigation for shared url.
     */
    private String link;
    private boolean isOverrideMargin = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);
    }

    /**
     * @param title String title of the current fragment
     */
    public void updateTitle(String title) {
        ((Toolbar) requireActivity().findViewById(R.id.main_toolbar)).setTitle(title);
    }

    /**
     * @param graphId current graph id
     * @return {@link NavBackStackEntry} a graph scope that can be used for creating viewModels that has the same lifecycle as the graph.
     * useful when want to use a sharedViewModel across fragments and update fragments in back stack from top level fragments. because the livedatas can be observed even if the fragment is in onPause().
     * @author Marthia
     */
    protected NavBackStackEntry getGraphScope(int graphId) {
        return NavHostFragment.findNavController(this).getBackStackEntry(graphId);
    }

    /**
     * This method controls the visibility or values of common parts of every screen like title, bottomNav and ...
     *
     * @param action JsonObject action containing a fragment window settings
     * @author Marthia
     */
    public void routeController(JsonObject action) {
        if (action == null) return;

        FragmentActivity activity = requireActivity();
        // show hide bottom nav based on destinations
        try {
            // used to access the current destination's `action-name` from any fragment
            setActionName(action.get("name").getAsString());

            // change the visibility of bottomNav
            if (action.get("showbottomnav").getAsBoolean()) {
                activity.findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
                navigator.handleBottomMargin(activity, !isOverrideMargin);
            } else {
                activity.findViewById(R.id.bottom_nav).setVisibility(View.GONE);
                navigator.handleBottomMargin(activity, isOverrideMargin);
            }

            // hide title
            if (!action.get("showtitle").getAsBoolean())
                ((Toolbar) activity.findViewById(R.id.main_toolbar)).setTitle("");

            // hide subtitle
            if (!action.get("showsubtitle").getAsBoolean())
                ((Toolbar) activity.findViewById(R.id.main_toolbar)).setSubtitle("");


            // change back button to hamburger button -----
            boolean hasDrawer = action.get("hasdrawer").getAsBoolean();
            // register action click of the icon
            ((MainActivity) activity).setHasDrawer(hasDrawer);

            if (hasDrawer) // change to hamburger icon otherwise it automatically changes to back button
                ((Toolbar) activity.findViewById(R.id.main_toolbar)).setNavigationIcon(R.drawable.ic_round_menu_24);

            // show hide logo ----
            activity.findViewById(R.id.logo)
                    .setVisibility(action.get("showlogo").getAsBoolean() ? View.VISIBLE : View.GONE);

        } catch (Exception e) {
            Log.e("route", action.toString());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error Occurred while applying currentAction params" + action.toString(), e));
        }
    }

    protected void overrideMargin(boolean offsetToZero) {
        this.isOverrideMargin = offsetToZero;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NotNull MenuInflater inflater) {
        if (hasSearch())
            inflater.inflate(R.menu.base_fragmnet_menu, menu);
    }

    protected FontDrawable getDrawable(String icon, int iconSize, int iconColor, boolean isSolid) {
        FontDrawable drawable = new FontDrawable(getContext(), icon, isSolid, false);

        drawable.setTextColor(iconColor);
        drawable.setTextSize(iconSize);

        return drawable;
    }

    /**
     * @return true the current screen should show a search icon, false otherwise
     */
    private boolean hasSearch() {
        try {
            String key = (String) getArguments().get("key");
            NavigationArgs args = routePersistence.getRoute(key);
            JsonObject action = args.getAction();

            if (action != null && JsonHelper.has(action, "hassearch"))
                return action.get("hassearch").getAsBoolean();
            else return false;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        return handleSearchSelection(item);
    }

    protected boolean handleSearchSelection(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            navigator.intrinsicNavigate(requireActivity(), BaseFragmentDirections.actionGoSearchResult(""), false);
            return true;
        }
        return false;
    }

    /**
     * control the visibility of shimmer for the given base root
     *
     * @param b       true for showing shimmer, false otherwise
     * @param root    base non-shimmer layout
     * @param shimmer base shimmer layout
     * @author Marthia
     */
    public void handleShimmer(ShimmerFrameLayout shimmer, View root, boolean b) {
        if (b)
            shimmer.startShimmer();
        else
            shimmer.stopShimmer();

        shimmer.setVisibility(b ? View.VISIBLE : View.GONE);
        root.setVisibility(!b ? View.VISIBLE : View.GONE);
    }

    /**
     * Update page titles and other settings in onResume. Here we can guarantee that UI settings gets updated because with every destination change we receive the args
     * in this base class too.
     *
     * @author Marthia
     */
    @Override
    public void onResume() {
        super.onResume();

        // essential to bypass static destinations
        ((MainActivity) requireActivity()).setHasDrawer(false);

        try {

            AndroidUtils.hideKeyboardFrom(getView());
            String argsKey = (String) getArguments().get("key");

            if (argsKey == null)
                return;

            NavigationArgs args = routePersistence.getRoute(argsKey);

            if (args == null)
                return;

            if (args.getTitle() != null && !args.getTitle().isEmpty())
                updateTitle(args.getTitle());

            // if it didn't have any action we weren't here: so no need to double check
            routeController(args.getAction());

            // save current page link in case of reload or something
            setLink(args.getLink());

        } catch (Exception e) {
            Log.e("baseRoute", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error occurred while handling currentAction", e));
        }
    }

    /**
     * With every destination change we show a dialog containing a progress gif which is created and controlled by
     * {@link MainActivity} only. here we access mainActivity and request a change in progress visibility.
     * <p> this was to avoid showing the transition animations before the progress dialog dismissal.
     *
     * @author Marthia
     */
    protected void navProgress(boolean b) {
        ((MainActivity) requireActivity()).navProgress(b);
    }

    protected void handleDefaultNavigationState(NavigationState state) {
        if (state instanceof NavigationState.Loading)
            navProgress(true);
        else if (state instanceof NavigationState.Error) {
            navProgress(false);
            UiComponents.showSnack(requireActivity(), NavigationState.errorMessage);
        } else if (state instanceof NavigationState.Success)
            navProgress(false);
    }

    protected void overrideBackPress(Consumer<Object> action) {
        ((MainActivity) requireActivity()).overrideBackPress(action);
    }

    protected void resetBackAction() {
        ((MainActivity) requireActivity()).resetOverrideBackPress();
    }

    public String getActionName() {
        return actionName;
    }

    private void setActionName(String name) {
        this.actionName = name;
    }

    public String getLink() {
        return link;
    }

    private void setLink(String link) {

        this.link = link;
    }
}
