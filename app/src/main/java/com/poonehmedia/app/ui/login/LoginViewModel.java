package com.poonehmedia.app.ui.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.gson.JsonObject;
import com.poonehmedia.app.data.model.Response;
import com.poonehmedia.app.data.repository.AccountRepository;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.modules.navigation.RoutePersistence;
import com.poonehmedia.app.ui.BaseViewModel;
import com.poonehmedia.app.util.base.DataController;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends BaseViewModel {

    private final AccountRepository repository;
    private final PreferenceManager preferenceManager;
    private final DataController dataController;
    private final MutableLiveData<Response> loginResponse = new MutableLiveData<>();
    private final MutableLiveData<String> signUpResponse = new MutableLiveData<>();
    private final MutableLiveData<String> confirmSingUpResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> resetPasswordValidateResponse = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> resetPasswordDone = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isError = new MutableLiveData<>();

    @Inject
    public LoginViewModel(AccountRepository repository,
                          PreferenceManager preferenceManager,
                          DataController dataController,
                          SavedStateHandle savedStateHandle,
                          RoutePersistence routePersistence) {
        super(routePersistence, savedStateHandle);
        this.repository = repository;
        this.preferenceManager = preferenceManager;
        this.dataController = dataController;
    }

    public void login(String username, String password) {
        // login
        requestData(
                repository.login(username, password),

                response -> {
                    dataController.onSuccess(response);
                    Object data = response.body().getAsJsonObject().get("data");
                    if (!preferenceManager.getUser().isEmpty() && data != null) {
                        loginResponse.postValue(new Response(((JsonObject) data)));
                    } else loginResponse.postValue(new Response("Error"));
                },

                throwable -> {
                    loginResponse.postValue(new Response("Error"));
                    dataController.onFailure(throwable);
                }
        );

    }

    public LiveData<Boolean> getIsError() {
        return isError;
    }

    public LiveData<String> getSignUpResponse() {
        return signUpResponse;
    }

    public LiveData<Response> onLoginResponse() {
        return loginResponse;
    }


    public void signUp(String phone, String password) {

        requestData(
                repository.signUp(phone, password),

                response -> {
                    dataController.onSuccess(response);
                    boolean isError = dataController.isError(response);
                    this.isError.postValue(isError);

                    String message = dataController.getMessageOrEmpty(response.body());
                    if (!message.isEmpty())
                        signUpResponse.postValue(message);

                },

                throwable -> {
                    dataController.onFailure(throwable);
                    this.isError.postValue(true);
                    Log.e("signup", throwable.getMessage());
                }
        );
    }

    public LiveData<Boolean> getResetPasswordValidateResponse() {
        return resetPasswordValidateResponse;
    }

    public void getResetPasswordValidationCode(String phone) {

        requestData(
                repository.getResetPasswordValidationCode(phone),

                response -> {
                    dataController.onSuccess(response);
                    boolean isError = dataController.isError(response);
                    resetPasswordValidateResponse.postValue(!isError);
                },
                throwable -> {
                    resetPasswordValidateResponse.postValue(false);
                    dataController.onFailure(throwable);
                }
        );
    }


    public void finalizeChangePassword(String password) {
        requestData(

                repository.finalizedResetPassword(password),

                response -> {
                    dataController.onSuccess(response);
                    boolean isError = dataController.isError(response);
                    resetPasswordDone.postValue(!isError);
                },

                throwable -> {
                    dataController.onFailure(throwable);
                    resetPasswordDone.postValue(false);
                }
        );
    }

    public void confirmUsername(String code) {
        requestData(

                repository.signupValidate(code),

                response -> {
                    dataController.onSuccess(response);

                    boolean isError = dataController.isError(response);
                    this.isError.postValue(isError);

                    String s = dataController.getMessageOrEmpty(response.body());
                    confirmSingUpResponse.postValue(s);

                },

                throwable -> {
                    dataController.onFailure(throwable);
                    this.isError.postValue(true);
                    Log.e("signupConfirm", throwable.getMessage());
                }
        );
    }

    public LiveData<String> getConfirmResponse() {
        return confirmSingUpResponse;
    }

//    public void saveReturn(JsonObject args) {
//        JsonObject forceRedirect = args.get("return").getAsJsonObject();
//
//        repository.saveReturn(forceRedirect);
//    }

    public LiveData<Boolean> isApproved() {
        return resetPasswordDone;
    }
}
