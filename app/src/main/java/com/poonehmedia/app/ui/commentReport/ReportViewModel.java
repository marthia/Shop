package com.poonehmedia.app.ui.commentReport;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.CommentResponse;
import com.poonehmedia.app.data.repository.CommentsRepository;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ReportViewModel extends BaseViewModel {

    private final CommentsRepository repository;
    private final DataController dataController;
    private final MutableLiveData<Boolean> isFinished = new MutableLiveData<>();
    private final MutableLiveData<String> submitMessage = new MutableLiveData<>();

    @Inject
    public ReportViewModel(CommentsRepository repository, DataController dataController, RoutePersistence persistence, SavedStateHandle savedStateHandle) {
        super(persistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
    }

    public void submitReport(JsonObject link, String name, String body) {

        requestData(repository.reportInappropriateContent(link, name, body),
                response -> {
                    dataController.onSuccess(response);

                    CommentResponse commentResponse = dataController.extractFunctionMessage(response);

                    boolean isError = !Strings.isNullOrEmpty(commentResponse.getErrorMessage());

                    isFinished.postValue(!isError);
                    submitMessage.postValue(commentResponse.getReportMessage());

                }, throwable -> {
                    dataController.onFailure(throwable);
                    isFinished.postValue(true);
                });
    }

    public LiveData<String> getSubmitMessage() {
        return submitMessage;
    }

    public LiveData<Boolean> getIsFinished() {
        return isFinished;
    }
}
