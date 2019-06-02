package ru.spark.slauncher.ui;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import ru.spark.slauncher.ui.construct.DialogCloseEvent;

import static ru.spark.slauncher.Metadata.CHANGELOG_URL;
import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class UpgradeDialog extends JFXDialogLayout {
    private final WebView webView = new WebView();

    public UpgradeDialog(Runnable updateRunnable) {
        {
            setHeading(new Label(i18n("update.changelog")));
        }

        {
            WebView webView = new WebView();
            WebEngine engine = webView.getEngine();
            engine.load(CHANGELOG_URL + config().getUpdateChannel().channelName);
            engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                String url = engine.getLoadWorker().getMessage().trim();
                if (!url.startsWith(CHANGELOG_URL)) {
                    engine.getLoadWorker().cancel();
                    FXUtils.openLink(url);
                }
            });
            setBody(webView);
        }

        {
            JFXButton updateButton = new JFXButton(i18n("update.accept"));
            updateButton.getStyleClass().add("dialog-accept");
            updateButton.setOnMouseClicked(e -> updateRunnable.run());

            JFXButton cancelButton = new JFXButton(i18n("button.cancel"));
            cancelButton.getStyleClass().add("dialog-cancel");
            cancelButton.setOnMouseClicked(e -> fireEvent(new DialogCloseEvent()));

            setActions(updateButton, cancelButton);
        }
    }
}