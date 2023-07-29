package com.poonehmedia.app.data.framework;

import com.poonehmedia.app.data.model.LogItem;
import com.poonehmedia.app.data.repository.PreferenceManager;
import com.poonehmedia.app.util.base.DeviceInfoManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class HeaderInterceptor implements Interceptor {
    private final PreferenceManager preferenceManager;
    private final DeviceInfoManager deviceInfoManager;
    private LoggerDatabase loggerDatabase;

    @Inject
    public HeaderInterceptor(PreferenceManager preferenceManager,
                             DeviceInfoManager deviceInfoManager,
                             LoggerDatabase loggerDatabase
    ) {
        this.preferenceManager = preferenceManager;
        this.deviceInfoManager = deviceInfoManager;
        this.loggerDatabase = loggerDatabase;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {

        String packageName = deviceInfoManager.getPackageName();
        String deviceId = deviceInfoManager.getUniquePsuedoID(0);

        HttpUrl.Builder urlBuilder = chain.request().url().newBuilder();
        String tag = chain.request().tag(String.class);

        // add the default query parameters to the request based on tag and sharedPreferences values
        HttpUrl.Builder defaultParameters = getDefaultParameters(tag, urlBuilder);

        String token = preferenceManager.getToken();
        if (token != null && !token.isEmpty())
            defaultParameters.addQueryParameter(token, "1");

        HttpUrl url = defaultParameters.build();

        // add default headers to the request
        Request request = chain.request().newBuilder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .addHeader("Device-UID", deviceId)
                .addHeader("Device-Platform", "android")
                .addHeader("App-Package-Name", packageName)
                .addHeader("Cache-Control", "no-cache")
                .url(url)
                .build();

        // save requests
        saveRequest(request.toString());

        return chain.proceed(request);
    }

    private void saveRequest(String log) {

        new Thread(() -> {
            String id = preferenceManager.getSessionId() +
                    " : " +
                    getCurrentTime();
            loggerDatabase.getLoggerDao().insert(new LogItem(id, log));
        }).start();
    }

    private String getCurrentTime() {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(today);
    }

    /**
     * This method is called for every request that is sent through retrofit to add default parameters
     * essential for requests to work
     *
     * @param tag     a request tag
     * @param builder a {@link HttpUrl.Builder} from interceptor's {@link okhttp3.Interceptor.Chain}
     * @return same {@link HttpUrl.Builder} as input with added default parameters
     * @author Marthia
     */
    public HttpUrl.Builder getDefaultParameters(String tag, HttpUrl.Builder builder) {

        boolean debuggingState = preferenceManager.getDebuggingState();
        String debugParam = debuggingState || (tag != null && tag.equals("report_crash")) ? "1" : "0";

        builder.addQueryParameter("rppjson", "1");
        builder.addQueryParameter("debug", debugParam);

        if (tag != null && (tag.equals("updateCart") || tag.equals("comment")))
            builder.addQueryParameter("tmpl", "component");
        else {
            builder.addQueryParameter("type", "raw");
            builder.addQueryParameter("tmpl", "component");
            return builder;
        }

        return builder;
    }
}