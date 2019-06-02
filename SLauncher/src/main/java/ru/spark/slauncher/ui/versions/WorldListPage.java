package ru.spark.slauncher.ui.versions;

import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import ru.spark.slauncher.game.GameVersion;
import ru.spark.slauncher.game.World;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.*;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class WorldListPage extends ListPageBase<WorldListItem> {
    private final BooleanProperty showAll = new SimpleBooleanProperty(this, "showAll", false);

    private Path savesDir;
    private List<World> worlds;
    private Profile profile;
    private String id;
    private String gameVersion;

    public WorldListPage() {
        FXUtils.applyDragListener(this, it -> "zip".equals(FileUtils.getExtension(it)), modpacks -> {
            installWorld(modpacks.get(0));
        });

        showAll.addListener(e -> {
            if (worlds != null)
                itemsProperty().setAll(worlds.stream()
                        .filter(world -> isShowAll() || world.getGameVersion() == null || world.getGameVersion().equals(gameVersion))
                        .map(WorldListItem::new).collect(Collectors.toList()));
        });
    }

    @Override
    protected ToolbarListPageSkin createDefaultSkin() {
        return new WorldListPageSkin();
    }

    public void loadVersion(Profile profile, String id) {
        this.profile = profile;
        this.id = id;
        this.savesDir = profile.getRepository().getRunDirectory(id).toPath().resolve("saves");
        refresh();
    }

    public void refresh() {
        if (profile == null || id == null)
            return;

        setLoading(true);
        Task
                .of(() -> gameVersion = GameVersion.minecraftVersion(profile.getRepository().getVersionJar(id)).orElse(null))
                .thenSupply(() -> World.getWorlds(savesDir).parallel().collect(Collectors.toList()))
                .whenComplete(Schedulers.javafx(), (result, isDependentSucceeded, exception) -> {
                    worlds = result;
                    setLoading(false);
                    if (isDependentSucceeded)
                        itemsProperty().setAll(result.stream()
                                .filter(world -> isShowAll() || world.getGameVersion() == null || world.getGameVersion().equals(gameVersion))
                                .map(WorldListItem::new).collect(Collectors.toList()));
                }).start();
    }

    public void add() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n("world.import.choose"));
        chooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter(i18n("world.extension"), "*.zip"));
        List<File> res = chooser.showOpenMultipleDialog(Controllers.getStage());

        if (res == null || res.isEmpty()) return;
        installWorld(res.get(0));
    }

    private void installWorld(File zipFile) {
        // Only accept one world file because user is required to confirm the new world name
        // Or too many input dialogs are popped.
        Task.ofResult(() -> new World(zipFile.toPath()))
                .whenComplete(Schedulers.javafx(), world -> {
                    Controllers.inputDialog(i18n("world.name.enter"), (name, resolve, reject) -> {
                        Task.of(() -> world.install(savesDir, name))
                                .whenComplete(Schedulers.javafx(), () -> {
                                    itemsProperty().add(new WorldListItem(new World(savesDir.resolve(name))));
                                    resolve.run();
                                }, e -> {
                                    if (e instanceof FileAlreadyExistsException)
                                        reject.accept(i18n("world.import.failed", i18n("world.import.already_exists")));
                                    else
                                        reject.accept(i18n("world.import.failed", e.getClass().getName() + ": " + e.getLocalizedMessage()));
                                }).start();
                    }).setInitialText(world.getWorldName());
                }, e -> {
                    Logging.LOG.log(Level.WARNING, "Unable to parse world file " + zipFile, e);
                    Controllers.dialog(i18n("world.import.invalid"));
                }).start();
    }

    public boolean isShowAll() {
        return showAll.get();
    }

    public void setShowAll(boolean showAll) {
        this.showAll.set(showAll);
    }

    public BooleanProperty showAllProperty() {
        return showAll;
    }

    private class WorldListPageSkin extends ToolbarListPageSkin<WorldListPage> {

        WorldListPageSkin() {
            super(WorldListPage.this);
        }

        @Override
        protected List<Node> initializeToolbar(WorldListPage skinnable) {
            JFXCheckBox chkShowAll = new JFXCheckBox();
            chkShowAll.getStyleClass().add("jfx-tool-bar-checkbox");
            chkShowAll.textFillProperty().bind(Theme.foregroundFillBinding());
            chkShowAll.setText(i18n("world.show_all"));
            chkShowAll.selectedProperty().bindBidirectional(skinnable.showAllProperty());

            return Arrays.asList(chkShowAll,
                    createToolbarButton(i18n("button.refresh"), SVG::refresh, skinnable::refresh),
                    createToolbarButton(i18n("world.add"), SVG::plus, skinnable::add));
        }
    }
}
