package com.poonehmedia.app.ui.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentAccountAddressBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.DividerDecor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddressFragment extends BaseFragment {
    private FragmentAccountAddressBinding binding;
    private ShopUserAddressesViewModel viewModel;
    private JsonObject addNewAddressAction;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelStoreOwner backStackEntry = getGraphScope(R.id.address_graph);
        viewModel = new ViewModelProvider(backStackEntry, getDefaultViewModelProviderFactory()).get(ShopUserAddressesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountAddressBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        AddressAdapter adapter = new AddressAdapter();

        adapter.subscribeEditCallback(item -> {

            navigator.navigate(
                    requireActivity(),
                    item.get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });

        adapter.subscribeDeleteCallback(item -> {
            viewModel.delete(item);
        });

        binding.recycler.setAdapter(adapter);
        binding.recycler.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL)
        );

        String args = (String) getArguments().get("args");
        String actionStr = (String) getArguments().get("action");
        JsonArray addresses = JsonParser.parseString(args).getAsJsonArray();
        addNewAddressAction = JsonParser.parseString(actionStr).getAsJsonObject();
        if (addresses.size() == 0) showEmptyListImage();
        else adapter.submitList(addresses);


        binding.setAddAddress(v -> {
            navigator.navigate(requireActivity(),
                    addNewAddressAction.get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });
    }

    private void showEmptyListImage() {
        binding.emptyText.setVisibility(View.VISIBLE);
        binding.emptyImage.setVisibility(View.VISIBLE);
    }
}
