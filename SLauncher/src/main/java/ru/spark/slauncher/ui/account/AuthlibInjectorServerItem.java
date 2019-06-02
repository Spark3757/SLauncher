package ru.spark.slauncher.ui.account;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorServer;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.SVG;

import java.util.function.Consumer;

public final class AuthlibInjectorServerItem extends BorderPane {
    private final AuthlibInjectorServer server;

    private final Label lblServerName = new Label();
    private final Label lblServerUrl = new Label();

    public AuthlibInjectorServerItem(AuthlibInjectorServer server, Consumer<AuthlibInjectorServerItem> deleteCallback) {
        this.server = server;

        lblServerName.setStyle("-fx-font-size: 15;");
        lblServerUrl.setStyle("-fx-font-size: 10;");

        VBox center = new VBox();
        BorderPane.setAlignment(center, Pos.CENTER);
        center.getChildren().addAll(lblServerName, lblServerUrl);
        setCenter(center);

        JFXButton right = new JFXButton();
        right.setOnMouseClicked(e -> deleteCallback.accept(this));
        right.getStyleClass().add("toggle-icon4");
        BorderPane.setAlignment(right, Pos.CENTER);
        right.setGraphic(SVG.close(Theme.blackFillBinding(), 15, 15));
        setRight(right);

        setStyle("-fx-background-radius: 2; -fx-background-color: white; -fx-padding: 8;");
        JFXDepthManager.setDepth(this, 1);
        lblServerName.textProperty().bind(Bindings.createStringBinding(server::getName, server));
        lblServerUrl.setText(server.getUrl());
    }

    public AuthlibInjectorServer getServer() {
        return server;
    }
}
