package ru.spark.slauncher.ui.versions;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import ru.spark.slauncher.game.World;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.wizard.SinglePageWizardProvider;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorldListItem extends Control {
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty subtitle = new SimpleStringProperty();
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();
    private final World world;
    private final SimpleDateFormat simpleDateFormat;

    public WorldListItem(World world) {
        this.world = world;
        this.simpleDateFormat = new SimpleDateFormat(I18n.i18n("world.time"));

        title.set(StringUtils.parseColorEscapes(world.getWorldName()));
        subtitle.set(I18n.i18n("world.description", world.getFileName(), simpleDateFormat.format(new Date(world.getLastPlayed())), world.getGameVersion() == null ? I18n.i18n("message.unknown") : world.getGameVersion()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WorldListItemSkin(this);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public void export() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.i18n("world.export.title"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.i18n("world"), "*.zip"));
        fileChooser.setInitialFileName(world.getWorldName());
        File file = fileChooser.showSaveDialog(Controllers.getStage());
        if (file == null) {
            return;
        }

        Controllers.getDecorator().startWizard(new SinglePageWizardProvider(controller -> new WorldExportPage(world, file.toPath(), controller::onFinish)));
    }

    public void reveal() {
        try {
            FXUtils.openFolder(world.getFile().toFile());
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }
    }

    public void manageDatapacks() {
        if (world.getGameVersion() == null || // old game will not write game version to level.dat
                (VersionNumber.isIntVersionNumber(world.getGameVersion()) // we don't parse snapshot version
                        && VersionNumber.asVersion(world.getGameVersion()).compareTo(VersionNumber.asVersion("1.13")) < 0)) {
            Controllers.dialog(I18n.i18n("world.datapack.1_13"));
            return;
        }
        Controllers.navigate(new DatapackListPage(world.getWorldName(), world.getFile()));
    }
}
