package ru.spark.slauncher.ui;

import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import ru.spark.slauncher.setting.ConfigHolder;

import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.ui.FXUtils.newImage;

public class WebStage extends Stage {
    protected final WebView webView = new WebView();
    protected final WebEngine webEngine = webView.getEngine();

    public WebStage() {
        this(800, 480);
    }

    public WebStage(int width, int height) {
        setScene(new Scene(webView, width, height));
        getScene().getStylesheets().addAll(config().getTheme().getStylesheets());
        getIcons().add(newImage("/assets/img/icon.png"));
        webView.setContextMenuEnabled(false);
        titleProperty().bind(webEngine.titleProperty());
    }

    public WebView getWebView() {
        return webView;
    }
}