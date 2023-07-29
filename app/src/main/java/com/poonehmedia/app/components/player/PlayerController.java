package com.poonehmedia.app.components.player;

public interface PlayerController {

    void setMuteMode(boolean mute);

    void showProgressBar(boolean visible);

    void showRetryBtn(boolean visible);

    void audioFocus();
}
