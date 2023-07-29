package com.poonehmedia.app.data.model;

import org.jetbrains.annotations.NotNull;

public class FormData {
    private String key;
    private String value;
    private String fieldNameKey;

    public FormData(String key, String value, String fieldNameKey) {
        this.key = key;
        this.value = value;
        this.fieldNameKey = fieldNameKey;
    }

    public String getFieldNameKey() {
        return fieldNameKey;
    }

    public void setFieldNameKey(String fieldNameKey) {
        this.fieldNameKey = fieldNameKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @NotNull
    @Override
    public String toString() {
        return "FormData{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", fieldPostKey='" + fieldNameKey + '\'' +
                '}';
    }
}
