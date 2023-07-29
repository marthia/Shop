package com.poonehmedia.app.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.Filter;
import com.poonehmedia.app.databinding.FragmentFilterListBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FilterListFragment extends BaseFragment {
    @Inject
    public FilterListAdapter adapter;
    private JsonObject filterItem;
    private int parentPos;
    private SharedFilterProductsViewModel sharedViewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        com.poonehmedia.app.databinding.FragmentFilterListBinding binding = FragmentFilterListBinding.inflate(inflater, container, false);

        NavBackStackEntry backStackEntry = getGraphScope(R.id.products_graph);
        sharedViewModel = new ViewModelProvider(backStackEntry, getDefaultViewModelProviderFactory()).get(SharedFilterProductsViewModel.class);

        parentPos = FilterListFragmentArgs.fromBundle(getArguments()).getParentPos();
        setHasOptionsMenu(true);

        binding.rvFilters.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL));
        adapter.subscribeCallbacks((type, isChecked, value, key, subtitle, item, position) -> {
            Filter filter = new Filter();
            filter.setChecked(isChecked);
            filter.setType(type);
            filter.setOptionKey(key);
            filter.setValue(value);
            filter.setMetadata(item);
            filter.setSubtitle(subtitle);
            filter.setParentPos(parentPos);

            sharedViewModel.setFilterValues(filter);
            sharedViewModel.applyFilter();
        });
        binding.rvFilters.setAdapter(adapter);

        sharedViewModel.getCurrentFilter().observe(backStackEntry, jsonObject -> {
            if (jsonObject != null) {
                filterItem = jsonObject;
                adapter.submitList(jsonObject);
            }
        });

        sharedViewModel.getFilterValues().observe(backStackEntry, stringFilterDataMap -> {
            if (stringFilterDataMap != null) {
                adapter.setSelectedValues(stringFilterDataMap);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.filter_controller_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_clear_sub_filter) {
            if (filterItem != null)
                sharedViewModel.clearFilterFor(filterItem.get("filter_id").getAsString());
            Navigation.findNavController(requireActivity(), R.id.main_nav_fragment).popBackStack();
            return true;
        }

        return false;
    }
}
