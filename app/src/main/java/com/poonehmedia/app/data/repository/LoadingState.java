package com.poonehmedia.app.data.repository;

public interface LoadingState {

    void loading(States states);

    enum States {LOADING, ERROR, EMPTY, SUCCESS}
}
