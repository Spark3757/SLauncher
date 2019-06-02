package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class JFXCheckBoxTreeTableCell<S, T> extends TreeTableCell<S, T> {
    private final StackPane pane;
    private final CheckBox checkBox;
    private boolean showLabel;
    private ObservableValue<Boolean> booleanProperty;
    private ObjectProperty<StringConverter<T>> converter =
            new SimpleObjectProperty<StringConverter<T>>(this, "converter") {
                protected void invalidated() {
                    updateShowLabel();
                }
            };
    private ObjectProperty<Callback<Integer, ObservableValue<Boolean>>>
            selectedStateCallback =
            new SimpleObjectProperty<Callback<Integer, ObservableValue<Boolean>>>(
                    this, "selectedStateCallback");

    public JFXCheckBoxTreeTableCell() {
        this(null, null);
    }

    public JFXCheckBoxTreeTableCell(
            final Callback<Integer, ObservableValue<Boolean>> getSelectedProperty) {
        this(getSelectedProperty, null);
    }

    public JFXCheckBoxTreeTableCell(
            final Callback<Integer, ObservableValue<Boolean>> getSelectedProperty,
            final StringConverter<T> converter) {
        this.getStyleClass().add("check-box-tree-table-cell");
        this.checkBox = new JFXCheckBox();
        this.pane = new StackPane(checkBox);
        this.pane.setAlignment(Pos.CENTER);
        setGraphic(null);
        setSelectedStateCallback(getSelectedProperty);
        setConverter(converter);
    }

    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    public final StringConverter<T> getConverter() {
        return converterProperty().get();
    }

    public final void setConverter(StringConverter<T> value) {
        converterProperty().set(value);
    }

    public final ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> selectedStateCallbackProperty() {
        return selectedStateCallback;
    }

    public final Callback<Integer, ObservableValue<Boolean>> getSelectedStateCallback() {
        return selectedStateCallbackProperty().get();
    }

    public final void setSelectedStateCallback(Callback<Integer, ObservableValue<Boolean>> value) {
        selectedStateCallbackProperty().set(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            StringConverter<T> c = getConverter();

            if (showLabel) {
                setText(c.toString(item));
            }

            setGraphic(pane);

            if (booleanProperty instanceof BooleanProperty) {
                checkBox.selectedProperty().unbindBidirectional((BooleanProperty) booleanProperty);
            }
            ObservableValue<?> obsValue = getSelectedProperty();
            if (obsValue instanceof BooleanProperty) {
                booleanProperty = (ObservableValue<Boolean>) obsValue;
                checkBox.selectedProperty().bindBidirectional((BooleanProperty) booleanProperty);
            }

            checkBox.disableProperty().bind(Bindings.not(
                    getTreeTableView().editableProperty().and(
                            getTableColumn().editableProperty()).and(
                            editableProperty())
            ));
        }
    }

    private void updateShowLabel() {
        this.showLabel = converter != null;
        this.checkBox.setAlignment(showLabel ? Pos.CENTER_LEFT : Pos.CENTER);
    }

    private ObservableValue<?> getSelectedProperty() {
        return getSelectedStateCallback() != null ?
                getSelectedStateCallback().call(getIndex()) :
                getTableColumn().getCellObservableValue(getIndex());
    }
}