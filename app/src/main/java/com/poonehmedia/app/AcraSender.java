package com.poonehmedia.app;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poonehmedia.app.data.framework.LoggerDatabase;
import com.poonehmedia.app.data.model.LogItem;
import com.poonehmedia.app.util.base.DeviceInfoManager;

import org.acra.ReportField;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;
import org.acra.sender.ReportSenderException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;

public class AcraSender extends HttpSender {


    private final DeviceInfoManager deviceInfoManager;
    private final LoggerDatabase loggerDatabase;

    public AcraSender(@NonNull CoreConfiguration config, @Nullable Method method, @Nullable StringFormat type, @Nullable String formUri, DeviceInfoManager deviceInfoManager, LoggerDatabase loggerDatabase) {
        super(config, method, type, formUri);
        this.deviceInfoManager = deviceInfoManager;
        this.loggerDatabase = loggerDatabase;
    }

    /**
     * Add custom fields to crash report before sending
     */
    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData report) throws ReportSenderException {
        boolean isCrash = !report.getString(ReportField.STACK_TRACE).contains("TryCatch");
        report.put("isCrash", isCrash);
        report.put("isDebug", BuildConfig.DEBUG);
        report.put("device_details", deviceInfoManager.getDeviceFeatures());
        report.put("logs", getLogs().toString());

        truncateLogDb();
        super.send(context, report);
    }
    // TODO THIS IS A SUGGESTION FOR SERVER IMPLEMENTATION OF ERROR HANDLING : AVOID DUPLICATE STACK_TRACES JUST INCREMENT PRIORITY

    private void truncateLogDb() {
        loggerDatabase.clearAllTables();
    }

    public List<LogItem> getLogs() {
        return loggerDatabase.getLoggerDao().getAll();
    }

    /**
     * Add custom headers to crash report request. <p>
     * This is like what is normally done in {@link com.poonehmedia.app.data.framework.HeaderInterceptor#intercept(Interceptor.Chain)}. but as acra uses its own method for sending requests we must override it here too.
     */
    @Override
    protected void sendHttpRequests(@NonNull CoreConfiguration configuration, @NonNull Context context, @NonNull Method method, @NonNull String contentType, @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers, @NonNull String content, @NonNull URL url, @NonNull List<Uri> attachments) throws IOException {

        String packageName = deviceInfoManager.getPackageName();
        String deviceId = deviceInfoManager.getUniquePsuedoID(0);

        headers = new HashMap<>();

        headers.put("Device-UID", deviceId);

        headers.put("Device-Platform", "android");

        headers.put("App-Package-Name", packageName);

        headers.put("Cache-Control", "no-cache");

        super.sendHttpRequests(configuration, context, method, contentType, login, password, connectionTimeOut, socketTimeOut, headers, content, url, attachments);
    }

}
