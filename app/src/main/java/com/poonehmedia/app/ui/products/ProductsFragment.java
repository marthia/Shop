package com.poonehmedia.app.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.gson.JsonObject;
import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.FilterData;
import com.poonehmedia.app.data.model.GroupedList;
import com.poonehmedia.app.databinding.FragmentProductsWithShimmerBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;
import com.poonehmedia.app.util.ui.StickyHeaderRecyclerItemDecoration;
import com.poonehmedia.app.util.ui.UiComponents;

import org.acra.ACRA;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;

@AndroidEntryPoint
public class ProductsFragment extends BaseFragment {
    @Inject
    public ProductsPagedListAdapterGrouped productsAdapter;
    private ProductsViewModel viewModel;
    private FragmentProductsWithShimmerBinding binding;
    private SharedFilterProductsViewModel sharedViewModel;
    private NavBackStackEntry backStackEntry;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProductsViewModel.class);

        backStackEntry = getGraphScope(R.id.products_graph);
        sharedViewModel = new ViewModelProvider(backStackEntry, getDefaultViewModelProviderFactory()).get(SharedFilterProductsViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (binding == null) {
            binding = FragmentProductsWithShimmerBinding.inflate(inflater, container, false);

            handleShimmer(binding.shimmer, binding.main.rootContainer, true);

            init();

            subscribeUi();

            binding.main.swipe.setDistanceToTriggerSync(1000);
            binding.main.btnCompare.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(ProductsFragmentDirections.actionGoToCompare()));

            binding.main.swipe.setOnRefreshListener(() -> fetchData(true, null));

            binding.main.btnFilter.setOnClickListener(v -> navigator.intrinsicNavigate(
                    requireActivity(),
                    ProductsFragmentDirections.actionGoToFilter(),
                    false));

            binding.main.btnSort.setOnClickListener(v -> navigator.intrinsicNavigate(
                    requireActivity(),
                    ProductsFragmentDirections.actionGoToSort(),
                    false));
        }

        return binding.getRoot();
    }

    private void init() {

        binding.main.setOnClearFiltersClick(v -> {
            sharedViewModel.clearFilterValues();
            binding.main.clearAllFilters.setVisibility(View.GONE);
        });

        productsAdapter.subscribeCallbacks(
                (item, position) -> navigator.navigate(
                        requireActivity(),
                        ((JsonObject) item).get("link").getAsString(),
                        this::handleDefaultNavigationState),

                (item, position) -> navigator.navigate(
                        requireActivity(),
                        ((JsonObject) item).get("link").getAsString(),
                        this::handleDefaultNavigationState)
        );

        // sticky headers
        StickyHeaderRecyclerItemDecoration stickyDecoration =
                new StickyHeaderRecyclerItemDecoration(productsAdapter);

        binding.main.rvList.addItemDecoration(stickyDecoration);

//        RecyclerItemTouchHelper touchHelper = new RecyclerItemTouchHelper(ItemTouchHelper.LEFT);
        // swipe action
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelper);
//        itemTouchHelper.attachToRecyclerView(binding.main.rvList);
        binding.main.btnCompare.setVisibility(View.GONE);
        // dividers
        binding.main.rvList.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL));
        binding.main.rvList.setAdapter(productsAdapter);
    }

    private void subscribeUi() {

        fetchData(false, null);

        sharedViewModel.getFilterParams().observe(backStackEntry, jsonObject -> {
            fetchData(true, jsonObject);
        });

        sharedViewModel.getFilterValues().observe(backStackEntry, this::handleBadges);

        viewModel.getLoadingResponse().observe(backStackEntry, aBoolean ->
                handleShimmer(binding.shimmer, binding.main.rootContainer, aBoolean)
        );

        viewModel.getErrorResponse().observe(backStackEntry, aBoolean -> {
            // do nothing
            if (aBoolean)
                UiComponents.showSnack(requireActivity(), getString(R.string.error_getting_list));
            binding.main.swipe.setRefreshing(false);
        });

        viewModel.getEmptyResponse().observe(backStackEntry, aBoolean -> {
            if (aBoolean) {
                binding.main.noContentText.setVisibility(View.VISIBLE);
                binding.main.noContentImage.setVisibility(View.VISIBLE);
                binding.main.clearAllFilters.setVisibility(View.VISIBLE);
            } else {
                binding.main.noContentText.setVisibility(View.GONE);
                binding.main.noContentImage.setVisibility(View.GONE);
                binding.main.clearAllFilters.setVisibility(View.GONE);
            }
            binding.main.swipe.setRefreshing(false);
        });

        viewModel.getFilters().observe(backStackEntry, array -> {
            if (array.size() != 0)
                sharedViewModel.setFilterData(array);
            else binding.main.btnFilter.setVisibility(View.GONE);
        });

        viewModel.getSorts().observe(backStackEntry, array -> {
            if (array.size() != 0)
                sharedViewModel.setSortData(array);
            else binding.main.btnSort.setVisibility(View.GONE);
        });

        viewModel.getTotalCount().observe(backStackEntry, integer -> {
            if (integer != null && integer != -1) {
                sharedViewModel.updateTotalCountVisibility(true);
                sharedViewModel.setTotalCount(integer);
            }
        });

    }


    private void handleBadges(Map<String, FilterData> item) {
        boolean hasSort = false;
        boolean hasFilter = false;
        String sortTitle = "";
        for (String key : item.keySet()) {
            if (item.get(key).getType().equals("sort")) {
                sortTitle = item.get(key).getSelectedValuesLabels().values().toString()
                        .replace("[", "")
                        .replace("]", "");
                hasSort = true;
            } else if (item.get(key).getSelectedValues().size() > 0)
                hasFilter = true;
        }

        binding.main.filterBadge.setVisibility(hasFilter ? View.VISIBLE : View.INVISIBLE);
        binding.main.sortBadge.setVisibility(hasSort ? View.VISIBLE : View.INVISIBLE);
        if (hasSort) binding.main.btnSort.setText(sortTitle);

    }

    private void fetchData(boolean isRefreshCall, JsonObject filters) {
        sharedViewModel.updateTotalCountVisibility(false);
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(viewModel);
        Flowable<PagingData<GroupedList>> flow = PagingRx.getFlowable(viewModel.fetchData(isRefreshCall, filters));
        PagingRx.cachedIn(flow, viewModelScope);
        flow.forEach(data -> productsAdapter.submitData(getLifecycle(), data));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedViewModel.clearFilterValues();
        viewModel.disposeAll();
    }
}
