package com.poonehmedia.app.data.model;

import java.util.HashMap;

public class FilterData extends BaseModel {
    private final HashMap<String, String> selectedValuesLabels = new HashMap<>();
    private final HashMap<String, String> selectedValues = new HashMap<>();
    private String parentId;
    private int parentPosition;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getParentPosition() {
        return parentPosition;
    }

    public void setParentPosition(int parentPosition) {
        this.parentPosition = parentPosition;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public HashMap<String, String> getSelectedValues() {
        return selectedValues;
    }

    public HashMap<String, String> getSelectedValuesLabels() {
        return selectedValuesLabels;
    }
}
