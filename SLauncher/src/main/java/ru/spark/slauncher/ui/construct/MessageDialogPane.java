package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;

import java.util.Optional;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class MessageDialogPane extends StackPane {

    @FXML
    private JFXButton acceptButton;
    @FXML
    private JFXButton cancelButton;
    @FXML
    private Label content;
    @FXML
    private Label graphic;
    @FXML
    private Label title;
    @FXML
    private HBox actions;

    public MessageDialogPane(String text, String title, MessageType type, Runnable onAccept) {
        FXUtils.loadFXML(this, "/assets/fxml/message-dialog.fxml");

        if (title != null)
            this.title.setText(title);

        content.setText(text);
        acceptButton.setOnMouseClicked(e -> {
            fireEvent(new DialogCloseEvent());
            Optional.ofNullable(onAccept).ifPresent(Runnable::run);
        });

        actions.getChildren().remove(cancelButton);

        switch (type) {
            case INFORMATION:
                graphic.setGraphic(SVG.infoCircle(Theme.blackFillBinding(), 40, 40));
                break;
            case ERROR:
                graphic.setGraphic(SVG.closeCircle(Theme.blackFillBinding(), 40, 40));
                break;
            case FINE:
                graphic.setGraphic(SVG.checkCircle(Theme.blackFillBinding(), 40, 40));
                break;
            case WARNING:
                graphic.setGraphic(SVG.alert(Theme.blackFillBinding(), 40, 40));
                break;
            case QUESTION:
                graphic.setGraphic(SVG.helpCircle(Theme.blackFillBinding(), 40, 40));
                break;
            default:
                throw new IllegalArgumentException("Unrecognized message box message type " + type);
        }
    }

    public MessageDialogPane(String text, String title, Runnable onAccept, Runnable onCancel) {
        this(text, title, MessageType.QUESTION, onAccept);

        cancelButton.setVisible(true);
        cancelButton.setOnMouseClicked(e -> {
            fireEvent(new DialogCloseEvent());
            Optional.ofNullable(onCancel).ifPresent(Runnable::run);
        });

        acceptButton.setText(i18n("button.yes"));
        cancelButton.setText(i18n("button.no"));

        actions.getChildren().add(cancelButton);
    }

    public enum MessageType {
        ERROR,
        INFORMATION,
        WARNING,
        QUESTION,
        FINE,
        PLAIN,
    }
}
