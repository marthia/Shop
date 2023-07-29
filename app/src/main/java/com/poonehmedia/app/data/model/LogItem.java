package com.poonehmedia.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "t_log")
public class LogItem {

    @PrimaryKey
    @NonNull
    private String _id;

    @ColumnInfo(name = "log_body")
    private String log;

    public LogItem(@NonNull String _id, String log) {
        this._id = _id;
        this.log = log;
    }

    public @NonNull
    String get_id() {
        return _id;
    }

    public void set_id(@NonNull String _id) {
        this._id = _id;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @NonNull
    @Override
    public String toString() {
        return "LogItem ---------------------------\n" +
                " id  ==> '" + _id + "' \n" +
                " log ==> '" + log + "' \n" +
                "-----------------------------------\n\n";
    }
}
