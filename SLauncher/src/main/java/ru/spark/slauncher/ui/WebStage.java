package ru.spark.slauncher.ui;

import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import ru.spark.slauncher.setting.ConfigHolder;

import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.ui.FXUtils.newImage;

public class WebStage extends Stage {
    protected final StackPane pane = new StackPane();
    protected final JFXProgressBar progressBar = new JFXProgressBar();
    protected final WebView webView = new WebView();
    protected final WebEngine webEngine = webView.getEngine();

    public WebStage() {
        this(800, 480);
    }

    public WebStage(int width, int height) {
        setScene(new Scene(pane, width, height));
        getScene().getStylesheets().addAll(config().getTheme().getStylesheets());
        getIcons().add(newImage("/assets/img/icon.png"));
        webView.setContextMenuEnabled(false);
        titleProperty().bind(webEngine.titleProperty());

        progressBar.progressProperty().bind(webView.getEngine().getLoadWorker().progressProperty());

        progressBar.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            switch (webView.getEngine().getLoadWorker().getState()) {
                case SUCCEEDED:
                case FAILED:
                case CANCELLED:
                    return false;
                default:
                    return true;
            }
        }, webEngine.getLoadWorker().stateProperty()));

        BorderPane borderPane = new BorderPane();
        borderPane.setPickOnBounds(false);
        borderPane.setTop(progressBar);
        progressBar.prefWidthProperty().bind(borderPane.widthProperty());
        pane.getChildren().setAll(webView, borderPane);
    }

    public WebView getWebView() {
        return webView;
    }
}