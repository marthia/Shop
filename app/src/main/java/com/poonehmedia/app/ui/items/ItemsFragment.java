package com.poonehmedia.app.ui.items;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentItemsWithShimmerBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;
import com.poonehmedia.app.util.ui.UiComponents;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;

@SuppressLint("FragmentLiveDataObserve")
@AndroidEntryPoint
public class ItemsFragment extends BaseFragment {

    @Inject
    public ItemsPagedListAdapter adapter;
    private FragmentItemsWithShimmerBinding binding;
    private ItemsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ItemsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (binding == null) {
            binding = FragmentItemsWithShimmerBinding.inflate(inflater, container, false);

            init();
            subscribeUi();
        }

        return binding.getRoot();

    }

    private void init() {
        adapter.subscribeCallbacks((item, position) ->
                navigator.navigate(
                        requireActivity(),
                        ((JsonObject) item).get("link").getAsString(),
                        this::handleDefaultNavigationState
                ));

        binding.main.recycler.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL));

        binding.main.recycler.setAdapter(adapter);

        binding.main.swipe.setDistanceToTriggerSync(1000);

        binding.main.swipe.setOnRefreshListener(() -> {
            fetchData(true);
        });
    }

    private void subscribeUi() {

        fetchData(false);

        viewModel.getErrorResponse().observe(this, aBoolean -> {
            if (aBoolean) {
                UiComponents.showSnack(requireActivity(), getString(R.string.error_getting_list));
                binding.main.swipe.setRefreshing(false);
            }
        });

        viewModel.getLoadingResponse().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.main.swipe.setRefreshing(true);
            }
            handleShimmer(binding.shimmer, binding.main.rootContainer, aBoolean);
        });

        viewModel.getEmptyResponse().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.main.emptyText.setVisibility(View.VISIBLE);
                binding.main.emptyImage.setVisibility(View.VISIBLE);
                binding.main.recycler.setVisibility(View.GONE);
            } else {
                binding.main.emptyText.setVisibility(View.GONE);
                binding.main.emptyImage.setVisibility(View.GONE);
                binding.main.recycler.setVisibility(View.VISIBLE);
            }
            binding.main.swipe.setRefreshing(false);
        });

    }

    private void fetchData(boolean isRefreshCall) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(viewModel);
        Flowable<PagingData<JsonObject>> flow = PagingRx.getFlowable(viewModel.fetchData(isRefreshCall));
        PagingRx.cachedIn(flow, viewModelScope);
        flow.forEach(data -> adapter.submitData(getLifecycle(), data));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
