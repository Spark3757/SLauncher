package ru.spark.slauncher.ui.versions;

import com.jfoenix.controls.JFXTabPane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.mod.ModInfo;
import ru.spark.slauncher.mod.ModManager;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.ListPageBase;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class ModListPage extends ListPageBase<ModListPageSkin.ModInfoObject> {
    private final BooleanProperty modded = new SimpleBooleanProperty(this, "modded", false);

    private JFXTabPane parentTab;
    private ModManager modManager;
    private LibraryAnalyzer libraryAnalyzer;

    public ModListPage() {

        FXUtils.applyDragListener(this, it -> Arrays.asList("jar", "zip", "litemod").contains(FileUtils.getExtension(it)), mods -> {
            mods.forEach(it -> {
                try {
                    modManager.addMod(it);
                } catch (IOException | IllegalArgumentException e) {
                    Logging.LOG.log(Level.WARNING, "Unable to parse mod file " + it, e);
                }
            });
            loadMods(modManager);
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ModListPageSkin(this);
    }

    public void refresh() {
        loadMods(modManager);
    }

    public void loadVersion(Profile profile, String id) {
        libraryAnalyzer = LibraryAnalyzer.analyze(profile.getRepository().getResolvedVersion(id));
        modded.set(libraryAnalyzer.hasForge() || libraryAnalyzer.hasLiteLoader());
        loadMods(profile.getRepository().getModManager(id));
    }

    private void loadMods(ModManager modManager) {
        this.modManager = modManager;
        Task.ofResult(() -> {
            synchronized (ModListPage.this) {
                FXUtils.runInFX(() -> loadingProperty().set(true));
                modManager.refreshMods();
                return new LinkedList<>(modManager.getMods());
            }
        }).whenComplete(Schedulers.javafx(), (list, isDependentSucceeded, exception) -> {
            loadingProperty().set(false);
            if (isDependentSucceeded)
                FXUtils.onWeakChangeAndOperate(parentTab.getSelectionModel().selectedItemProperty(), newValue -> {
                    if (newValue != null && newValue.getUserData() == ModListPage.this)
                        itemsProperty().setAll(list.stream().map(ModListPageSkin.ModInfoObject::new).collect(Collectors.toList()));
                });
        }).start();
    }

    public void add() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n("mods.choose_mod"));
        chooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter(i18n("extension.mod"), "*.jar", "*.zip", "*.litemod"));
        List<File> res = chooser.showOpenMultipleDialog(Controllers.getStage());

        // It's guaranteed that succeeded and failed are thread safe here.
        List<String> succeeded = new LinkedList<>();
        List<String> failed = new LinkedList<>();
        if (res == null) return;
        Task.of(() -> {
            for (File file : res) {
                try {
                    modManager.addMod(file);
                    succeeded.add(file.getName());
                } catch (Exception e) {
                    Logging.LOG.log(Level.WARNING, "Unable to add mod " + file, e);
                    failed.add(file.getName());

                    // Actually addMod will not throw exceptions because FileChooser has already filtered files.
                }
            }
        }).with(Task.of(Schedulers.javafx(), () -> {
            List<String> prompt = new LinkedList<>();
            if (!succeeded.isEmpty())
                prompt.add(i18n("mods.add.success", String.join(", ", succeeded)));
            if (!failed.isEmpty())
                prompt.add(i18n("mods.add.failed", String.join(", ", failed)));
            Controllers.dialog(String.join("\n", prompt), i18n("mods.add"));
            loadMods(modManager);
        })).start();
    }

    public void setParentTab(JFXTabPane parentTab) {
        this.parentTab = parentTab;
    }

    public void removeSelected(ObservableList<TreeItem<ModListPageSkin.ModInfoObject>> selectedItems) {
        try {
            modManager.removeMods(selectedItems.stream()
                    .map(TreeItem::getValue)
                    .map(ModListPageSkin.ModInfoObject::getModInfo)
                    .toArray(ModInfo[]::new));
            loadMods(modManager);
        } catch (IOException ignore) {
            // Fail to remove mods if the game is running or the mod is absent.
        }
    }

    public void enableSelected(ObservableList<TreeItem<ModListPageSkin.ModInfoObject>> selectedItems) {
        selectedItems.stream()
                .map(TreeItem::getValue)
                .map(ModListPageSkin.ModInfoObject::getModInfo)
                .forEach(info -> info.setActive(true));
    }

    public void disableSelected(ObservableList<TreeItem<ModListPageSkin.ModInfoObject>> selectedItems) {
        selectedItems.stream()
                .map(TreeItem::getValue)
                .map(ModListPageSkin.ModInfoObject::getModInfo)
                .forEach(info -> info.setActive(false));
    }

    public boolean isModded() {
        return modded.get();
    }

    public void setModded(boolean modded) {
        this.modded.set(modded);
    }

    public BooleanProperty moddedProperty() {
        return modded;
    }
}
