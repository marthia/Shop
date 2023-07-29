package com.poonehmedia.app.ui.comments;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.paging.Pager;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.poonehmedia.app.R;
import com.poonehmedia.app.data.framework.RestUtils;
import com.poonehmedia.app.data.framework.service.BaseApi;
import com.poonehmedia.app.data.model.Comment;
import com.poonehmedia.app.data.model.CommentResponse;
import com.poonehmedia.app.data.model.CommentsPagingMetaData;
import com.poonehmedia.app.data.repository.CommentsPagingSource;
import com.poonehmedia.app.data.repository.CommentsRepository;
import com.poonehmedia.app.data.repository.LoadingState;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class CommentsViewModel extends BaseViewModel {


    private final MutableLiveData<Boolean> errorResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emptyResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionResult = new MutableLiveData<>();
    private final MutableLiveData<String> quoteResult = new MutableLiveData<>();
    private final MutableLiveData<String> resultMessage = new MutableLiveData<>();
    private final MutableLiveData<String> addCommentText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAddCommentLocked = new MutableLiveData<>();
    private final MutableLiveData<String> addCommentSubtitle = new MutableLiveData<>();
    private final RestUtils restUtils;
    private final DataController dataController;
    private final Context context;
    private final MutableLiveData<Boolean> voteResult = new MutableLiveData<>();
    private final CommentsRepository repository;
    private final BaseApi baseApi;
    private int PAGE_SIZE;
    private String path;
    private JsonObject firstPageData;
    private String pageStartKey;

    @Inject
    public CommentsViewModel(SavedStateHandle savedStateHandle,
                             RoutePersistence routePersistence,
                             CommentsRepository repository,
                             RestUtils restUtils,
                             DataController dataController,
                             BaseApi baseApi,
                             @ApplicationContext Context context
    ) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.restUtils = restUtils;
        this.dataController = dataController;
        this.baseApi = baseApi;
        this.context = context;

        NavigationArgs navigationArgs = resolveArgument(savedStateHandle);
        if (navigationArgs != null) {
            this.path = navigationArgs.getLink();
            this.PAGE_SIZE = navigationArgs.getPageSize();
            this.firstPageData = (JsonObject) navigationArgs.getData();
            this.pageStartKey = navigationArgs.getPagingStartKey();
        }
    }

    public Pager<Integer, Comment> fetchData(boolean isRefreshCall) {
        CommentsPagingSource pagingSource = new CommentsPagingSource(baseApi,
                dataController,
                restUtils,
                path,
                states -> {
                    if (states.equals(LoadingState.States.SUCCESS)) {
                        loadingResponse.postValue(false);
                        errorResponse.postValue(false);
                        emptyResponse.postValue(false);
                    } else {
                        loadingResponse.postValue(states.equals(LoadingState.States.LOADING));
                        errorResponse.postValue(states.equals(LoadingState.States.ERROR));
                        emptyResponse.postValue(states.equals(LoadingState.States.EMPTY));
                    }
                },
                o -> {
                    if (o != null) {
                        CommentsPagingMetaData metaData = (CommentsPagingMetaData) o;
                        repository.setAddCommentObj(metaData.getAddComment());
                        repository.setSubmitLocked(metaData.isLocked());

                        isAddCommentLocked.postValue(metaData.isLocked());

                        if (metaData.isLocked()) {
                            isAddCommentLocked.postValue(true);
                            addCommentSubtitle.postValue(metaData.getLockedText());
                        } else {
                            addCommentText.postValue(metaData.getAddComment().get("text").getAsString());
                            addCommentSubtitle.postValue(context.getString(R.string.comments_subtitle));
                            isAddCommentLocked.postValue(false);
                        }

                    }
                },
                isRefreshCall ? null : firstPageData,
                PAGE_SIZE,
                pageStartKey
        );

        return repository.get(pagingSource, PAGE_SIZE);
    }

    // TODO THE WHOLE STRUCTURE MUST BE REVISED
    public void doAction(JsonObject link) {
        requestData(repository.doAction(link),
                response -> {
                    if (response.isSuccessful()) {

                        CommentResponse commentResponse = dataController.extractFunctionMessage(response);

                        boolean isError = !Strings.isNullOrEmpty(commentResponse.getErrorMessage());

                        if (!Strings.isNullOrEmpty(commentResponse.getQuoteMessage())) {
                            quoteResult.postValue(commentResponse.getQuoteMessage());
                        } else if (!isError && !Strings.isNullOrEmpty(commentResponse.getVoteMessage())) {
                            resultMessage.postValue("با موفقیت ثبت شد");
                            voteResult.postValue(true);
                        } else {
                            resultMessage.postValue(commentResponse.getMessage());
                            actionResult.postValue(true);
                        }
                    } else actionResult.postValue(false);
                },
                dataController::onFailure);
    }

    public LiveData<String> getActionResultMessage() {
        return resultMessage;
    }


    public LiveData<String> getAddCommentSubtitle() {
        return addCommentSubtitle;
    }

    public LiveData<Boolean> getAddCommentLocked() {
        return isAddCommentLocked;
    }

    public LiveData<String> getQuoteResult() {
        return quoteResult;
    }

    public LiveData<Boolean> getLoadingResponse() {
        return loadingResponse;
    }

    public LiveData<Boolean> getEmptyResponse() {
        return emptyResponse;
    }

    public LiveData<Boolean> getErrorResponse() {
        return errorResponse;
    }

    public LiveData<Boolean> getVoteResult() {
        return voteResult;
    }
}
