package com.poonehmedia.app.ui.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.SearchHistory;
import com.poonehmedia.app.databinding.FragmentSearchWithShimmerBinding;
import com.poonehmedia.app.ui.adapter.GenericListAdapterImp;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.base.DataController;
import com.poonehmedia.app.util.ui.AndroidUtils;
import com.poonehmedia.app.util.ui.DelayedTextWatcher;
import com.poonehmedia.app.util.ui.DividerDecor;
import com.poonehmedia.app.util.ui.ItemSpaceDecoration;
import com.poonehmedia.app.util.ui.UiComponents;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;

@SuppressLint("FragmentLiveDataObserve")
@AndroidEntryPoint
public class SearchFragment extends BaseFragment {

    private static final int QUERY_MIN_LENGTH = 2;
    public SearchPagedListAdapter adapter;
    @Inject
    public DataController dataController;
    private FragmentSearchWithShimmerBinding binding;
    private SearchViewModel viewModel;
    private String previousQuery = "";
    private GenericListAdapterImp<SearchHistory> historyAdapter;
    private List<SearchHistory> historySuggestionsList = new ArrayList<>(); // avoid NPE
    private boolean isPopulated = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        isPopulated = binding != null;

        if (binding == null) {
            binding = FragmentSearchWithShimmerBinding.inflate(inflater, container, false);
            init();
            subscribe();
        }

        return binding.getRoot();
    }

    private void doSearch(String query) {
        if (query.length() < QUERY_MIN_LENGTH) return;

        // avoid duplicate submits
        if (query.equals(previousQuery))
            return;

        query = dataController.replaceSpecialCharacters(query);
        previousQuery = query;

        //  save recent queries
        viewModel.saveRecentQuery(query, historySuggestionsList);

        requestSearch(query);

    }

    private void requestSearch(String query) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(viewModel);
        Flowable<PagingData<JsonObject>> flowable = PagingRx.getFlowable(viewModel.fetchData(query));
        PagingRx.cachedIn(flowable, viewModelScope);

        viewModel.addToDispose(
                flowable.forEach(data -> {
                    adapter.submitData(getLifecycle(), data);
                    binding.main.setIsResult(true);
                })
        );
    }


    private void init() {
        adapter = new SearchPagedListAdapter();

        binding.backButton.setOnClickListener(v -> {
//            if (binding.searchView.hasFocus()) {
//                AndroidUtils.hideKeyboardFrom(binding.getRoot());
//                binding.main.rootContainer.requestFocus();
//            } else
            Navigation.findNavController(binding.getRoot()).navigateUp();
        });
        binding.main.btnDeleteHistory.setOnClickListener(v -> viewModel.deleteSearchHistory(historySuggestionsList));

        binding.searchView.setOnFocusChangeListener((v, hasFocus) -> {
            binding.setDividerVisibility(hasFocus);
            binding.main.setIsResult(isPopulated || !hasFocus);
        });

        binding.searchView.addTextChangedListener(new DelayedTextWatcher(this::doSearch, 1500));

        historyAdapter = new GenericListAdapterImp<>();
        historyAdapter.setLayoutRes(R.layout.list_item_search_history);
        binding.main.suggestionsRecycler.addItemDecoration(new ItemSpaceDecoration(getContext(), 8));
        binding.main.suggestionsRecycler.setAdapter(historyAdapter);

        // on history suggestions item click
        historyAdapter.subscribeCallbacks((item, position) -> {
            String query = ((SearchHistory) item).getQuery();
            binding.searchView.setText(query);
            doSearch(query);
            AndroidUtils.hideKeyboardFrom(binding.getRoot());
        });


        // bring up keyboard
        binding.searchView.postDelayed(() -> AndroidUtils.showKeyboard(binding.searchView), 400);

    }

    private void subscribe() {
        adapter.subscribeCallbacks((item, position) -> {

            navigator.navigate(
                    requireActivity(),
                    ((JsonObject) item).get("link").getAsString(),
                    this::handleDefaultNavigationState
            );
        });

        binding.main.recycler.addItemDecoration(new DividerDecor(requireContext(), 16, DividerDecor.VERTICAL));
        binding.main.recycler.setAdapter(adapter);

        viewModel.getLoadingResponse().observe(this, aBoolean -> {
            if (!aBoolean) {
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(View.GONE);
                binding.main.rootContainer.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErrorResponse().observe(this, aBoolean -> {
            if (aBoolean)
                UiComponents.showSnack(requireActivity(), getString(R.string.error_getting_list));
        });

        viewModel.getEmptyResponse().observe(this, aBoolean -> binding.main.setIsEmpty(aBoolean));

        viewModel.fetchSearchHistory();

        viewModel.getSearchHistory().observe(this, list -> {
            historySuggestionsList = list;
            if (list.size() > 0)
                historyAdapter.submitList(list);
            else binding.main.suggestionsLayout.setVisibility(View.GONE);
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().findViewById(R.id.main_toolbar).setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().findViewById(R.id.main_toolbar).setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (viewModel != null)
            viewModel.disposeAll();
    }
}
