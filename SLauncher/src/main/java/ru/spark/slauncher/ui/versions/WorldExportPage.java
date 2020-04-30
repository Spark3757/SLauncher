package ru.spark.slauncher.ui.versions;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import ru.spark.slauncher.game.World;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.wizard.WizardSinglePage;
import ru.spark.slauncher.util.i18n.I18n;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WorldExportPage extends WizardSinglePage {
    private final StringProperty path = new SimpleStringProperty();
    private final StringProperty gameVersion = new SimpleStringProperty();
    private final StringProperty worldName = new SimpleStringProperty();
    private final World world;

    public WorldExportPage(World world, Path export, Runnable onFinish) {
        super(onFinish);

        this.world = world;

        path.set(export.toString());
        gameVersion.set(world.getGameVersion());
        worldName.set(world.getWorldName());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WorldExportPageSkin(this);
    }


    public StringProperty pathProperty() {
        return path;
    }

    public StringProperty gameVersionProperty() {
        return gameVersion;
    }

    public StringProperty worldNameProperty() {
        return worldName;
    }

    public void export() {
        onFinish.run();
    }

    @Override
    public String getTitle() {
        return I18n.i18n("world.export.wizard", world.getFileName());
    }

    @Override
    protected Object finish() {
        return Task.runAsync(I18n.i18n("world.export.wizard", worldName.get()), () -> world.export(Paths.get(path.get()), worldName.get()));
    }
}
