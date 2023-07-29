package com.poonehmedia.app.ui.manufacturer;

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
import com.poonehmedia.app.databinding.FragmentManufacturerBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.ItemSpaceDecoration;
import com.poonehmedia.app.util.ui.UiComponents;

import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;

public class ManufacturerFragment extends BaseFragment {

    private FragmentManufacturerBinding binding;
    private ManufacturerViewModel viewModel;
    private ManufacturerPagingAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManufacturerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (binding == null) {

            binding = FragmentManufacturerBinding.inflate(LayoutInflater.from(container.getContext()), container, false);

            init();
            subscribeUi();
        }

        return binding.getRoot();
    }

    private void subscribeUi() {

        fetchData(false);

        viewModel.getLoadingResponse().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) binding.progress.setVisibility(View.VISIBLE);
            else binding.progress.setVisibility(View.GONE);
        });

        viewModel.getErrorResponse().observe(getViewLifecycleOwner(), aBoolean -> {
            // do nothing
            if (aBoolean)
                UiComponents.showSnack(requireActivity(), getString(R.string.error_getting_list));
        });

        viewModel.getEmptyResponse().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                binding.noContentText.setVisibility(View.VISIBLE);
                binding.noContentImage.setVisibility(View.VISIBLE);
            } else {
                binding.noContentText.setVisibility(View.GONE);
                binding.noContentImage.setVisibility(View.GONE);
            }
        });

    }

    private void fetchData(boolean isRefreshCall) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(viewModel);
        Flowable<PagingData<JsonObject>> flow = PagingRx.getFlowable(viewModel.fetchData(isRefreshCall));
        PagingRx.cachedIn(flow, viewModelScope);
        flow.forEach(data -> adapter.submitData(getLifecycle(), data));
    }

    private void init() {
        adapter = new ManufacturerPagingAdapter();

        adapter.subscribeCallbacks((item, position) -> {
            navigator.navigate(
                    requireActivity(),
                    ((JsonObject) item).get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });
        ItemSpaceDecoration itemSpaceDecoration = new ItemSpaceDecoration(
                (int) AndroidUtils.getPixels(8, getContext())
        );
        binding.recycler.addItemDecoration(itemSpaceDecoration);
        binding.recycler.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
