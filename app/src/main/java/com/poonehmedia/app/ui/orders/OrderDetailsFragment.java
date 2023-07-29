package com.poonehmedia.app.ui.orders;

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
import com.poonehmedia.app.databinding.FragmentOrderBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OrderDetailsFragment extends BaseFragment {

    private GenericListAdapterImp<JsonElement> adapter;
    private FragmentOrderBinding binding;
    private OrderDetailsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OrderDetailsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

//        handleShimmer(binding.shimmer, binding.main.rootContainer, true);

        setUpList();
        fetchData();

        return binding.getRoot();
    }


    private void fetchData() {
        viewModel.resolveData();

        viewModel.getProducts().observe(getViewLifecycleOwner(), array -> {
            if (array != null && array.size() > 0) {
                binding.setQuantity(array.size());
                adapter.submitList(array);
//                handleShimmer(binding.shimmer, binding.main.rootContainer, false);
            }
        });

    }

    private void setUpList() {
        adapter = new GenericListAdapterImp<>();
        adapter.setLayoutRes(R.layout.list_item_order_products);
        adapter.subscribeCallbacks((item, position) -> {

            navigator.navigate(
                    requireActivity(),
                    ((JsonObject) item)
                            .get("actions").getAsJsonObject()
                            .get("product_link").getAsJsonObject()
                            .get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });
        binding.recycler.setAdapter(adapter);
        binding.recycler.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL)
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
