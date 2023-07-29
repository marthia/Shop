package com.poonehmedia.app.ui.checkoutend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.gson.JsonObject;
import com.poonehmedia.app.databinding.FragmentCheckoutEndBinding;
import com.poonehmedia.app.ui.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CheckoutEndFragment extends BaseFragment {

    private FragmentCheckoutEndBinding binding;
    private CheckoutEndViewModel viewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CheckoutEndViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentCheckoutEndBinding.inflate(inflater, container, false);

            binding.swipe.setRefreshing(true);

            viewModel.fetchData();
            bind();
        }
        return binding.getRoot();
    }

    private void bind() {
        viewModel.getData().observe(getViewLifecycleOwner(), jsonObject -> {
            if (jsonObject != null) {

                binding.swipe.setRefreshing(false);
                binding.action.setVisibility(View.VISIBLE);

                binding.text.setText(jsonObject.get("text").getAsString());
                JsonObject action = jsonObject.get("action").getAsJsonObject();
                binding.action.setText(action.get("text").getAsString());

                binding.action.setOnClickListener(v -> {

                    Navigation.findNavController(binding.getRoot()).popBackStack();
                    navigator.navigate(
                            requireActivity(),
                            action.get("link").getAsString(),
                            this::handleDefaultNavigationState
                    );

                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }

}
