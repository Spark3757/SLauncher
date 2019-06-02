package ru.spark.slauncher.ui.profile;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Skin;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.Profiles;

public class ProfileListItem extends RadioButton {
    private final Profile profile;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty subtitle = new SimpleStringProperty();

    public ProfileListItem(Profile profile) {
        this.profile = profile;
        getStyleClass().clear();
        setUserData(profile);

        title.set(Profiles.getProfileDisplayName(profile));
        subtitle.set(profile.getGameDir().toString());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ProfileListItemSkin(this);
    }

    public void remove() {
        Profiles.getProfiles().remove(profile);
    }

    public Profile getProfile() {
        return profile;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getSubtitle() {
        return subtitle.get();
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.set(subtitle);
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }
}
