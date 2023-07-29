package com.poonehmedia.app.components.adaptiveTable;


class DataSetObserverProxy implements AdaptiveTableDataSetObserver {
    private final AdaptiveTableDataSetObserver mAdaptiveTableAdapter;

    DataSetObserverProxy(AdaptiveTableAdapter adaptiveTableAdapter) {
        mAdaptiveTableAdapter = adaptiveTableAdapter;
    }

    @Override
    public void notifyDataSetChanged() {
        mAdaptiveTableAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyLayoutChanged() {
        mAdaptiveTableAdapter.notifyLayoutChanged();
    }

    @Override
    public void notifyItemChanged(int rowIndex, int columnIndex) {
        mAdaptiveTableAdapter.notifyItemChanged(rowIndex, columnIndex);
    }

    @Override
    public void notifyRowChanged(int rowIndex) {
        mAdaptiveTableAdapter.notifyRowChanged(rowIndex);
    }

    @Override
    public void notifyColumnChanged(int columnIndex) {
        mAdaptiveTableAdapter.notifyColumnChanged(columnIndex);
    }
}
