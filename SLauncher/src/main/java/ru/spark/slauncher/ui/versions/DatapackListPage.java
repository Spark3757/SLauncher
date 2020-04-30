package ru.spark.slauncher.ui.versions;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.stage.FileChooser;
import ru.spark.slauncher.mod.Datapack;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.ListPageBase;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.javafx.MappedObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class DatapackListPage extends ListPageBase<DatapackListPageSkin.DatapackInfoObject> implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>();
    private final Path worldDir;
    private final Datapack datapack;

    private final ObservableList<DatapackListPageSkin.DatapackInfoObject> items;

    public DatapackListPage(String worldName, Path worldDir) {
        this.worldDir = worldDir;

        state.set(State.fromTitle(I18n.i18n("datapack.title", worldName)));

        datapack = new Datapack(worldDir.resolve("datapacks"));
        datapack.loadFromDir();

        setItems(items = MappedObservableList.create(datapack.getInfo(), DatapackListPageSkin.DatapackInfoObject::new));

        FXUtils.applyDragListener(this, it -> Objects.equals("zip", FileUtils.getExtension(it)),
                mods -> mods.forEach(this::installSingleDatapack), this::refresh);
    }

    private void installSingleDatapack(File datapack) {
        try {
            Datapack zip = new Datapack(datapack.toPath());
            zip.loadFromZip();
            zip.installTo(worldDir);
        } catch (IOException | IllegalArgumentException e) {
            Logging.LOG.log(Level.WARNING, "Unable to parse datapack file " + datapack, e);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DatapackListPageSkin(this);
    }

    public void refresh() {
        setLoading(true);
        Task.runAsync(datapack::loadFromDir)
                .withRunAsync(Schedulers.javafx(), () -> setLoading(false))
                .start();
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    public void add() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.i18n("datapack.choose_datapack"));
        chooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter(I18n.i18n("datapack.extension"), "*.zip"));
        List<File> res = chooser.showOpenMultipleDialog(Controllers.getStage());

        if (res != null)
            res.forEach(this::installSingleDatapack);

        datapack.loadFromDir();
    }

    void removeSelected(ObservableList<DatapackListPageSkin.DatapackInfoObject> selectedItems) {
        selectedItems.stream()
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

    void enableSelected(ObservableList<DatapackListPageSkin.DatapackInfoObject> selectedItems) {
        selectedItems.stream()
                .map(DatapackListPageSkin.DatapackInfoObject::getPackInfo)
                .forEach(info -> info.setActive(true));
    }

    void disableSelected(ObservableList<DatapackListPageSkin.DatapackInfoObject> selectedItems) {
        selectedItems.stream()
                .map(DatapackListPageSkin.DatapackInfoObject::getPackInfo)
                .forEach(info -> info.setActive(false));
    }
}
