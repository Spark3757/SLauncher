package ru.spark.slauncher.ui.versions;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.effects.JFXDepthManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.mod.ModInfo;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.ToolbarListPageSkin;
import ru.spark.slauncher.ui.construct.JFXCheckBoxTreeTableCell;
import ru.spark.slauncher.ui.construct.SpinnerPane;
import ru.spark.slauncher.ui.construct.TwoLineListItem;
import ru.spark.slauncher.util.StringUtils;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

class ModListPageSkin extends SkinBase<ModListPage> {

    ModListPageSkin(ModListPage skinnable) {
        super(skinnable);

        StackPane pane = new StackPane();
        pane.getStyleClass().addAll("notice-pane", "white-background");

        BorderPane root = new BorderPane();
        JFXTreeTableView<ModInfoObject> tableView = new JFXTreeTableView<>();

        {
            HBox toolbar = new HBox();
            toolbar.getStyleClass().add("jfx-tool-bar-second");
            JFXDepthManager.setDepth(toolbar, 1);
            toolbar.setPickOnBounds(false);

            toolbar.getChildren().add(ToolbarListPageSkin.createToolbarButton(i18n("button.refresh"), SVG::refresh, skinnable::refresh));
            toolbar.getChildren().add(ToolbarListPageSkin.createToolbarButton(i18n("mods.add"), SVG::plus, skinnable::add));
            toolbar.getChildren().add(ToolbarListPageSkin.createToolbarButton(i18n("mods.remove"), SVG::delete, () ->
                    skinnable.removeSelected(tableView.getSelectionModel().getSelectedItems())));
            toolbar.getChildren().add(ToolbarListPageSkin.createToolbarButton(i18n("mods.enable"), SVG::check, () ->
                    skinnable.enableSelected(tableView.getSelectionModel().getSelectedItems())));
            toolbar.getChildren().add(ToolbarListPageSkin.createToolbarButton(i18n("mods.disable"), SVG::close, () ->
                    skinnable.disableSelected(tableView.getSelectionModel().getSelectedItems())));
            root.setTop(toolbar);
        }

        {
            SpinnerPane center = new SpinnerPane();
            center.getStyleClass().add("large-spinner-pane");
            center.loadingProperty().bind(skinnable.loadingProperty());

            tableView.getStyleClass().add("no-header");
            tableView.setShowRoot(false);
            tableView.setEditable(true);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.setRoot(new RecursiveTreeItem<>(skinnable.getItems(), RecursiveTreeObject::getChildren));

            JFXTreeTableColumn<ModInfoObject, Boolean> activeColumn = new JFXTreeTableColumn<>();
            FXUtils.setupCellValueFactory(activeColumn, ModInfoObject::activeProperty);
            activeColumn.setCellFactory(c -> new JFXCheckBoxTreeTableCell<>());
            activeColumn.setEditable(true);
            activeColumn.setMaxWidth(40);
            activeColumn.setMinWidth(40);

            JFXTreeTableColumn<ModInfoObject, Node> detailColumn = new JFXTreeTableColumn<>();
            FXUtils.setupCellValueFactory(detailColumn, ModInfoObject::nodeProperty);

            tableView.getColumns().setAll(activeColumn, detailColumn);

            tableView.setColumnResizePolicy(JFXTreeTableView.CONSTRAINED_RESIZE_POLICY);
            center.setContent(tableView);
            root.setCenter(center);
        }

        Label label = new Label(i18n("mods.not_modded"));
        label.prefWidthProperty().bind(pane.widthProperty().add(-100));

        FXUtils.onChangeAndOperate(skinnable.moddedProperty(), modded -> {
            if (modded) pane.getChildren().setAll(root);
            else pane.getChildren().setAll(label);
        });

        getChildren().setAll(pane);
    }

    static class ModInfoObject extends RecursiveTreeObject<ModInfoObject> {
        private final BooleanProperty active;
        private final ModInfo modInfo;
        private final ObjectProperty<Node> node;

        ModInfoObject(ModInfo modInfo) {
            this.modInfo = modInfo;
            this.active = modInfo.activeProperty();
            StringBuilder message = new StringBuilder(modInfo.getName());
            if (StringUtils.isNotBlank(modInfo.getVersion()))
                message.append(", ").append(i18n("archive.version")).append(": ").append(modInfo.getVersion());
            if (StringUtils.isNotBlank(modInfo.getGameVersion()))
                message.append(", ").append(i18n("archive.game_version")).append(": ").append(modInfo.getGameVersion());
            if (StringUtils.isNotBlank(modInfo.getAuthors()))
                message.append(", ").append(i18n("archive.author")).append(": ").append(modInfo.getAuthors());
            this.node = new SimpleObjectProperty<>(FXUtils.wrapMargin(new TwoLineListItem(modInfo.getFileName(), message.toString()), new Insets(8, 0, 8, 0)));
        }

        BooleanProperty activeProperty() {
            return active;
        }

        ObjectProperty<Node> nodeProperty() {
            return node;
        }

        ModInfo getModInfo() {
            return modInfo;
        }
    }
}
