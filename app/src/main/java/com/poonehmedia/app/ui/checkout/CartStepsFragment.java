package com.poonehmedia.app.ui.checkout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonArray;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentCartStepsBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.ui.checkout.adapter.CartStepsAdapter;
import com.poonehmedia.app.ui.product.ProductCartViewModel;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.ui.UiComponents;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CartStepsFragment extends BaseFragment {

    @Inject
    public DataController dataController;
    private CheckoutViewModel viewModel;
    private FragmentCartStepsBinding binding;
    private CartStepsAdapter adapter;
    private ProductCartViewModel productCartViewModel;
    private boolean isFirstRun = true;
    private boolean refresh = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        productCartViewModel = new ViewModelProvider(this).get(ProductCartViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // when an old instance is revived (e.g; when editing current page data) the data must be fetched
        // from server rather than using the old data
        refresh = binding != null;

        binding = FragmentCartStepsBinding.inflate(inflater, container, false);

        initViews();
        subscribeLiveDatas();

        return binding.getRoot();
    }

    @SuppressLint("FragmentLiveDataObserve")
    private void subscribeLiveDatas() {

        if (refresh)
            viewModel.fetchData();
        else
            viewModel.resolveData();

        viewModel.getData().observe(this, content -> {
            if (content != null && content.size() > 0) {
                adapter.submitList(content);
            }
        });

        viewModel.getNextButton().observe(this, info -> {
            if (info != null) {
                adapter.submitNextButton(info);
            }
        });

        viewModel.getResponseMessage().observe(this, s -> {
            if (s != null)
                handleEmptyList(s);
        });

        viewModel.getNextButtonStatus().observe(this, aBoolean -> {
            adapter.handleStubStatus(aBoolean);
        });
    }

    private void initViews() {

        adapter = new CartStepsAdapter(navigator, this, requireActivity(),
                dataController,
                productCartViewModel,
                viewModel,
                (items, position) -> {
                    viewModel.setNextButtonStatus(true);
                    if (((JsonArray) items).size() == 0)
                        handleEmptyList(getResources().getString(R.string.empty_cart));
                });
        adapter.subscribeNavigation(link ->
                navigator.navigate(binding.getRoot(), link, this::handleDefaultNavigationState)
        );

        binding.staggeredList.setAdapter(adapter);
        binding.staggeredList.setItemViewCacheSize(10);
        binding.staggeredList.setDrawingCacheEnabled(true);
        binding.staggeredList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    private void handleEmptyList(String message) {
        binding.emptyText.setVisibility(View.VISIBLE);
        binding.emptyImage.setVisibility(View.VISIBLE);
        UiComponents.showSnack(requireActivity(), message);
        adapter.hideBottomBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.hideBottomBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        // true only when resumed from an old instance in system tray (recent apps or when pressed home and relaunched again).
        if (adapter != null && !isFirstRun)
            adapter.intiBottomBar();

        isFirstRun = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
