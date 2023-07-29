package com.poonehmedia.app.ui.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.DataState;
import com.poonehmedia.app.databinding.FragmentShopUserAddressesWithShimmerBinding;
import com.poonehmedia.app.ui.adapter.FragmentPagerAdapter;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.UiComponents;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ShopUserAddressesFragment extends BaseFragment {

    private FragmentShopUserAddressesWithShimmerBinding binding;
    private ShopUserAddressesViewModel viewModel;
    private boolean shouldRefetch = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelStoreOwner backStackEntry = getGraphScope(R.id.address_graph);
        viewModel = new ViewModelProvider(backStackEntry, getDefaultViewModelProviderFactory()).get(ShopUserAddressesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        shouldRefetch = binding != null;

        binding = FragmentShopUserAddressesWithShimmerBinding.inflate(inflater, container, false);
        handleShimmer(binding.shimmer, binding.main.rootContainer, true);
        subscribeViewModels();


        return binding.getRoot();
    }

    private void subscribeViewModels() {
        if (shouldRefetch)
            viewModel.fetchData();
        else
            viewModel.resolveData(false);

        viewModel.getAddress().observe(getViewLifecycleOwner(), jsonObjects -> {
            if (jsonObjects != null && jsonObjects.size() > 0) {
                extractDataAndSetUpTabLayout(jsonObjects);
                handleShimmer(binding.shimmer, binding.main.rootContainer, false);
            }
        });

        viewModel.getOnDeleteAddress().observe(getViewLifecycleOwner(), dataState -> {
            if (dataState instanceof DataState.Success) {
                UiComponents.showSnack(requireActivity(), (String) DataState.data);
                viewModel.fetchData();
            } else if (dataState instanceof DataState.Error) {
                UiComponents.showSnack(requireActivity(), DataState.message);
            }
        });
    }

    private void extractDataAndSetUpTabLayout(JsonObject jsonObject) {
        if (!JsonHelper.has(jsonObject, "addresses")) {
            showEmptyListImage();
            return;
        }

        JsonArray addresses = jsonObject.get("addresses").getAsJsonArray();

        List<JsonObject> billing = new ArrayList<>();
        List<JsonObject> shopping = new ArrayList<>();

        for (int i = 0; i < addresses.size(); i++) {

            JsonObject item = addresses.get(i).getAsJsonObject();

            if (item.get("type").getAsString().equals("billing") || item.get("type").getAsString().isEmpty()) {
                billing.add(item);
            } else if (item.get("type").getAsString().equals("shipping") || item.get("type").getAsString().isEmpty())
                shopping.add(item);
        }
        setUpTabLayout(billing, shopping, jsonObject);
    }

    private void showEmptyListImage() {
        binding.main.emptyText.setVisibility(View.VISIBLE);
        binding.main.emptyImage.setVisibility(View.VISIBLE);
    }

    private void setUpTabLayout(List<JsonObject> billing, List<JsonObject> shipping, JsonObject obj) {

        JsonObject shippingAction = obj.get("new_shipping_address_action").getAsJsonObject();
        JsonObject billingAction = obj.get("new_billing_address_action").getAsJsonObject();

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(getFragment(billing.toString(), billingAction.toString()));
        fragments.add(getFragment(shipping.toString(), shippingAction.toString()));


        FragmentPagerAdapter adapter = new FragmentPagerAdapter(this, fragments);
        binding.main.viewPagerAddresses.setAdapter(adapter);

        String billingTitle = obj.get("default_billing_select_title").getAsString();
        String shippingTitle = obj.get("default_shipping_select_title").getAsString();

        new TabLayoutMediator(binding.main.tabLayout, binding.main.viewPagerAddresses,
                (tab, position) -> {
                    if (position == 0) tab.setText(billingTitle);
                    else tab.setText(shippingTitle);
                }
        ).attach();

    }

    private AddressFragment getFragment(String args, String action) {

        Bundle b = new Bundle();
        b.putString("args", args);
        b.putString("action", action);

        AddressFragment f = new AddressFragment();
        f.setArguments(b);

        return f;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }

}
