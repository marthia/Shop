package com.poonehmedia.app.data.model;

public class DividerData {
    private final int position;
    private final int max;

    public DividerData(int position, int max) {
        this.position = position;
        this.max = max;
    }

    public int getPosition() {
        return position;
    }

    public int getMax() {
        return max;
    }
}
