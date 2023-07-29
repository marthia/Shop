package com.poonehmedia.app.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.Filter;
import com.poonehmedia.app.data.model.FilterCategories;
import com.poonehmedia.app.data.model.FilterData;
import com.poonehmedia.app.databinding.BottomBarFilterBinding;
import com.poonehmedia.app.databinding.FragmentFilterCategoriesBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FilterCategoriesFragment extends BaseFragment {
    @Inject
    public FilterCategoriesAdapter adapter;
    private FragmentFilterCategoriesBinding binding;
    private NavController navController;
    private SharedFilterProductsViewModel sharedViewModel;
    private BottomBarFilterBinding bottomBarBinding;
    private ViewStub stub;
    private View bottomBar;
    private NavBackStackEntry backStackEntry;
    private Map<String, FilterData> filterData;

    private ArrayList<FilterCategories> bindJsonToFilterObject(JsonArray array) {
        ArrayList<FilterCategories> categories = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {

            JsonObject item = array.get(i).getAsJsonObject();
            String title = item.get("filter_name").getAsString();
            String type = item.get("filter_type").getAsString();

            FilterCategories category = new FilterCategories();
            category.setTitle(title);
            category.setType(type);
            category.setMetadata(item);

            categories.add(category);
        }
        return categories;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backStackEntry = getGraphScope(R.id.products_graph);
        sharedViewModel = new ViewModelProvider(backStackEntry, getDefaultViewModelProviderFactory()).get(SharedFilterProductsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (binding == null) {
            binding = FragmentFilterCategoriesBinding.inflate(inflater, container, false);

            setHasOptionsMenu(true);

            initBottomBar();
            init();
            subscribeUi();
        }

        return binding.getRoot();
    }

    private void subscribeUi() {

        sharedViewModel.getFilterData().observe(backStackEntry, array -> {
            if (array != null && array.size() > 0) {
                if (filterData != null && filterData.size() != 0) {
                    notifyChange();
                } else {
                    ArrayList<FilterCategories> categories = bindJsonToFilterObject(array);
                    adapter.submitList(categories);
                }
            }
        });

        // listen for filter values and bind the changes to the current list
        sharedViewModel.getFilterValues().observe(backStackEntry, stringFilterDataMap -> {
            filterData = stringFilterDataMap;
            adapter.setSelectedValues(stringFilterDataMap);
            notifyChange();
        });
    }

    private void notifyChange() {
        int changedPosition = sharedViewModel.extractValueAndSetToAdapterList(
                filterData,
                adapter.getCurrentList()
        );
        if (changedPosition != -1) {
            binding.rvFilters.post(() -> adapter.notifyItemChanged(changedPosition));
        }
    }

    private void init() {
        navController = Navigation.findNavController(requireActivity(), R.id.main_nav_fragment);

        // init list
        adapter.subscribeCallbacks((item, position) -> {
            sharedViewModel.setCurrentFilter(((FilterCategories) item).getMetadata());
            updateTitle(((FilterCategories) item).getTitle());
            navController.navigate(FilterCategoriesFragmentDirections.actionFilterCatToList(position));
        });

        adapter.subscribeFilterChanged((type, isChecked, value, key, subtitle, item, position) -> {
            Filter filter = new Filter();
            filter.setChecked(isChecked);
            filter.setType(type);
            filter.setOptionKey(key);
            filter.setValue(value);
            filter.setMetadata(item);
            filter.setSubtitle(subtitle);
            filter.setParentPos(position);

            sharedViewModel.setFilterValues(filter);
            sharedViewModel.applyFilter();
        });


        binding.rvFilters.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL));
        binding.rvFilters.setAdapter(adapter);
    }

    private void initBottomBar() {

        stub = requireActivity().findViewById(R.id.filter_layout_stub);

        if (stub != null) {
            stub.setLayoutResource(R.layout.bottom_bar_filter);
            stub.setOnInflateListener((stub1, inflated) -> {
                inflateBottomBar(inflated);
            });
            stub.inflate();

        } else {
            bottomBar = requireActivity().findViewById(R.id.bottom_bar_filter);
            bottomBar.setVisibility(View.VISIBLE);
            inflateBottomBar(bottomBar);
        }
    }

    private void inflateBottomBar(View inflatedView) {

        // prevent rebinding
        if (bottomBarBinding != null) return;

        bottomBarBinding = DataBindingUtil.bind(inflatedView);
        bottomBarBinding.setLifecycleOwner(backStackEntry);

        bottomBarBinding.setViewModel(sharedViewModel);
        bottomBarBinding.getRoot().setOnClickListener(v -> {
            navigator.intrinsicNavigate(requireActivity(), FilterCategoriesFragmentDirections.actionReturnToProducts(), false);
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.filter_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_clear) {
            sharedViewModel.clearFilterValues();
            navController.popBackStack();
            return true;
        }

        return false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (stub != null)
            stub.setVisibility(View.GONE);
        else bottomBar.setVisibility(View.GONE);
    }
}
