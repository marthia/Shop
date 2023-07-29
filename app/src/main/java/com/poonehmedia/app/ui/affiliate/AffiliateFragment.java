package com.poonehmedia.app.ui.affiliate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentAffiliateWithShimmerBinding;
import com.poonehmedia.app.ui.adapter.FragmentPagerAdapter;
import com.poonehmedia.app.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AffiliateFragment extends BaseFragment {

    private FragmentAffiliateWithShimmerBinding binding;
    private AffiliateViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavBackStackEntry backStackEntry = getGraphScope(R.id.affiliate_graph);
        viewModel = new ViewModelProvider(backStackEntry, getDefaultViewModelProviderFactory()).get(AffiliateViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentAffiliateWithShimmerBinding.inflate(inflater, container, false);

        viewModel.resolveData();

        viewModel.subscribeData().observe(getViewLifecycleOwner(), this::setUpTabLayout);

        viewModel.isPosting().observe(getViewLifecycleOwner(), loading -> {
            if (loading)
                handleShimmer(binding.shimmer, binding.main.rootContainer, true);
        });

        return binding.getRoot();
    }

    private void setUpTabLayout(JsonObject data) {
        handleShimmer(binding.shimmer, binding.main.rootContainer, false);

        List<Fragment> fragments = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (String key : data.keySet()) {
            JsonObject item = data.get(key).getAsJsonObject();
            Fragment fragment = null;

            switch (key) {
                case "settings":
                    fragment = new AffiliateTabSettingsFragment();
                    break;
                case "stats":
                    fragment = new AffiliateTabStatsFragment();

                    break;
                case "banners":
                    fragment = new AffiliateTabBannersFragment();
                    break;
            }
            if (fragment != null) {
                Bundle bundle = new Bundle();
                bundle.putString("args", item.toString());
                fragment.setArguments(bundle);
                fragments.add(fragment);

                titles.add(item.get("title").getAsString());
            }
        }

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(this, fragments);
        binding.main.viewPagerAddresses.setAdapter(adapter);

        new TabLayoutMediator(
                binding.main.tabLayout,
                binding.main.viewPagerAddresses,
                (tab, position) -> tab.setText(titles.get(position))
        ).attach();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }

}
