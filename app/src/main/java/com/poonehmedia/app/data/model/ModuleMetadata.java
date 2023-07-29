package com.poonehmedia.app.data.model;

import com.google.gson.JsonObject;

public class ModuleMetadata {
    private String title;
    private String subtitle;
    private String description;
    private String iconLink;
    private String readMoreText;
    private Runnable readMoreAction;
    private JsonObject readMoreActionObject;
    private String readMoreTextColor;
    private String backgroundColor;
    private String backgroundImage;
    private String titleColor;
    private String subtitleColor;
    private String thumbWidth;
    private String thumbHeight;
    private String thumbRadius = "16";
    private String imagePosition;
    private boolean showPrice = false;
    private boolean showTitle = false;
    private boolean showDate = false;
    private boolean showText = false;
    private boolean showImage = false;
    private boolean clickable = false;
    private int columns = 1;

    public String getThumbRadius() {
        return thumbRadius;
    }

    public void setThumbRadius(String thumbRadius) {
        this.thumbRadius = thumbRadius;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public boolean isShowDate() {
        return showDate;
    }

    public void setShowDate(boolean showDate) {
        this.showDate = showDate;
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public JsonObject getReadMoreActionObject() {
        return readMoreActionObject;
    }

    public void setReadMoreActionObject(JsonObject readMoreActionObject) {
        this.readMoreActionObject = readMoreActionObject;
    }

    public String getReadMoreTextColor() {
        return readMoreTextColor;
    }

    public void setReadMoreTextColor(String readMoreTextColor) {
        this.readMoreTextColor = readMoreTextColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    public String getSubtitleColor() {
        return subtitleColor;
    }

    public void setSubtitleColor(String subtitleColor) {
        this.subtitleColor = subtitleColor;
    }

    public String getReadMoreText() {
        return readMoreText;
    }

    public void setReadMoreText(String readMoreText) {
        this.readMoreText = readMoreText;
    }

    public Runnable getReadMoreAction() {
        return readMoreAction;
    }

    public void setReadMoreAction(Runnable readMoreAction) {
        this.readMoreAction = readMoreAction;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconLink() {
        return iconLink;
    }

    public void setIconLink(String iconLink) {
        this.iconLink = iconLink;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public boolean isShowPrice() {
        return showPrice;
    }

    public void setShowPrice(boolean showPrice) {
        this.showPrice = showPrice;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public String getImagePosition() {
        return imagePosition;
    }

    public void setImagePosition(String imagePosition) {
        this.imagePosition = imagePosition;
    }

    public boolean isShowImage() {
        return showImage;
    }

    public void setShowImage(boolean showImage) {
        this.showImage = showImage;
    }

    public String getThumbHeight() {
        return thumbHeight;
    }

    public void setThumbHeight(String thumbHeight) {
        this.thumbHeight = thumbHeight;
    }

    public String getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth(String thumbWidth) {
        this.thumbWidth = thumbWidth;
    }
}
