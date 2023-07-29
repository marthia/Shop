package com.poonehmedia.app.data.model;

import java.util.Map;

public class CompareResult {
    private final Map<Integer, Map<Integer, CompareItem>> data;
    private final Map<Integer, Integer> metaData;

    public CompareResult(Map<Integer, Map<Integer, CompareItem>> data, Map<Integer, Integer> metaData) {
        this.data = data;
        this.metaData = metaData;
    }

    public Map<Integer, Map<Integer, CompareItem>> getData() {
        return data;
    }

    public Map<Integer, Integer> getMetaData() {
        return metaData;
    }
}
