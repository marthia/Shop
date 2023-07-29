package com.poonehmedia.app.ui.product;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentPriceHistoryBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PriceHistoryFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentPriceHistoryBinding binding = FragmentPriceHistoryBinding.inflate(inflater, container, false);

        String listStr = PriceHistoryFragmentArgs.fromBundle(getArguments()).getList();
        JsonArray list = (JsonArray) JsonParser.parseString(listStr);

        GenericListAdapterImp<JsonElement> adapter = new GenericListAdapterImp<>();
        adapter.setLayoutRes(R.layout.list_item_price_history);
        adapter.setAlternateBackgroundColor(Color.parseColor("#F8F8F8"));
        binding.priceHistory.setAdapter(adapter);
        adapter.submitList(list);

        return binding.getRoot();
    }
}
