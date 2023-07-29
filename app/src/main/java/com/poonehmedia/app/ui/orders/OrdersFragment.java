package com.poonehmedia.app.ui.orders;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;
import androidx.transition.TransitionManager;

import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.SpinnerItem;
import com.poonehmedia.app.databinding.FragmentOrdersBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;
import com.poonehmedia.app.util.ui.ItemSpaceDecoration;
import com.poonehmedia.app.util.ui.MySearchView;
import com.poonehmedia.app.util.ui.UiComponents;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;

@SuppressLint("FragmentLiveDataObserve")
@AndroidEntryPoint
public class OrdersFragment extends BaseFragment {
    private static final int DELAY = 1500;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, String> filterParams = new HashMap<>();
    private final Map<String, Map<String, String>> filterController = new HashMap<>();
    private final Map<String, SpinnerItem> spinnerSelection = new HashMap<>();
    private final String orderUrl = "";
    @Inject
    public OrdersPagedListAdapter adapter;
    private Runnable delayedTask = null;
    private OrdersViewModel viewModel;
    private FragmentOrdersBinding binding;
    private String statusUrl = "";
    private String searchUrl = "";
    private GenericListAdapterImp<SpinnerItem> statusAdapter;
    /**
     * using this boolean value to avoid rebinding already created views since the recreation of the views not only
     * reduces the responsiveness of the ui but also creates an annoying flickering while updating.
     */
    private boolean isBound = false;
    private String lastQuery = "";
    private MySearchView searchView;
    private MenuItem searchItemMenu;
    private JsonObject searchObj;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (binding == null) {

            binding = FragmentOrdersBinding.inflate(inflater, container, false);

            init();
            subscribeUi();

            binding.swipe.setDistanceToTriggerSync(1000);
            binding.swipe.setOnRefreshListener(() -> fetchData(true, null));
        }
        return binding.getRoot();
    }

    private void init() {
        binding.rvStatus.addItemDecoration(new ItemSpaceDecoration(getContext(), 4));
        statusAdapter = new GenericListAdapterImp<>();
        statusAdapter.setLayoutRes(R.layout.list_item_order_status);
        statusAdapter.hasSelection(true);
        statusAdapter.subscribeCallbacks((item, position) -> reload(statusUrl, "order_status", ((SpinnerItem) item).getValue()));
        binding.rvStatus.setAdapter(statusAdapter);

        viewModel.getInfo().observe(this, info -> {
            if (!isBound) {
                try {
                    JsonObject ordersFilters = info.get("orders_filters").getAsJsonObject();
                    bindStatusChips(ordersFilters.get("order_status").getAsJsonObject());
//                bindSpinners(binding.spRange, ordersFilters.get("order_range").getAsJsonObject());
                    searchObj = ordersFilters.get("search").getAsJsonObject();
                    bindSearch();
                    isBound = true;
                } catch (Exception e) {
                    ACRA.getErrorReporter().handleException(new CrashReportException("DATA NOT CORRECT : " + info.toString(), e));
                }
            } else { // clear old selection and select a new one
                statusAdapter.notifyDataSetChanged();
            }
        });

        this.adapter.subscribeCallbacks(
                (item, position) -> navigator.navigate(
                        requireActivity(),
                        ((JsonObject) item).get("link").getAsString(),
                        this::handleDefaultNavigationState)
        );

        binding.rvList.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL));
        binding.rvList.setAdapter(this.adapter);
    }

    private void fetchData(boolean isRefreshCall, Map<String, Map<String, String>> filterController) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(viewModel);
        Flowable<PagingData<JsonObject>> flow = PagingRx.getFlowable(viewModel.fetchData(isRefreshCall, filterController));
        PagingRx.cachedIn(flow, viewModelScope);
        flow.forEach(data -> adapter.submitData(getLifecycle(), data));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        searchItemMenu = menu.findItem(R.id.menu_search);
        searchView = (MySearchView) searchItemMenu.getActionView();
        bindSearch();
        if (!lastQuery.isEmpty()) {
            searchView.setIconified(false);
            searchView.setQuery(lastQuery, false);
        }
    }

    private void bindSearch() {
        if (searchView == null || searchObj == null)
            return;

        searchUrl = searchObj.get("url_key").getAsString();

        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(true);
        searchView.setQueryHint(searchObj.get("title").getAsString());

        // prevent overlapping spinner
//        searchView.setMaxWidth((getResources().getDisplayMetrics().widthPixels / 2));
//        ViewGroup.LayoutParams defaultLayoutParams = searchView.getLayoutParams();
//
        searchView.setOnSearchClickListener(v -> {
            searchView.setBackgroundResource(R.drawable.round_corners_medium_shape_with_tint);
            TransitionManager.beginDelayedTransition(getActivity().findViewById(R.id.main_toolbar));
            searchItemMenu.expandActionView();
        });

        searchView.setOnCloseListener(() -> {
            searchView.setBackground(null);
            TransitionManager.beginDelayedTransition(getActivity().findViewById(R.id.main_toolbar));
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // handle queries with a delay: provide better UX while not exhausting server
                if (delayedTask != null) handler.removeCallbacks(delayedTask);
                delayedTask = () -> {
                    if (!lastQuery.equals(newText)) {
                        reload(searchUrl, "search", newText);
                        lastQuery = newText;
                    }
                };
                handler.postDelayed(delayedTask, DELAY);
                return true;
            }
        });
    }

    private void bindStatusChips(JsonObject orderStatus) {

        statusUrl = orderStatus.get("url_key").getAsString();
        binding.statusTitle.setText(orderStatus.get("title").getAsString());

        List<SpinnerItem> values = bindFiledValuesToSpinnerItem(orderStatus.get("values").getAsJsonObject());

        int defaultSelection = 0;
        for (int i = 0; i < values.size(); i++) {
            SpinnerItem item = values.get(i);
            boolean isChecked = orderStatus.get("current_value").getAsString().equals(item.getKey());
            if (isChecked) {
                statusAdapter.setSelectedItem(i);
                defaultSelection = i;
                break;
            }
        }
        statusAdapter.submitList(values);
        int finalDefaultSelection = defaultSelection;
        binding.rvStatus.post(() -> binding.rvStatus.getLayoutManager().scrollToPosition(finalDefaultSelection));
    }

    private void bindSpinners(Spinner spinner, JsonObject item) {

//        List<SpinnerItem> values = bindFiledValuesToSpinnerItem(item.get("values").getAsJsonObject());
//
//        orderUrl = item.get("url_key").getAsString();
//
//        ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<>(
//                getContext(),
//                R.layout.spinner_item,
//                values
//        );
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                SpinnerItem spinnerItem = (SpinnerItem) parent.getItemAtPosition(position);
//
//                if (spinnerSelection.get("order_range").equals(spinnerItem))
//                    return;
//
//                spinnerSelection.put("order_range", spinnerItem);
//
//                reload(orderUrl, "order_range", spinnerItem.getValue());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        spinner.post(
//                () -> {
//                    int selectedPosition = findSelectedPosition(item.get("current_value").getAsString(), values);
//                    SpinnerItem spinnerItem = values.get(selectedPosition);
//                    spinnerSelection.put("order_range", spinnerItem);
//                    binding.spRange.setSelection(selectedPosition);
//                }
//        );
//
//        spinner.setAdapter(adapter);
    }

    private void reload(String url, String key, String value) {
        filterParams.put(url, value);
        filterController.put(key, filterParams);
        fetchData(true, filterController);
    }

    public int findSelectedPosition(String selectedKey, List<SpinnerItem> values) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getKey().equals(selectedKey))
                return i;
        }
        return 0;
    }

    private List<SpinnerItem> bindFiledValuesToSpinnerItem(JsonObject items) {
        List<SpinnerItem> result = new ArrayList<>();

        try {
            for (String key : items.keySet()) {
                JsonObject obj = items.get(key).getAsJsonObject();

                if (obj.get("disable").getAsBoolean())
                    continue;

                SpinnerItem item = new SpinnerItem();
                String value = obj.get("value").getAsString();
                String text = obj.get("text").getAsString();
                item.setKey(key);
                item.setValue(value);
                item.setTitle(text);
                if (obj.has("count"))
                    item.setCount(obj.get("count").getAsString());

                result.add(item);
            }
        } catch (Exception e) {
            Log.e("fields", e.getMessage());
            ACRA.getErrorReporter().handleException(new CrashReportException("Error occurred in bindFiledValuesToSpinnerItem (OrdersFragment). data: " + items, e));
        }
        return result;
    }


    private void subscribeUi() {

        fetchData(false, null);

        viewModel.getLoadingResponse().observe(this, aBoolean -> {
            if (!aBoolean) {
//                binding.shimmer.stopShimmer();
//                binding.shimmer.setVisibility(View.GONE);
                binding.rootContainer.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErrorResponse().observe(this, aBoolean -> {
            // do nothing
            if (aBoolean)
                UiComponents.showSnack(requireActivity(), getString(R.string.error_getting_list));
            binding.swipe.setRefreshing(false);
        });

        viewModel.getEmptyResponse().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.noContentText.setVisibility(View.VISIBLE);
                binding.noContentImage.setVisibility(View.VISIBLE);
                statusAdapter.notifyDataSetChanged(); // update selection stroke
            } else {
                binding.noContentText.setVisibility(View.GONE);
                binding.noContentImage.setVisibility(View.GONE);
            }
            binding.swipe.setRefreshing(false);
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
