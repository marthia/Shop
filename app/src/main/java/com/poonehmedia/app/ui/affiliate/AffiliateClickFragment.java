package com.poonehmedia.app.ui.affiliate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.databinding.FragmentAffiliateClickFragmentBinding;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.UiComponents;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;

@AndroidEntryPoint
public class AffiliateClickFragment extends BaseFragment {

    @Inject
    public AffiliateClickPager adapter;
    @Inject
    public RoutePersistence routePersistence;
    private AffiliateViewModel viewModel;
    private FragmentAffiliateClickFragmentBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavBackStackEntry backStackEntry = getGraphScope(R.id.affiliate_graph);
        viewModel = new ViewModelProvider(backStackEntry, getDefaultViewModelProviderFactory()).get(AffiliateViewModel.class);
    }

    @SuppressLint("FragmentLiveDataObserve")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAffiliateClickFragmentBinding.inflate(inflater, container, false);

        init();

        fetchData(false);

        viewModel.getErrorResponse().observe(this, aBoolean -> {
            if (aBoolean) {
                UiComponents.showSnack(requireActivity(), getString(R.string.error_getting_list));
            }
        });

        viewModel.getEmptyResponse().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.emptyText.setVisibility(View.VISIBLE);
                binding.emptyImage.setVisibility(View.VISIBLE);
                binding.recycler.setVisibility(View.GONE);
            } else {
                binding.emptyText.setVisibility(View.GONE);
                binding.emptyImage.setVisibility(View.GONE);
                binding.recycler.setVisibility(View.VISIBLE);
            }
        });

        return binding.getRoot();
    }

    private void fetchData(boolean isRefreshCall) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(viewModel);
        Flowable<PagingData<JsonObject>> flow = PagingRx.getFlowable(viewModel.fetchClickData(routePersistence.getRoute(((String) getArguments().get("key"))).getLink()));
        PagingRx.cachedIn(flow, viewModelScope);
        flow.forEach(data -> adapter.submitData(getLifecycle(), data));
    }

    private void init() {
        String actionName = getActionName();
        int layoutRes = R.layout.list_item_affiliate_click;

        if (actionName != null && actionName.contains("Leads")) {
            layoutRes = R.layout.list_item_affiliate_leads;
        } else if (actionName != null && actionName.contains("Sales")) {
            layoutRes = R.layout.list_item_affiliate_sales;
        }

        adapter.setLayoutRes(layoutRes);

        binding.recycler.setAdapter(adapter);
    }
}
