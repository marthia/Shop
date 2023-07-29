package com.poonehmedia.app.data.model;

public class RequestException {
    private final String message;
    private final String buttonText;

    public RequestException(String message, String buttonText) {
        this.message = message;
        this.buttonText = buttonText;
    }

    public String getMessage() {
        return message;
    }

    public String getButtonText() {
        return buttonText;
    }
}
