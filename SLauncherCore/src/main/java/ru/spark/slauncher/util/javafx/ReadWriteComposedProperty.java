package ru.spark.slauncher.util.javafx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

import java.util.function.Consumer;

/**
 * @author Spark1337
 */
public class ReadWriteComposedProperty<T> extends SimpleObjectProperty<T> {

    @SuppressWarnings("unused")
    private final ObservableValue<T> readSource;
    private final Consumer<T> writeTarget;

    private ChangeListener<T> listener;

    public ReadWriteComposedProperty(ObservableValue<T> readSource, Consumer<T> writeTarget) {
        this(null, "", readSource, writeTarget);
    }

    public ReadWriteComposedProperty(Object bean, String name, ObservableValue<T> readSource, Consumer<T> writeTarget) {
        super(bean, name);
        this.readSource = readSource;
        this.writeTarget = writeTarget;

        this.listener = (observable, oldValue, newValue) -> set(newValue);
        readSource.addListener(new WeakChangeListener<>(listener));
        set(readSource.getValue());
    }

    @Override
    protected void invalidated() {
        writeTarget.accept(get());
    }
}
