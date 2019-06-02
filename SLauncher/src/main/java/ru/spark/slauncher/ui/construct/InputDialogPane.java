package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.util.FutureCallback;

public class InputDialogPane extends StackPane {

    @FXML
    private JFXButton acceptButton;
    @FXML
    private JFXButton cancelButton;
    @FXML
    private JFXTextField textField;
    @FXML
    private Label content;
    @FXML
    private Label lblCreationWarning;
    @FXML
    private SpinnerPane acceptPane;

    public InputDialogPane(String text, FutureCallback<String> onResult) {
        FXUtils.loadFXML(this, "/assets/fxml/input-dialog.fxml");
        content.setText(text);
        cancelButton.setOnMouseClicked(e -> fireEvent(new DialogCloseEvent()));
        acceptButton.setOnMouseClicked(e -> {
            acceptPane.showSpinner();
            onResult.call(textField.getText(), () -> {
                acceptPane.hideSpinner();
                fireEvent(new DialogCloseEvent());
            }, msg -> {
                acceptPane.hideSpinner();
                lblCreationWarning.setText(msg);
            });
        });

        acceptButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !textField.validate(),
                textField.textProperty()
        ));
    }

    public void setInitialText(String text) {
        textField.setText(text);
    }
}
