package ru.spark.slauncher.ui;

import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import ru.spark.slauncher.setting.ConfigHolder;

import static ru.spark.slauncher.ui.FXUtils.newImage;

public class WebStage extends Stage {
    private final WebView webView = new WebView();

    public WebStage() {
        setScene(new Scene(webView, 800, 480));
        getScene().getStylesheets().addAll(ConfigHolder.config().getTheme().getStylesheets());
        getIcons().add(newImage("/assets/img/icon.png"));
    }

    public WebView getWebView() {
        return webView;
    }
}
