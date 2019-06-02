package ru.spark.slauncher.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.upgrade.UpdateChecker;

import static ru.spark.slauncher.ui.FXUtils.newImage;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

/**
 * @author Spark1337
 */
public class CrashWindow extends Stage {

    public CrashWindow(String text) {
        Label lblCrash = new Label();
        if (UpdateChecker.isOutdated())
            lblCrash.setText(i18n("launcher.crash_out_dated"));
        else
            lblCrash.setText(i18n("launcher.crash"));
        lblCrash.setWrapText(true);

        TextArea textArea = new TextArea();
        textArea.setText(text);
        textArea.setEditable(false);

        Button btnContact = new Button();
        btnContact.setText(i18n("launcher.contact"));
        btnContact.setOnMouseClicked(event -> FXUtils.openLink(Metadata.CONTACT_URL));
        HBox box = new HBox();
        box.setStyle("-fx-padding: 8px;");
        box.getChildren().add(btnContact);
        box.setAlignment(Pos.CENTER_RIGHT);

        BorderPane pane = new BorderPane();
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-padding: 8px;");
        stackPane.getChildren().add(lblCrash);
        pane.setTop(stackPane);
        pane.setCenter(textArea);
        pane.setBottom(box);

        Scene scene = new Scene(pane, 800, 480);
        setScene(scene);
        getIcons().add(newImage("/assets/img/icon.png"));
        setTitle(i18n("message.error"));

        setOnCloseRequest(e -> System.exit(1));
    }

}
