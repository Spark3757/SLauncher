package ru.spark.slauncher.ui.versions;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.effects.JFXDepthManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import ru.spark.slauncher.mod.ModInfo;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.construct.FloatListCell;
import ru.spark.slauncher.ui.construct.SpinnerPane;
import ru.spark.slauncher.ui.construct.TwoLineListItem;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;

import static ru.spark.slauncher.ui.ToolbarListPageSkin.createToolbarButton;

class ModListPageSkin extends SkinBase<ModListPage> {

    ModListPageSkin(ModListPage skinnable) {
        super(skinnable);

        StackPane pane = new StackPane();
        pane.getStyleClass().addAll("notice-pane");

        BorderPane root = new BorderPane();
        JFXListView<ModInfoObject> listView = new JFXListView<>();

        {
            HBox toolbar = new HBox();
            toolbar.getStyleClass().add("jfx-tool-bar-second");
            JFXDepthManager.setDepth(toolbar, 1);
            toolbar.setPickOnBounds(false);

            toolbar.getChildren().add(createToolbarButton(I18n.i18n("button.refresh"), SVG::refresh, skinnable::refresh));
            toolbar.getChildren().add(createToolbarButton(I18n.i18n("mods.add"), SVG::plus, skinnable::add));
            toolbar.getChildren().add(createToolbarButton(I18n.i18n("button.remove"), SVG::delete, () -> {
                Controllers.confirm(I18n.i18n("button.remove.confirm"), I18n.i18n("button.remove"), () -> {
                    skinnable.removeSelected(listView.getSelectionModel().getSelectedItems());
                }, null);
            }));
            toolbar.getChildren().add(createToolbarButton(I18n.i18n("mods.enable"), SVG::check, () ->
                    skinnable.enableSelected(listView.getSelectionModel().getSelectedItems())));
            toolbar.getChildren().add(createToolbarButton(I18n.i18n("mods.disable"), SVG::close, () ->
                    skinnable.disableSelected(listView.getSelectionModel().getSelectedItems())));
            root.setTop(toolbar);
        }

        {
            SpinnerPane center = new SpinnerPane();
            center.getStyleClass().add("large-spinner-pane");
            center.loadingProperty().bind(skinnable.loadingProperty());

            listView.setCellFactory(x -> new FloatListCell<ModInfoObject>() {
                JFXCheckBox checkBox = new JFXCheckBox();
                TwoLineListItem content = new TwoLineListItem();
                BooleanProperty booleanProperty;

                {

                    Region clippedContainer = (Region) listView.lookup(".clipped-container");
                    setPrefWidth(0);
                    HBox container = new HBox(8);
                    container.setAlignment(Pos.CENTER_LEFT);
                    pane.getChildren().add(container);
                    if (clippedContainer != null) {
                        maxWidthProperty().bind(clippedContainer.widthProperty());
                        prefWidthProperty().bind(clippedContainer.widthProperty());
                        minWidthProperty().bind(clippedContainer.widthProperty());
                    }

                    container.getChildren().setAll(checkBox, content);
                }

                @Override
                protected void updateControl(ModInfoObject dataItem, boolean empty) {
                    if (empty) return;
                    content.setTitle(dataItem.getTitle());
                    content.setSubtitle(dataItem.getSubtitle());
                    if (booleanProperty != null) {
                        checkBox.selectedProperty().unbindBidirectional(booleanProperty);
                    }
                    checkBox.selectedProperty().bindBidirectional(booleanProperty = dataItem.active);
                }
            });
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            Bindings.bindContent(listView.getItems(), skinnable.getItems());

            center.setContent(listView);
            root.setCenter(center);
        }

        Label label = new Label(I18n.i18n("mods.not_modded"));
        label.prefWidthProperty().bind(pane.widthProperty().add(-100));

        FXUtils.onChangeAndOperate(skinnable.moddedProperty(), modded -> {
            if (modded) pane.getChildren().setAll(root);
            else pane.getChildren().setAll(label);
        });

        getChildren().setAll(pane);
    }

    static class ModInfoObject extends RecursiveTreeObject<ModInfoObject> implements Comparable<ModInfoObject> {
        private final BooleanProperty active;
        private final ModInfo modInfo;
        private final String message;

        ModInfoObject(ModInfo modInfo) {
            this.modInfo = modInfo;
            this.active = modInfo.activeProperty();
            StringBuilder message = new StringBuilder(modInfo.getName());
            if (StringUtils.isNotBlank(modInfo.getVersion()))
                message.append(", ").append(I18n.i18n("archive.version")).append(": ").append(modInfo.getVersion());
            if (StringUtils.isNotBlank(modInfo.getGameVersion()))
                message.append(", ").append(I18n.i18n("archive.game_version")).append(": ").append(modInfo.getGameVersion());
            if (StringUtils.isNotBlank(modInfo.getAuthors()))
                message.append(", ").append(I18n.i18n("archive.author")).append(": ").append(modInfo.getAuthors());
            this.message = message.toString();
        }

        String getTitle() {
            return modInfo.getFileName();
        }

        String getSubtitle() {
            return message;
        }

        ModInfo getModInfo() {
            return modInfo;
        }

        @Override
        public int compareTo(@NotNull ModListPageSkin.ModInfoObject o) {
            return modInfo.getFileName().toLowerCase().compareTo(o.modInfo.getFileName().toLowerCase());
        }
    }
}
