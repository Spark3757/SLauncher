package ru.spark.slauncher.util.javafx;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

class ReferenceHolder implements InvalidationListener {
    @SuppressWarnings("unused")
    private Object ref;

    public ReferenceHolder(Object ref) {
        this.ref = ref;
    }

    @Override
    public void invalidated(Observable observable) {
        // no-op
    }
}
