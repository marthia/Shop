package com.poonehmedia.app.ui.club;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentCustomerClubWithShimmerBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.ui.modules.ModulesAdapter;
import com.poonehmedia.app.util.base.JsonHelper;
import com.poonehmedia.app.util.ui.DividerDecor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CustomerClubFragment extends BaseFragment {

    @Inject
    public ModulesAdapter modulesAdapter;
    private GenericListAdapterImp<JsonElement> clubMenusAdapter;
    private GenericListAdapterImp<JsonElement> lastActivitiesAdapter;
    private CustomerClubViewModel viewModel;
    private FragmentCustomerClubWithShimmerBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CustomerClubViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCustomerClubWithShimmerBinding.inflate(inflater, container, false);

        handleShimmer(binding.shimmer, binding.main.rootContainer, true);

        init();
        initModules();
        subscribeUi();

        viewModel.resolveData();

        return binding.getRoot();
    }

    private void subscribeUi() {
        viewModel.getData().observe(getViewLifecycleOwner(), jsonObject -> {

            bindLastActivities(jsonObject);

            bindUserProfile(jsonObject);

            bindMenus(jsonObject);

            bindModules(jsonObject);

            handleShimmer(binding.shimmer, binding.main.rootContainer, false);
        });
    }

    private void bindModules(JsonObject jsonObject) {
        if (JsonHelper.has(jsonObject, "otherParts")) {
            modulesAdapter.submitList(jsonObject.get("otherParts").getAsJsonArray());

        } else {
            binding.main.modules.setVisibility(View.GONE);
        }
    }

    private void bindMenus(JsonObject jsonObject) {
        if (JsonHelper.has(jsonObject, "userMenus")) {
            clubMenusAdapter.submitList(jsonObject.get("userMenus").getAsJsonArray());
        }
    }

    private void bindUserProfile(JsonObject jsonObject) {
        if (JsonHelper.has(jsonObject, "userProfile")) {
            binding.main.setItem(jsonObject.get("userProfile").getAsJsonObject());

            if (JsonHelper.has(jsonObject.get("userProfile").getAsJsonObject(), "user_referral")) {
                binding.main.setReferral(jsonObject.get("userProfile").getAsJsonObject().get("user_referral").getAsJsonObject());
                binding.main.referralLink.setSelected(true);
            } else binding.main.referralSection.setVisibility(View.GONE);
        }
    }

    private void bindLastActivities(JsonObject jsonObject) {
        if (JsonHelper.has(jsonObject, "userActivities")) {
            binding.main.lastActivitiesTitle.setText(jsonObject.get("userActivities").getAsJsonObject().get("title").getAsString());
            JsonArray rows = jsonObject.get("userActivities").getAsJsonObject()
                    .get("rows").getAsJsonArray();

            if (rows.size() > 0) lastActivitiesAdapter.submitList(rows);

            else binding.main.lastActivitiesSection.setVisibility(View.GONE);
        }
    }

    private void init() {
        lastActivitiesAdapter = new GenericListAdapterImp<>();
        lastActivitiesAdapter.setLayoutRes(R.layout.list_item_club_last_activities);
        binding.main.lastActivities.setAdapter(lastActivitiesAdapter);

        clubMenusAdapter = new GenericListAdapterImp<>();
        clubMenusAdapter.setLayoutRes(R.layout.list_item_profile_menus);
        clubMenusAdapter.subscribeCallbacks((item, position) -> {
            navigator.navigate(requireActivity(),
                    ((JsonObject) item).get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });
        binding.main.recyclerMenus.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL)
        );

        binding.main.recyclerMenus.setAdapter(clubMenusAdapter);

        binding.main.setOnCopyLink(v -> {
            ShareCompat.IntentBuilder
                    .from(requireActivity())
                    .setText(binding.main.referralLink.getText().toString())
                    .setType("text/plain")
                    .startChooser();
        });
    }

    private void initModules() {
        modulesAdapter.setLang(viewModel.getLanguage());
        modulesAdapter.subscribeCallbacks((item, position) -> {

            navigator.navigate(
                    requireActivity(),
                    ((JsonObject) item).get("link").getAsString(),
                    this::handleDefaultNavigationState
            );

        });

        binding.main.staggeredList.setAdapter(modulesAdapter);
        binding.main.staggeredList.setItemViewCacheSize(10);
        binding.main.staggeredList.setDrawingCacheEnabled(true);
        binding.main.staggeredList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }

}
