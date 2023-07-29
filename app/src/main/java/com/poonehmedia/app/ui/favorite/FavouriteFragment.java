package com.poonehmedia.app.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentFavoriteBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.DividerDecor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavouriteFragment extends BaseFragment {

    @Inject
    public DataController dataController;
    private GenericListAdapterImp<JsonElement> adapter;
    private FavoriteViewModel viewModel;
    private FragmentFavoriteBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);

        setUpList();

        subscribeLiveDatas();

        return binding.getRoot();
    }

    private void subscribeLiveDatas() {
        viewModel.resolveData();

        viewModel.getData().observe(getViewLifecycleOwner(), item -> {
            if (JsonHelper.has(item, "products"))
                adapter.submitList(item.get("products").getAsJsonArray());
            else handleEmptyList();
        });
    }

    private void handleEmptyList() {
        binding.recycler.setVisibility(View.GONE);
        binding.emptyText.setVisibility(View.VISIBLE);
        binding.emptyImage.setVisibility(View.VISIBLE);
    }

    private void setUpList() {
        adapter = new GenericListAdapterImp<>();
        adapter.setLayoutRes(R.layout.list_item_favorite);
        adapter.subscribeCallbacks((item, position) -> {

            navigator.navigate(
                    requireActivity(),
                    ((JsonObject) item).get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });
        binding.recycler.setAdapter(adapter);

        binding.recycler.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL)
        );
        binding.recycler.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.HORIZONTAL)
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }


}
