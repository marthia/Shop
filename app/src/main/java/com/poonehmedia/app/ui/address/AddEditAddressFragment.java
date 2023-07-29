package com.poonehmedia.app.ui.address;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.DataState;
import com.poonehmedia.app.data.model.FormData;
import com.poonehmedia.app.databinding.FragmentAddNewAddressWithShimmerBinding;
import com.poonehmedia.app.ui.adapter.FormBuilderAdapter;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.UiComponents;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddEditAddressFragment extends BaseFragment {

    @Inject
    public FormBuilderAdapter adapter;
    private ShopUserAddressesViewModel viewModel;
    private FragmentAddNewAddressWithShimmerBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ShopUserAddressesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddNewAddressWithShimmerBinding.inflate(inflater, container, false);

        handleShimmer(binding.shimmer, binding.main.rootContainer, true);

        init();
        subscribeData();

        return binding.getRoot();
    }

    private void subscribeData() {
        viewModel.resolveData(true);

        viewModel.getEditIem().observe(getViewLifecycleOwner(), jsonObject -> {
            if (jsonObject != null) {
                adapter.submitList(jsonObject.get("fields").getAsJsonArray());
                handleShimmer(binding.shimmer, binding.main.rootContainer, false);
            }
        });

        viewModel.getSaveResponse().observe(getViewLifecycleOwner(), s -> {
            if (s instanceof DataState.Error) {
                UiComponents.showSnack(requireActivity(), DataState.message);
            } else if (s instanceof DataState.Success) {
                UiComponents.showSnack(requireActivity(), ((String) DataState.data));
                Navigation.findNavController(requireActivity(), R.id.main_nav_fragment).navigateUp();
            }
        });

        viewModel.getSpinnerUpdate().observe(getViewLifecycleOwner(), jsonObject -> {
            if (jsonObject != null) {
                if (jsonObject.has("tag")) {
                    String tag = jsonObject.get("tag").getAsString();
                    jsonObject.remove("tag");
                    adapter.updateSpinner(tag, jsonObject);
                }
            }
        });
    }

    private void init() {
        adapter.subscribeCallbacks((item, key) -> {
            viewModel.update(((JsonObject) item), key);
        });

        adapter.subscribeOnSubmit(this::submit);

        binding.main.recyclerView.setHasFixedSize(true);
        binding.main.recyclerView.setItemViewCacheSize(20);
        binding.main.recyclerView.setAdapter(adapter);

        binding.main.btnSave.setOnClickListener(v -> submit());

    }

    private void submit() {
        AndroidUtils.hideKeyboardFrom(binding.getRoot());
        if (!adapter.hasErrors()) {
            Map<Integer, FormData> values = adapter.getValues();
            Log.i("values", values.toString());
            viewModel.saveNewAddress(values);

        } else {
            String message = getString(R.string.make_sure_all_filled);
            UiComponents.showSnack(requireActivity(), message);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
