package com.poonehmedia.app.data.model;

import com.google.gson.JsonElement;

public class ParameterItem {
    private String title;
    private JsonElement element;
    private int characteristicsSize;
    private String defaultSelectedItem;
    private boolean visible;
    private boolean isAssistedSelection = false;

    public String getDefaultSelectedItem() {
        return defaultSelectedItem;
    }

    public void setDefaultSelectedItem(String defaultSelectedItem) {
        this.defaultSelectedItem = defaultSelectedItem;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getCharacteristicsSize() {
        return characteristicsSize;
    }

    public void setCharacteristicsSize(int characteristicsSize) {
        this.characteristicsSize = characteristicsSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public JsonElement getElement() {
        return element;
    }

    public void setElement(JsonElement element) {
        this.element = element;
    }

    public boolean isAssistedSelection() {
        return isAssistedSelection;
    }

    public void setAssistedSelection(boolean assistedSelection) {
        isAssistedSelection = assistedSelection;
    }
}
