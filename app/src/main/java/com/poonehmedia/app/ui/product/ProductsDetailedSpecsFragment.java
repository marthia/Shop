package com.poonehmedia.app.ui.product;

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
import com.poonehmedia.app.databinding.FragmentDetailedSpecsBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProductsDetailedSpecsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentDetailedSpecsBinding binding = FragmentDetailedSpecsBinding.inflate(inflater, container, false);

        String cf = ProductsDetailedSpecsFragmentArgs.fromBundle(getArguments()).getSpecs();
        JsonArray specs = (JsonArray) JsonParser.parseString(cf);
        if (specs != null) {

            GenericListAdapterImp<JsonElement> adapter = new GenericListAdapterImp<>();
            adapter.setLayoutRes(R.layout.list_item_product_details);
            adapter.submitList(specs);
            binding.rvDetailsItem.setAdapter(adapter);
        }

        return binding.getRoot();
    }
}