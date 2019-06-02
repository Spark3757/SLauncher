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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ru.spark.slauncher.mod.Datapack;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.ToolbarListPageSkin;
import ru.spark.slauncher.ui.construct.JFXCheckBoxTreeTableCell;
import ru.spark.slauncher.ui.construct.SpinnerPane;
import ru.spark.slauncher.ui.construct.TwoLineListItem;
import ru.spark.slauncher.util.StringUtils;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

class DatapackListPageSkin extends SkinBase<DatapackListPage> {

    DatapackListPageSkin(DatapackListPage skinnable) {
        super(skinnable);

        BorderPane root = new BorderPane();
        JFXTreeTableView<DatapackInfoObject> tableView = new JFXTreeTableView<>();

        {
            HBox toolbar = new HBox();
            toolbar.getStyleClass().add("jfx-tool-bar-second");
            JFXDepthManager.setDepth(toolbar, 1);
            toolbar.setPickOnBounds(false);

            toolbar.getChildren().add(ToolbarListPageSkin.createToolbarButton(i18n("button.refresh"), SVG::refresh, skinnable::refresh));
            toolbar.getChildren().add(ToolbarListPageSkin.createToolbarButton(i18n("datapack.add"), SVG::plus, skinnable::add));
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

            tableView.getStyleClass().addAll("no-header");
            tableView.setShowRoot(false);
            tableView.setEditable(true);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.setRoot(new RecursiveTreeItem<>(skinnable.getItems(), RecursiveTreeObject::getChildren));

            JFXTreeTableColumn<DatapackInfoObject, Boolean> activeColumn = new JFXTreeTableColumn<>();
            FXUtils.setupCellValueFactory(activeColumn, DatapackInfoObject::activeProperty);
            activeColumn.setCellFactory(c -> new JFXCheckBoxTreeTableCell<>());
            activeColumn.setEditable(true);
            activeColumn.setMaxWidth(40);
            activeColumn.setMinWidth(40);

            JFXTreeTableColumn<DatapackInfoObject, Node> detailColumn = new JFXTreeTableColumn<>();
            FXUtils.setupCellValueFactory(detailColumn, DatapackInfoObject::nodeProperty);

            tableView.getColumns().setAll(activeColumn, detailColumn);

            tableView.setColumnResizePolicy(JFXTreeTableView.CONSTRAINED_RESIZE_POLICY);
            center.setContent(tableView);
            root.setCenter(center);
        }

        getChildren().setAll(root);
    }

    static class DatapackInfoObject extends RecursiveTreeObject<DatapackInfoObject> {
        private final BooleanProperty active;
        private final Datapack.Pack packInfo;
        private final ObjectProperty<Node> node;

        DatapackInfoObject(Datapack.Pack packInfo) {
            this.packInfo = packInfo;
            this.active = packInfo.activeProperty();
            this.node = new SimpleObjectProperty<>(FXUtils.wrapMargin(new TwoLineListItem(packInfo.getId(), StringUtils.parseColorEscapes(packInfo.getDescription())),
                    new Insets(8, 0, 8, 0)));
        }

        BooleanProperty activeProperty() {
            return active;
        }

        ObjectProperty<Node> nodeProperty() {
            return node;
        }

        Datapack.Pack getPackInfo() {
            return packInfo;
        }
    }
}
