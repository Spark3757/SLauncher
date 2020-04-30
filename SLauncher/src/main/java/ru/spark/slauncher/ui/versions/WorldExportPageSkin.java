package ru.spark.slauncher.ui.versions;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.construct.ComponentList;
import ru.spark.slauncher.ui.construct.FileItem;
import ru.spark.slauncher.util.i18n.I18n;

import java.nio.file.Files;
import java.nio.file.Paths;

public class WorldExportPageSkin extends SkinBase<WorldExportPage> {

    public WorldExportPageSkin(WorldExportPage skinnable) {
        super(skinnable);

        Insets insets = new Insets(0, 0, 12, 0);
        VBox container = new VBox();
        container.setSpacing(16);
        container.setAlignment(Pos.CENTER);
        FXUtils.setLimitWidth(container, 500);
        {
            HBox labelContainer = new HBox();
            labelContainer.setPadding(new Insets(0, 0, 0, 5));
            Label label = new Label(I18n.i18n("world.export"));
            labelContainer.getChildren().setAll(label);
            container.getChildren().add(labelContainer);
        }

        ComponentList list = new ComponentList();

        FileItem fileItem = new FileItem();
        fileItem.setName(I18n.i18n("world.export.location"));
        fileItem.pathProperty().bindBidirectional(skinnable.pathProperty());
        list.getContent().add(fileItem);

        JFXTextField txtWorldName = new JFXTextField();
        txtWorldName.textProperty().bindBidirectional(skinnable.worldNameProperty());
        txtWorldName.setLabelFloat(true);
        txtWorldName.setPromptText(I18n.i18n("world.name"));
        StackPane.setMargin(txtWorldName, insets);
        list.getContent().add(txtWorldName);

        Label lblGameVersionTitle = new Label(I18n.i18n("world.game_version"));
        Label lblGameVersion = new Label();
        lblGameVersion.textProperty().bind(skinnable.gameVersionProperty());
        BorderPane gameVersionPane = new BorderPane();
        gameVersionPane.setPadding(new Insets(4, 0, 4, 0));
        gameVersionPane.setLeft(lblGameVersionTitle);
        gameVersionPane.setRight(lblGameVersion);
        list.getContent().add(gameVersionPane);

        container.getChildren().add(list);

        JFXButton btnExport = new JFXButton(I18n.i18n("button.export"));
        btnExport.disableProperty().bind(Bindings.createBooleanBinding(() -> txtWorldName.getText().isEmpty() || Files.exists(Paths.get(fileItem.getPath())),
                txtWorldName.textProperty().isEmpty(), fileItem.pathProperty()));
        btnExport.setButtonType(JFXButton.ButtonType.RAISED);
        btnExport.getStyleClass().add("jfx-button-raised");
        btnExport.setOnMouseClicked(e -> skinnable.export());
        HBox bottom = new HBox();
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.getChildren().setAll(btnExport);
        container.getChildren().add(bottom);

        getChildren().setAll(container);
    }


}
