package com.poonehmedia.app.ui.comments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.model.Comment;
import com.poonehmedia.app.databinding.BottomBarCommentsBinding;
import com.poonehmedia.app.databinding.FragmentCommentsWithShimmerBinding;
import com.poonehmedia.app.ui.base.BaseFragment;
import com.poonehmedia.app.util.ui.UiComponents;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;

@AndroidEntryPoint
public class CommentsFragment extends BaseFragment {

    public CommentsAdapter adapter;
    private FragmentCommentsWithShimmerBinding binding;
    private CommentsViewModel viewModel;
    private BottomBarCommentsBinding bottomBarBinding;
    private ViewStub stub;
    private View bottomBar;
    private boolean bottomBarVisible = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommentsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentCommentsWithShimmerBinding.inflate(inflater, container, false);
            binding.main.setViewModel(viewModel);
            binding.main.setLifecycleOwner(this);
            init();
            subscribe();
        } else fetchData(true);

        return binding.getRoot();
    }

    @SuppressLint("FragmentLiveDataObserve")
    private void subscribe() {

        viewModel.getLoadingResponse().observe(this, aBoolean -> {
            handleShimmer(binding.shimmer, binding.main.rootContainer, aBoolean);
        });

        viewModel.getErrorResponse().observe(this, aBoolean -> {
            if (aBoolean)
                UiComponents.showSnack(requireActivity(), getString(R.string.error_getting_list));
        });

        viewModel.getEmptyResponse().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.main.emptyText.setVisibility(View.VISIBLE);
                binding.main.emptyImage.setVisibility(View.VISIBLE);
            } else {
                binding.main.emptyText.setVisibility(View.GONE);
                binding.main.emptyImage.setVisibility(View.GONE);
                handleShimmer(binding.shimmer, binding.main.rootContainer, false);
            }
        });

        // messages must be approved by admin first so no need to refresh the list as it has yet to change
        // refresh layout when new comment saved
//        viewModel.getActionResult().observe(this, aBoolean -> {
//            if (aBoolean) viewModel.invalidatePagingSource();
//        });

        viewModel.getVoteResult().observe(this, a -> {
            if (a) {
                fetchData(true);
            }
        });
        viewModel.getQuoteResult().observe(this, s -> {
            navProgress(false);
            navigator.intrinsicNavigate(requireActivity(),
                    CommentsFragmentDirections.actionAddEdit(s.replaceAll("\\\\n", "\n")),
                    false);
        });

        viewModel.getActionResultMessage().observe(this, s -> {
            UiComponents.showSnack(requireActivity(), s);
        });

        viewModel.getAddCommentLocked().observe(this, aBoolean -> {
            bottomBarVisible = aBoolean;
            if (aBoolean) {
                hideBottomBar();
            }
        });
    }

    private void init() {

        binding.main.setOnAddComment(v -> {

            navigator.intrinsicNavigate(requireActivity(),
                    CommentsFragmentDirections.actionAddEdit(""),
                    false);
        });

        adapter = new CommentsAdapter();

        adapter.subscribeClick((item, position) -> {
            handleShimmer(binding.shimmer, binding.main.rootContainer, true);
            viewModel.doAction((JsonObject) item);
        });

        adapter.subscribeReply((item, position) -> {
            viewModel.doAction((JsonObject) item);
            navProgress(true);
        });

        adapter.subscribeReport((item, position) -> {
            Comment comment = (Comment) item;

            JsonObject inappropriateLink = comment.getInappropriateLink();
            inappropriateLink.addProperty("commentId", comment.getId());

            navigator.intrinsicNavigate(requireActivity(),
                    CommentsFragmentDirections.actionReport(inappropriateLink.toString()),
                    false);
        });

        adapter.subscribeShowMessage((item, position) -> {
            UiComponents.showSnack(requireActivity(), getString(R.string.cannot_vote));
        });
        binding.main.recycler.setAdapter(adapter);

        fetchData(false);
    }

    private void initBottomBar() {
        stub = requireActivity().findViewById(R.id.comments_layout_stub);

        if (stub != null) {
            stub.setLayoutResource(R.layout.bottom_bar_comments);
            stub.setOnInflateListener((stub1, inflated) -> inflateBottomBar(inflated));
            stub.inflate();

        } else {
            bottomBar = requireActivity().findViewById(R.id.bottom_bar_comments);
            bottomBar.setVisibility(View.VISIBLE);
            inflateBottomBar(bottomBar);
        }
    }

    private void inflateBottomBar(View inflatedView) {

        // prevent rebinding
        if (bottomBarBinding != null) return;

        bottomBarBinding = DataBindingUtil.bind(inflatedView);
        bottomBarBinding.setLifecycleOwner(this);
        bottomBarBinding.setViewModel(viewModel);
    }

    private void fetchData(boolean isRefreshCall) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(viewModel);
        Flowable<PagingData<Comment>> flow = PagingRx.getFlowable(viewModel.fetchData(isRefreshCall));
        PagingRx.cachedIn(flow, viewModelScope);
        flow.forEach(data -> adapter.submitData(getLifecycle(), data));
    }

    private void hideBottomBar() {
        if (stub != null)
            stub.setVisibility(View.GONE);
        else bottomBar.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideBottomBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bottomBarVisible)
            initBottomBar();
        else
            hideBottomBar();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.disposeAll();
    }
}
