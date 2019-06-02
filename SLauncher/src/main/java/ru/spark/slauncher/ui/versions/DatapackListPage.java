package ru.spark.slauncher.ui.versions;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import ru.spark.slauncher.mod.Datapack;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.ListPageBase;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.javafx.MappedObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class DatapackListPage extends ListPageBase<DatapackListPageSkin.DatapackInfoObject> implements DecoratorPage {
    private final StringProperty title = new SimpleStringProperty();
    private final Path worldDir;
    private final Datapack datapack;

    private final ObservableList<DatapackListPageSkin.DatapackInfoObject> items;

    public DatapackListPage(String worldName, Path worldDir) {
        this.worldDir = worldDir;

        title.set(i18n("datapack.title", worldName));

        datapack = new Datapack(worldDir.resolve("datapacks"));
        datapack.loadFromDir();

        setItems(items = MappedObservableList.create(datapack.getInfo(), DatapackListPageSkin.DatapackInfoObject::new));

        FXUtils.applyDragListener(this, it -> Objects.equals("zip", FileUtils.getExtension(it)), mods -> {
            mods.forEach(it -> {
                try {
                    Datapack zip = new Datapack(it.toPath());
                    zip.loadFromZip();
                    zip.installTo(worldDir);
                } catch (IOException | IllegalArgumentException e) {
                    Logging.LOG.log(Level.WARNING, "Unable to parse datapack file " + it, e);
                }
            });
        }, this::refresh);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DatapackListPageSkin(this);
    }

    public void refresh() {
        setLoading(true);
        Task.of(datapack::loadFromDir)
                .with(Task.of(() -> setLoading(false)))
                .start();
    }

    @Override
    public StringProperty titleProperty() {
        return title;
    }

    public void add() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n("datapack.choose_datapack"));
        chooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter(i18n("datapack.extension"), "*.zip"));
        List<File> res = chooser.showOpenMultipleDialog(Controllers.getStage());

        if (res != null)
            res.forEach(it -> {
                try {
                    Datapack zip = new Datapack(it.toPath());
                    zip.loadFromZip();
                    zip.installTo(worldDir);
                } catch (IOException | IllegalArgumentException e) {
                    Logging.LOG.log(Level.WARNING, "Unable to parse datapack file " + it, e);
                }
            });

        datapack.loadFromDir();
    }

    void removeSelected(ObservableList<TreeItem<DatapackListPageSkin.DatapackInfoObject>> selectedItems) {
        selectedItems.stream()
                .map(TreeItem::getValue)
                .map(DatapackListPageSkin.DatapackInfoObject::getPackInfo)
                .forEach(pack -> {
                    try {
                        datapack.deletePack(pack);
                    } catch (IOException e) {
                        // Fail to remove mods if the game is running or the datapack is absent.
                        Logging.LOG.warning("Failed to delete datapack " + pack);
                    }
                });
    }

    void enableSelected(ObservableList<TreeItem<DatapackListPageSkin.DatapackInfoObject>> selectedItems) {
        selectedItems.stream()
                .map(TreeItem::getValue)
                .map(DatapackListPageSkin.DatapackInfoObject::getPackInfo)
                .forEach(info -> info.setActive(true));
    }

    void disableSelected(ObservableList<TreeItem<DatapackListPageSkin.DatapackInfoObject>> selectedItems) {
        selectedItems.stream()
                .map(TreeItem::getValue)
                .map(DatapackListPageSkin.DatapackInfoObject::getPackInfo)
                .forEach(info -> info.setActive(false));
    }
}
