package com.poonehmedia.app.ui.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentProfileWithShimmerBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.DividerDecor;
import com.poonehmedia.app.util.ui.UiComponents;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends BaseFragment {

    private GenericListAdapterImp<JsonElement> adapter;
    private GenericListAdapterImp<JsonElement> profileMenusAdapter;
    private FragmentProfileWithShimmerBinding binding;
    private ProfileViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentProfileWithShimmerBinding.inflate(inflater, container, false);

            handleShimmer(binding.shimmer, binding.main.rootContainer, true);

            binding.main.setLifecycleOwner(this);
            binding.main.setViewModel(viewModel);

            initViews();
            bindData();
        } else viewModel.fetchData();

        return binding.getRoot();
    }

    private void initViews() {

        binding.main.recycler.addItemDecoration(new DividerDecor(requireContext(), 8, DividerDecor.HORIZONTAL));

        adapter = new GenericListAdapterImp<>();
        adapter.setLayoutRes(R.layout.list_item_order);
        // subscribe on click item
        adapter.subscribeCallbacks((item, position) ->
                navigator.navigate(
                        binding.getRoot(),
                        ((JsonObject) item).get("url").getAsString(),
                        this::handleDefaultNavigationState)
        );

        binding.main.recycler.setAdapter(adapter);

        profileMenusAdapter = new GenericListAdapterImp<>();
        profileMenusAdapter.setLayoutRes(R.layout.list_item_profile_menus);
        profileMenusAdapter.subscribeCallbacks((item, position) -> {
            if (((JsonObject) item).get("action").getAsJsonObject().get("name").getAsString().equals("Logout")) {
                binding.getRoot().setAlpha(.4f);
                viewModel.logout();
            } else
                navigator.navigate(
                        requireActivity(),
                        ((JsonObject) item).get("link").getAsString(),
                        this::handleDefaultNavigationState);
        });
        binding.main.recyclerMenus.addItemDecoration(new DividerDecor(requireContext(), 24, DividerDecor.VERTICAL));

        binding.main.recyclerMenus.setAdapter(profileMenusAdapter);

    }

    @SuppressLint("FragmentLiveDataObserve")
    private void bindData() {
        viewModel.resolveData(false);

        viewModel.getData().observe(this, jsonObject -> {
            JsonObject userInfo = jsonObject.get("userInfo").getAsJsonObject();
            if (JsonHelper.has(jsonObject, "customerClub") && JsonHelper.isNotEmptyNorNull(jsonObject.get("customerClub").getAsJsonObject())) {
                JsonObject customerClub = jsonObject.get("customerClub").getAsJsonObject();
                bindClubInfo(customerClub);
            } else binding.main.clubSection.setVisibility(View.GONE);

            updateUI(userInfo);
            JsonArray orderSummary = viewModel.convertJsonObjectToJsonArray(jsonObject.get("orderSummery").getAsJsonObject());
            profileMenusAdapter.submitList(jsonObject.get("userMenus").getAsJsonArray());
            adapter.submitList(orderSummary);

            handleShimmer(binding.shimmer, binding.main.rootContainer, false);
        });

        viewModel.onLogoutResponse().observe(this, response -> {
            binding.getRoot().setAlpha(1);
            if (!response.isError()) {
                UiComponents.showSnack(requireActivity(), getString(R.string.logout_success));
                // navigate to splash screen
//                navigator.clearBackStack(requireActivity());
//                ((MainActivity) requireActivity()).initializeApp();
            } else
                UiComponents.showSnack(requireActivity(), getString(R.string.logout_error));
        });

    }

    private void bindClubInfo(JsonObject customerClub) {
        String points = customerClub.get("user_points").getAsJsonObject().get("value").getAsString();
        JsonObject action = customerClub.get("link").getAsJsonObject();
        String text = action.get("text").getAsString();

        binding.main.setItem(action);
        binding.main.points.setText(points);
        binding.main.buttonEnterClub.setText(text);
        binding.main.buttonEnterClubLayout.setOnClickListener(v -> {

            navigator.navigate(requireActivity(),
                    action.get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });

        // binding.main.executePendingBindings();
    }

    private void updateUI(JsonObject userInfo) {
        String nameLabel = userInfo.get("name").getAsJsonObject().get("label").getAsString();
        String name = userInfo.get("name").getAsJsonObject().get("value").getAsString();

        String mobileLabel = userInfo.get("mobile").getAsJsonObject().get("label").getAsString();
        String mobile = userInfo.get("mobile").getAsJsonObject().get("value").getAsString();

        String emailLabel = userInfo.get("email").getAsJsonObject().get("label").getAsString();
        String email = userInfo.get("email").getAsJsonObject().get("value").getAsString();

        binding.main.userNameTitle.setText(nameLabel);
        binding.main.userName.setText(name);
        binding.main.userPhoneTitle.setText(mobileLabel);
        binding.main.userPhone.setText(mobile);
        binding.main.userEmailTitle.setText(emailLabel);
        binding.main.userEmail.setText(email);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
