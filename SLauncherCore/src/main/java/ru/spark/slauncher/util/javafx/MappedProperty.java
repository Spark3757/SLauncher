package ru.spark.slauncher.util.javafx;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.util.function.Function;

/**
 * @author Spark1337
 */
public class MappedProperty<T, U> extends SimpleObjectProperty<U> {

    private final Property<T> predecessor;
    private final Function<U, T> reservedMapper;

    private final ObjectBinding<U> binding;

    public MappedProperty(Property<T> predecessor, Function<T, U> mapper, Function<U, T> reservedMapper) {
        this(null, "", predecessor, mapper, reservedMapper);
    }

    public MappedProperty(Object bean, String name, Property<T> predecessor, Function<T, U> mapper, Function<U, T> reservedMapper) {
        super(bean, name);
        this.predecessor = predecessor;
        this.reservedMapper = reservedMapper;

        binding = new ObjectBinding<U>() {
            {
                bind(predecessor);
            }

            @Override
            protected U computeValue() {
                return mapper.apply(predecessor.getValue());
            }

            @Override
            protected void onInvalidating() {
                MappedProperty.this.fireValueChangedEvent();
            }
        };
    }

    @Override
    public U get() {
        return binding.get();
    }

    @Override
    public void set(U value) {
        predecessor.setValue(reservedMapper.apply(value));
    }

    @Override
    public void bind(ObservableValue<? extends U> observable) {
        predecessor.bind(Bindings.createObjectBinding(() -> reservedMapper.apply(observable.getValue()), observable));
    }

    @Override
    public void unbind() {
        predecessor.unbind();
    }

    @Override
    public boolean isBound() {
        return predecessor.isBound();
    }
}
