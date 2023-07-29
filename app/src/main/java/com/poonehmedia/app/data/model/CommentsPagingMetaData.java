package com.poonehmedia.app.data.model;

import com.google.gson.JsonObject;

public class CommentsPagingMetaData {
    private JsonObject addComment;
    private boolean isLocked;
    private String lockedText;

    public CommentsPagingMetaData(JsonObject addComment, boolean isLocked, String lockedText) {
        this.addComment = addComment;
        this.isLocked = isLocked;
        this.lockedText = lockedText;
    }

    public JsonObject getAddComment() {
        return addComment;
    }

    public void setAddComment(JsonObject addComment) {
        this.addComment = addComment;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getLockedText() {
        return lockedText;
    }

    public void setLockedText(String lockedText) {
        this.lockedText = lockedText;
    }
}
