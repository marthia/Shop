package com.poonehmedia.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonObject;
import com.poonehmedia.app.databinding.FragmentHomeBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.ui.modules.ModulesAdapter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends BaseFragment {

    @Inject
    public ModulesAdapter adapter;
    private HomeViewModel viewModel;
    private FragmentHomeBinding binding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);

            initViews();
            subscribeLiveDatas();
            viewModel.resolveData();
        }
        return binding.getRoot();
    }

    private void initViews() {
        adapter.setLang(viewModel.getLanguage());
        adapter.subscribeCallbacks((item, position) -> {

            navigator.navigate(
                    requireActivity(),
                    ((JsonObject) item).get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });

        binding.swipe.setDistanceToTriggerSync(1000);

        binding.swipe.setOnRefreshListener(() -> {
            viewModel.fetchData();
        });

        binding.swipe.setRefreshing(true);

        binding.staggeredList.setItemViewCacheSize(10);
        binding.staggeredList.setDrawingCacheEnabled(true);
        binding.staggeredList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.staggeredList.setAdapter(adapter);
    }

    private void subscribeLiveDatas() {

        viewModel.getData().observe(getViewLifecycleOwner(), array -> {
            adapter.submitList(array);
            binding.swipe.setRefreshing(false);
        });

        viewModel.getIsError().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean)
                binding.swipe.setRefreshing(false);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}