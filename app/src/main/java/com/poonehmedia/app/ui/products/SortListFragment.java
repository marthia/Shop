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
import com.poonehmedia.app.data.model.SortItem;
import com.poonehmedia.app.databinding.BottomBarFilterBinding;
import com.poonehmedia.app.databinding.FragmentSortBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SortListFragment extends BaseFragment {
    @Inject
    public SortListAdapter adapter;
    private SharedFilterProductsViewModel sharedViewModel;
    private NavController navController;
    private FragmentSortBinding binding;
    private String filterId;
    private NavBackStackEntry backStackEntry;
    private BottomBarFilterBinding bottomBarBinding;
    private ViewStub stub;
    private View bottomBar;

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

            binding = FragmentSortBinding.inflate(inflater, container, false);
            setHasOptionsMenu(true);

            navController = Navigation.findNavController(requireActivity(), R.id.main_nav_fragment);

            initBottomBar();
            initViews();
            subscribeLiveDatas();
        }

        return binding.getRoot();
    }

    private void subscribeLiveDatas() {

        sharedViewModel.getSortData().observe(backStackEntry, array -> {
            if (array != null) {
                filterId = array.get(0).getAsJsonObject().get("filter_id").getAsString();
                List<SortItem> sortItems = bindSortArrayToSortItem(array);
                adapter.submitList(sortItems);
            }
        });

        sharedViewModel.getFilterValues().observe(backStackEntry, stringFilterDataMap -> {
            if (stringFilterDataMap != null) {
                adapter.submitSelectedValues(stringFilterDataMap);
            }
        });

    }

    private List<SortItem> bindSortArrayToSortItem(JsonArray sort) {
        List<SortItem> sortItems = new ArrayList<>();
        JsonObject item = sort.get(0).getAsJsonObject();
        JsonArray filterOption = item.get("filter_option").getAsJsonArray();


        for (int i = 0; i < filterOption.size(); i++) {
            JsonObject filterOptionItem = filterOption.get(i).getAsJsonObject();
            SortItem sortItem = new SortItem();

            sortItem.setTitle(filterOptionItem.get("title").getAsString());
            sortItem.setAction(filterOptionItem.get("key").getAsString());
            sortItem.setData(item);

            sortItems.add(sortItem);
        }

        return sortItems;
    }

    private void initViews() {
        // init list
        adapter.setSharedViewModel(sharedViewModel);
        adapter.subscribeCallbacks((item, position) -> {
            sharedViewModel.applyFilter();
            binding.rvSorts.postDelayed(() -> navController.popBackStack(), 1500);
        });
        binding.rvSorts.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL)
        );
        binding.rvSorts.setAdapter(adapter);

        bottomBarBinding.getRoot().setOnClickListener(v -> {
            navController.popBackStack();
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
            sharedViewModel.clearSortFilter(filterId);
            navController.popBackStack();
            return true;
        }

        return false;
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
            navController.popBackStack();
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (stub != null)
            stub.setVisibility(View.GONE);
        else bottomBar.setVisibility(View.GONE);
    }

}
