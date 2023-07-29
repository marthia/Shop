package com.poonehmedia.app.ui.comment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.common.base.Strings;
import com.poonehmedia.app.data.model.CommentResponse;
import com.poonehmedia.app.data.repository.CommentsRepository;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CommentViewModel extends BaseViewModel {

    private final CommentsRepository repository;
    private final MutableLiveData<String> resultMessage = new MutableLiveData<>();
    private final DataController dataController;
    private final MutableLiveData<Boolean> isFinished = new MutableLiveData<>();

    @Inject
    public CommentViewModel(CommentsRepository repository, DataController dataController, RoutePersistence persistence, SavedStateHandle savedStateHandle) {
        super(persistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
    }

    public void submitComment(String name, String email, String body) {
        if (repository.isSubmitLocked() || repository.getAddCommentObj() == null)
            return;

        requestData(repository.submitComment(name, email, body),
                response -> {
                    dataController.onSuccess(response);

                    CommentResponse commentResponse = dataController.extractFunctionMessage(response);

                    boolean isError = !Strings.isNullOrEmpty(commentResponse.getErrorMessage());

                    isFinished.postValue(!isError);
                    String message = Strings.isNullOrEmpty(commentResponse.getMessage()) ? "" : commentResponse.getMessage();
                    String error = Strings.isNullOrEmpty(commentResponse.getErrorMessage()) ? "" : commentResponse.getErrorMessage();
                    String finalMessage = "";

                    if (!error.isEmpty())
                        finalMessage += error;
                    if (!finalMessage.isEmpty() && !message.isEmpty())
                        finalMessage += "\n";

                    if (!message.isEmpty())
                        finalMessage += message;

                    resultMessage.postValue(finalMessage);

                }, throwable -> {
                    dataController.onFailure(throwable);
                    isFinished.postValue(true);
                });

    }

    public LiveData<String> getSubmitMessage() {
        return resultMessage;
    }

    public LiveData<Boolean> getIsFinished() {
        return isFinished;
    }
}
