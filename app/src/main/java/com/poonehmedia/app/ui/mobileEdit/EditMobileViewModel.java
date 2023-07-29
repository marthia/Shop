package com.poonehmedia.app.ui.mobileEdit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.poonehmedia.app.data.repository.EditMobileRepository;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class EditMobileViewModel extends BaseViewModel {

    private final EditMobileRepository repository;
    private final DataController dataController;
    private final MutableLiveData<Boolean> isValidated = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isConfirmed = new MutableLiveData<>(false);


    @Inject
    public EditMobileViewModel(EditMobileRepository repository,
                               DataController dataController,
                               SavedStateHandle savedStateHandle,
                               RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.dataController = dataController;
    }

    public LiveData<Boolean> getIsValidated() {
        return isValidated;
    }

    public void getValidationCode(String phone) {
        requestData(
                repository.getValidationCode(phone)
                , response -> {
                    dataController.onSuccess(response);
                    boolean isError = dataController.isError(response);
                    isValidated.postValue(!isError);
                },
                error -> {
                    isValidated.postValue(false);
                    dataController.onFailure(error);
                });

    }

    public void validate(String code) {

        requestData(
                repository.validateCode(code)
                , response -> {
                    dataController.onSuccess(response);
                    boolean isError = dataController.isError(response);
                    isConfirmed.postValue(!isError);
                },
                error -> {
                    isConfirmed.postValue(false);
                    dataController.onFailure(error);
                });
    }

    public LiveData<Boolean> isConfirmed() {
        return isConfirmed;
    }
}
