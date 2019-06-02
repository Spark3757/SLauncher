package ru.spark.slauncher.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;

import java.util.LinkedList;
import java.util.List;

public class WeakListenerHolder {
    private List<Object> refs = new LinkedList<>();

    public WeakListenerHolder() {
    }

    public WeakInvalidationListener weak(InvalidationListener listener) {
        refs.add(listener);
        return new WeakInvalidationListener(listener);
    }

    public <T> WeakChangeListener<T> weak(ChangeListener<T> listener) {
        refs.add(listener);
        return new WeakChangeListener<>(listener);
    }

    public <T> WeakListChangeListener<T> weak(ListChangeListener<T> listener) {
        refs.add(listener);
        return new WeakListChangeListener<>(listener);
    }

    public void add(Object obj) {
        refs.add(obj);
    }

    public boolean remove(Object obj) {
        return refs.remove(obj);
    }
}
