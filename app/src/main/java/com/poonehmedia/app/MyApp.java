package com.poonehmedia.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.najva.sdk.NajvaClient;
import com.poonehmedia.app.data.framework.LoggerDatabase;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.util.base.DeviceInfoManager;

import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.annotation.AcraCore;

import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

@AcraCore(buildConfigClass = BuildConfig.class,
        reportSenderFactoryClasses = AcraSenderFactory.class,
        additionalSharedPreferences = {"base_pref"}
)
@HiltAndroidApp
public class MyApp extends Application {

    @Inject
    public DeviceInfoManager deviceInfoManager;
    @Inject
    public PreferenceManager preferenceManager;
    @Inject
    public LoggerDatabase loggerDatabase;

    private String najvaKey;

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate() {
        super.onCreate();

        String sessionId = UUID.randomUUID().toString();
        preferenceManager.saveSessionId(sessionId);

        try {
            RxJavaPlugins.setErrorHandler(e -> Log.i("subscribeRxError", "Undeliverable exception received, not sure what to do: \n" + e.getMessage()));
        } catch (Exception e) {
            Log.e("subscribeRxError", "COULD NOT OVERRIDE");
        }

        najvaKey = deviceInfoManager.getNajvaApiKey();
        if (najvaKey != null)
            registerActivityLifecycleCallbacks(NajvaClient.getInstance(this, null));

        System.setProperty("http.keepAlive", "false");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (najvaKey != null)
            NajvaClient.getInstance().onAppTerminated();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);

    }
}
