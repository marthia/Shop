package com.poonehmedia.app.ui.affiliate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.TabAffiliateBannersBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AffiliateTabBannersFragment extends BaseFragment {

    private GenericListAdapterImp<JsonElement> adapter;
    private TabAffiliateBannersBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = TabAffiliateBannersBinding.inflate(inflater, container, false);

        init();
        parseArgument();

        return binding.getRoot();
    }

    private void init() {
        adapter = new GenericListAdapterImp<>();
        adapter.setLayoutRes(R.layout.list_item_tab_affiliate_banner);
        binding.recycler.setAdapter(adapter);
        binding.recycler.addItemDecoration(new DividerDecor(requireContext(), 0, DividerDecor.VERTICAL));
    }

    private void parseArgument() {
        String args = (String) getArguments().get("args");
        JsonObject data = JsonParser.parseString(args).getAsJsonObject();
        JsonArray items = data.get("items").getAsJsonArray();
        adapter.submitList(items);
    }
}
