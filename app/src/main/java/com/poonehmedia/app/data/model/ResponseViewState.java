package com.poonehmedia.app.data.model;

public class ResponseViewState {
    public static Throwable throwable;
    public static Object data;

    public static class Loading extends ResponseViewState {
    }

    public static class Error extends ResponseViewState {

        public Error(Throwable throwable) {
            ResponseViewState.throwable = throwable;
        }
    }

    public static class Success extends ResponseViewState {
        public Success(Object data) {
            ResponseViewState.data = data;
        }
    }
}
