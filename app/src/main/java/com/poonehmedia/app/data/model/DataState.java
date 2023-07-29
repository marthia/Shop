package com.poonehmedia.app.data.model;

public class DataState {
    public static String message;
    public static Object data;

    public static class Error extends DataState {
        public Error(String msg) {
            message = msg;
        }
    }

    public static class Success extends DataState {
        public Success(Object obj) {
            data = obj;
        }
    }

    public static class Nothing extends DataState {
        public Nothing() {
        }
    }
}
