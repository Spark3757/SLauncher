package ru.spark.slauncher.ui.versions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleGroup;
import ru.spark.slauncher.setting.Profile;

public class GameListItem extends Control {
    private final Profile profile;
    private final String version;
    private final boolean isModpack;
    private final ToggleGroup toggleGroup;
    private final BooleanProperty selected = new SimpleBooleanProperty();

    public GameListItem(ToggleGroup toggleGroup, Profile profile, String id) {
        this.profile = profile;
        this.version = id;
        this.toggleGroup = toggleGroup;
        this.isModpack = profile.getRepository().isModpack(id);

        selected.set(id.equals(profile.getSelectedVersion()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new GameListItemSkin(this);
    }

    public ToggleGroup getToggleGroup() {
        return toggleGroup;
    }

    public Profile getProfile() {
        return profile;
    }

    public String getVersion() {
        return version;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void checkSelection() {
        selected.set(version.equals(profile.getSelectedVersion()));
    }

    public void rename() {
        Versions.renameVersion(profile, version);
    }

    public void duplicate() {
        Versions.duplicateVersion(profile, version);
    }

    public void remove() {
        Versions.deleteVersion(profile, version);
    }

    public void export() {
        Versions.exportVersion(profile, version);
    }

    public void browse() {
        Versions.openFolder(profile, version);
    }

    public void launch() {
        Versions.testGame(profile, version);
    }

    public void modifyGameSettings() {
        Versions.modifyGameSettings(profile, version);
    }

    public void generateLaunchScript() {
        Versions.generateLaunchScript(profile, version);
    }

    public boolean canUpdate() {
        return isModpack;
    }

    public void update() {
        Versions.updateVersion(profile, version);
    }
}
