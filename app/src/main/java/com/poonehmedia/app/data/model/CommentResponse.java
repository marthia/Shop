package com.poonehmedia.app.data.model;

public class CommentResponse {
    private String message;
    private String errorMessage;
    private String quoteMessage;
    private String voteMessage;
    private String reportMessage;

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    public String getVoteMessage() {
        return voteMessage;
    }

    public void setVoteMessage(String voteMessage) {
        this.voteMessage = voteMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getQuoteMessage() {
        return quoteMessage;
    }

    public void setQuoteMessage(String quoteMessage) {
        this.quoteMessage = quoteMessage;
    }
}
