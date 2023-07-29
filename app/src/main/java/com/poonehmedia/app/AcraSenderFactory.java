package com.poonehmedia.app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.poonehmedia.app.data.framework.LoggerDatabase;
import com.poonehmedia.app.util.base.DeviceInfoManager;

import org.acra.config.CoreConfiguration;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

public class AcraSenderFactory implements ReportSenderFactory {

    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull CoreConfiguration config) {
        return new AcraSender(
                config,
                HttpSender.Method.POST,
                StringFormat.JSON,
                getUrl(),
                new DeviceInfoManager(context),
                getLoggerDatabase(context)
        );
    }

    public LoggerDatabase getLoggerDatabase(Context context) {
        return Room.databaseBuilder(context,
                LoggerDatabase.class, "logger_debug_database").build();
    }

    @Override
    public boolean enabled(@NonNull CoreConfiguration config) {
        return true;
    }

    public String getUrl() {
        return BuildConfig.baseUrl + "index.php?option=com_rppamspro&task=device.submitcrash&rppjson=1&debug=1&type=raw&tmpl=component";
    }

}