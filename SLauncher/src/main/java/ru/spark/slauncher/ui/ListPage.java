package ru.spark.slauncher.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;

public abstract class ListPage<T extends Node> extends ListPageBase<T> {
    private final BooleanProperty refreshable = new SimpleBooleanProperty(this, "refreshable", false);

    public abstract void add();

    public void refresh() {
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ListPageSkin(this);
    }

    public boolean isRefreshable() {
        return refreshable.get();
    }

    public BooleanProperty refreshableProperty() {
        return refreshable;
    }

    public void setRefreshable(boolean refreshable) {
        this.refreshable.set(refreshable);
    }
}
