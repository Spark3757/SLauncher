package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.util.FutureCallback;

import java.util.concurrent.CompletableFuture;

public class InputDialogPane extends StackPane {
    private final CompletableFuture<String> future = new CompletableFuture<>();

    @FXML
    private JFXButton acceptButton;
    @FXML
    private JFXButton cancelButton;
    @FXML
    private Label title;
    @FXML
    private VBox vbox;
    @FXML
    private Label lblCreationWarning;
    @FXML
    private SpinnerPane acceptPane;

    public InputDialogPane(String text, String initialValue, FutureCallback<String> onResult) {
        FXUtils.loadFXML(this, "/assets/fxml/input-dialog.fxml");
        title.setText(text);
        JFXTextField textField = new JFXTextField();
        textField.setText(initialValue);
        vbox.getChildren().setAll(textField);
        cancelButton.setOnMouseClicked(e -> fireEvent(new DialogCloseEvent()));
        acceptButton.setOnMouseClicked(e -> {
            acceptPane.showSpinner();

            onResult.call(textField.getText(), () -> {
                acceptPane.hideSpinner();
                future.complete(textField.getText());
                fireEvent(new DialogCloseEvent());
            }, msg -> {
                acceptPane.hideSpinner();
                lblCreationWarning.setText(msg);
            });
        });
    }

    public CompletableFuture<String> getCompletableFuture() {
        return future;
    }
}
