package ru.spark.slauncher.ui.export;

import com.jfoenix.controls.JFXTreeView;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.mod.ModAdviser;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.construct.NoneMultipleSelectionModel;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardPage;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Pair;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author spark1337
 */
public final class ModpackFileSelectionPage extends StackPane implements WizardPage {
    private final WizardController controller;
    private final String version;
    private final ModAdviser adviser;
    @FXML
    private JFXTreeView<String> treeView;
    private final CheckBoxTreeItem<String> rootNode;

    public ModpackFileSelectionPage(WizardController controller, Profile profile, String version, ModAdviser adviser) {
        this.controller = controller;
        this.version = version;
        this.adviser = adviser;

        FXUtils.loadFXML(this, "/assets/fxml/modpack/selection.fxml");
        rootNode = getTreeItem(profile.getRepository().getRunDirectory(version), "minecraft");
        treeView.setRoot(rootNode);
        treeView.setSelectionModel(new NoneMultipleSelectionModel<>());
    }

    private CheckBoxTreeItem<String> getTreeItem(File file, String basePath) {
        if (!file.exists())
            return null;

        ModAdviser.ModSuggestion state = ModAdviser.ModSuggestion.SUGGESTED;
        if (basePath.length() > "minecraft/".length()) {
            state = adviser.advise(StringUtils.substringAfter(basePath, "minecraft/") + (file.isDirectory() ? "/" : ""), file.isDirectory());
            if (file.isFile() && Objects.equals(FileUtils.getNameWithoutExtension(file), version)) // Ignore <version>.json, <version>.jar
                state = ModAdviser.ModSuggestion.HIDDEN;
            if (file.isDirectory() && Objects.equals(file.getName(), version + "-natives")) // Ignore <version>-natives
                state = ModAdviser.ModSuggestion.HIDDEN;
            if (state == ModAdviser.ModSuggestion.HIDDEN)
                return null;
        }

        CheckBoxTreeItem<String> node = new CheckBoxTreeItem<>(StringUtils.substringAfterLast(basePath, "/"));
        if (state == ModAdviser.ModSuggestion.SUGGESTED)
            node.setSelected(true);

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null)
                for (File it : files) {
                    CheckBoxTreeItem<String> subNode = getTreeItem(it, basePath + "/" + it.getName());
                    if (subNode != null) {
                        node.setSelected(subNode.isSelected() || node.isSelected());
                        if (!subNode.isSelected())
                            node.setIndeterminate(true);
                        node.getChildren().add(subNode);
                    }
                }
            if (!node.isSelected()) node.setIndeterminate(false);

            // Empty folder need not to be displayed.
            if (node.getChildren().isEmpty())
                return null;
        }

        HBox graphic = new HBox();
        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().bindBidirectional(node.selectedProperty());
        checkBox.indeterminateProperty().bindBidirectional(node.indeterminateProperty());
        graphic.getChildren().add(checkBox);

        if (TRANSLATION.containsKey(basePath)) {
            Label comment = new Label();
            comment.setText(TRANSLATION.get(basePath));
            comment.setStyle("-fx-text-fill: gray;");
            comment.setMouseTransparent(true);
            graphic.getChildren().add(comment);
        }
        graphic.setPickOnBounds(false);
        node.setExpanded("minecraft".equals(basePath));
        node.setGraphic(graphic);

        return node;
    }

    private void getFilesNeeded(CheckBoxTreeItem<String> node, String basePath, List<String> list) {
        if (node == null) return;
        if (node.isSelected()) {
            if (basePath.length() > "minecraft/".length())
                list.add(StringUtils.substringAfter(basePath, "minecraft/"));
            for (TreeItem<String> child : node.getChildren()) {
                if (child instanceof CheckBoxTreeItem)
                    getFilesNeeded(((CheckBoxTreeItem<String>) child), basePath + "/" + child.getValue(), list);
            }
        }
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
        controller.getSettings().remove(MODPACK_FILE_SELECTION);
    }

    @FXML
    private void onNext() {
        LinkedList<String> list = new LinkedList<>();
        getFilesNeeded(rootNode, "minecraft", list);
        controller.getSettings().put(MODPACK_FILE_SELECTION, list);
        controller.onFinish();
    }

    @Override
    public String getTitle() {
        return I18n.i18n("modpack.wizard.step.2.title");
    }

    public static final String MODPACK_FILE_SELECTION = "modpack.accepted";
    private static final Map<String, String> TRANSLATION = Lang.mapOf(
            Pair.pair("minecraft/slauncherversion.cfg", I18n.i18n("modpack.files.slauncherversion_cfg")),
            Pair.pair("minecraft/servers.dat", I18n.i18n("modpack.files.servers_dat")),
            Pair.pair("minecraft/saves", I18n.i18n("modpack.files.saves")),
            Pair.pair("minecraft/mods", I18n.i18n("modpack.files.mods")),
            Pair.pair("minecraft/config", I18n.i18n("modpack.files.config")),
            Pair.pair("minecraft/liteconfig", I18n.i18n("modpack.files.liteconfig")),
            Pair.pair("minecraft/resourcepacks", I18n.i18n("modpack.files.resourcepacks")),
            Pair.pair("minecraft/resources", I18n.i18n("modpack.files.resourcepacks")),
            Pair.pair("minecraft/options.txt", I18n.i18n("modpack.files.options_txt")),
            Pair.pair("minecraft/optionsshaders.txt", I18n.i18n("modpack.files.optionsshaders_txt")),
            Pair.pair("minecraft/mods/VoxelMods", I18n.i18n("modpack.files.mods.voxelmods")),
            Pair.pair("minecraft/dumps", I18n.i18n("modpack.files.dumps")),
            Pair.pair("minecraft/blueprints", I18n.i18n("modpack.files.blueprints")),
            Pair.pair("minecraft/scripts", I18n.i18n("modpack.files.scripts"))
    );
}
