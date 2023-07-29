package com.poonehmedia.app.ui.compare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.poonehmedia.app.data.model.CompareResult;
import com.poonehmedia.app.databinding.FragmentCompareBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;

public class CompareFragment extends BaseFragment {

    private FragmentCompareBinding binding;
    private CompareViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CompareViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCompareBinding.inflate(inflater, container, false);

        init();

        subscribeData();

        viewModel.resolveData();

        return binding.getRoot();
    }

    private void init() {
        int divider = (int) AndroidUtils.getPixels(1, getContext());
        binding.table.setShadowThick(divider);
        binding.table.setShadowHeadersThick(divider);
        binding.table.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    private void subscribeData() {
        viewModel.getCompareList().observe(getViewLifecycleOwner(), this::bindTable);
    }

    private void bindTable(CompareResult result) {

        AdaptiveAdapter adapter = new AdaptiveAdapter(getContext(), result.getData(), result.getMetaData());
        adapter.setRtl(true);
        binding.table.setAdapter(adapter);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
