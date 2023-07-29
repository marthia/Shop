package com.poonehmedia.app.data.model;

public class AppParams {
    private Float appVersion;
    private boolean isForceUpdate;

    public Float getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(Float appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isForceUpdate() {
        return isForceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        isForceUpdate = forceUpdate;
    }

}
