package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.scene.text.Font;
import ru.spark.slauncher.util.javafx.BindingMapping;

import static javafx.collections.FXCollections.*;

public class FontComboBox extends JFXComboBox<String> {

    private boolean loaded = false;

    public FontComboBox(@NamedArg(value = "fontSize", defaultValue = "12.0") double fontSize) {
        styleProperty().bind(Bindings.concat("-fx-font-family: \"", valueProperty(), "\""));

        setCellFactory(listView -> new JFXListCell<String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setText(item);
                    setGraphic(null);
                    setStyle("-fx-font-family: \"" + item + "\"");
                }
            }
        });

        itemsProperty().bind(BindingMapping.of(valueProperty())
                .map(value -> value == null ? emptyObservableList() : singletonObservableList(value)));

        setOnMouseClicked(e -> {
            if (loaded)
                return;
            itemsProperty().unbind();
            setItems(observableList(Font.getFamilies()));
            loaded = true;
        });
    }
}
