package com.poonehmedia.app.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.poonehmedia.app.data.repository.AccountRepository;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class ValidationViewModel extends BaseViewModel {

    private final AccountRepository repository;
    private final DataController dataController;
    private final MutableLiveData<Boolean> isApproved = new MutableLiveData<>();


    @Inject
    public ValidationViewModel(AccountRepository repository,
                               DataController dataController,
                               SavedStateHandle savedStateHandle,
                               RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
    }

    public void validate(String code) {
        requestData(
                repository.resetPasswordValidation(code),

                response -> {
                    dataController.onSuccess(response);
                    boolean isError = dataController.isError(response);
                    isApproved.postValue(!isError);
                },
                throwable -> {
                    dataController.onFailure(throwable);
                    isApproved.postValue(false);
                }
        );
    }

    public LiveData<Boolean> isValidate() {
        return isApproved;
    }

}