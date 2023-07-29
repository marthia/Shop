package com.poonehmedia.app.data.model;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.google.gson.JsonObject;

import java.util.Objects;

public class Comment {
    private String id;
    private String userName;
    private String title;
    private String comment;
    private String date;
    private String rate;
    private String likeCount;
    private String link;
    private String dislikeCount;

    private JsonObject likeLink;
    private JsonObject replyLink;
    private JsonObject dislikeLink;
    private JsonObject inappropriateLink;

    private boolean isLikeEnabled;
    private boolean isDislikeEnabled;
    private boolean isInappropriateEnabled;
    private boolean isReplyEnabled;
    private boolean isRecommend;
    private boolean isLiked;

    public JsonObject getLikeLink() {
        return likeLink;
    }

    public void setLikeLink(JsonObject likeLink) {
        this.likeLink = likeLink;
    }

    public JsonObject getReplyLink() {
        return replyLink;
    }

    public void setReplyLink(JsonObject replyLink) {
        this.replyLink = replyLink;
    }

    public JsonObject getDislikeLink() {
        return dislikeLink;
    }

    public void setDislikeLink(JsonObject dislikeLink) {
        this.dislikeLink = dislikeLink;
    }

    public JsonObject getInappropriateLink() {
        return inappropriateLink;
    }

    public void setInappropriateLink(JsonObject inappropriateLink) {
        this.inappropriateLink = inappropriateLink;
    }

    public boolean isLikeEnabled() {
        return isLikeEnabled;
    }

    public void setLikeEnabled(boolean likeEnabled) {
        isLikeEnabled = likeEnabled;
    }

    public boolean isDislikeEnabled() {
        return isDislikeEnabled;
    }

    public void setDislikeEnabled(boolean dislikeEnabled) {
        isDislikeEnabled = dislikeEnabled;
    }

    public boolean isInappropriateEnabled() {
        return isInappropriateEnabled;
    }

    public void setInappropriateEnabled(boolean inappropriateEnabled) {
        isInappropriateEnabled = inappropriateEnabled;
    }

    public boolean isReplyEnabled() {
        return isReplyEnabled;
    }

    public void setReplyEnabled(boolean replyEnabled) {
        isReplyEnabled = replyEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment1 = (Comment) o;
        return isRecommend() == comment1.isRecommend() &&
                isLiked() == comment1.isLiked() &&
                getId().equals(comment1.getId()) &&
                Objects.equals(getUserName(), comment1.getUserName()) &&
                Objects.equals(getTitle(), comment1.getTitle()) &&
                Objects.equals(getComment(), comment1.getComment()) &&
                Objects.equals(getDate(), comment1.getDate()) &&
                Objects.equals(getRate(), comment1.getRate()) &&
                Objects.equals(getLikeCount(), comment1.getLikeCount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserName(), getTitle(), getComment(), getDate(), getRate(), getLikeCount(), isRecommend(), isLiked());
    }

    public boolean isRecommend() {
        return isRecommend;
    }

    public void setRecommend(boolean recommend) {
        isRecommend = recommend;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(String dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public static class CommentDiff extends DiffUtil.ItemCallback<Comment> {
        @Override
        public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.equals(newItem);
        }
    }
}
