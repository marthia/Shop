package com.poonehmedia.app.ui;

import android.util.Log;

import androidx.core.util.Consumer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.poonehmedia.app.CrashReportException;
import com.poonehmedia.app.modules.navigation.NavigationArgs;
import com.poonehmedia.app.modules.navigation.RoutePersistence;

import org.acra.ACRA;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BaseViewModel extends ViewModel {

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final RoutePersistence persistence;
    private final SavedStateHandle savedStateHandle;

    public BaseViewModel(RoutePersistence persistence, SavedStateHandle savedStateHandle) {
        this.persistence = persistence;
        this.savedStateHandle = savedStateHandle;
    }

    /**
     * main method to request data from repository which is method agnostic so that if the implementation
     * changed to read data from anywhere else other than api, we can still use this method as a convenient
     * shortcut to request our data. <p> The only limitation would be the direct usage of Rxjava Singles which may or
     * may not be problem in a future refactoring.
     *
     * @param <T>       server response data structure.
     * @param function  api interface
     * @param onSuccess callback for success result
     * @param onFailure callback for failure messages
     * @author Marthia
     */
    protected <T> void requestData(Single<T> function, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        disposable.add(function.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(onSuccess::accept, onFailure::accept));
    }


    /**
     * main method to request data from repository which is method agnostic so that if the implementation
     * changed to read data from anywhere else other than api, we can still use this method as a convenient
     * shortcut to request our data. <p> The only limitation would be the direct usage of Rxjava Singles which may or
     * may not be problem in a future refactoring.
     *
     * @param <T>       server response data structure.
     * @param function  api interface
     * @param onSuccess callback for success result
     * @param onFailure callback for failure messages
     * @author Marthia
     */
    protected <T> void requestData(Flowable<T> function, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        disposable.add(function.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(onSuccess::accept, onFailure::accept));
    }

    /**
     * @param savedStateHandle injected bundle with arguments
     * @return {@link NavigationArgs} object containing all info of the current page. used mainly for paging for
     * getting limit start key and page size.
     */
    public NavigationArgs resolveArgument(SavedStateHandle savedStateHandle) {
        try {
            String argsKey = savedStateHandle.get("key");
            return persistence.getRoute(argsKey);

        } catch (Exception e) {
            Log.e("resolveArgs", "Could not extract page size arg");
            ACRA.getErrorReporter().handleException(new CrashReportException("Could not extract NavigationArg argument in (BaseViewModel)", e));
            return null;
        }
    }

    protected CompositeDisposable getDisposables() {
        return disposable;
    }

    public void disposeAll() {
        disposable.clear();
        persistence.dispose(savedStateHandle.get("key"));
        Log.i("disposable", "disposed " + getClass());
    }
}
