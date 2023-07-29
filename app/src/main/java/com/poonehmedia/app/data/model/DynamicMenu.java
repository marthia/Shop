package com.poonehmedia.app.data.model;

public class DynamicMenu {
    private int id;
    private String title;
    private String icon;
    private boolean isSolidIcon;
    private int textSize;
    private int iconColor;

    public DynamicMenu(int id, String title, String icon, boolean isSolidIcon, int textSize, int iconColor) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.isSolidIcon = isSolidIcon;
        this.textSize = textSize;
        this.iconColor = iconColor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isSolidIcon() {
        return isSolidIcon;
    }

    public void setSolidIcon(boolean solidIcon) {
        isSolidIcon = solidIcon;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
    }

}
