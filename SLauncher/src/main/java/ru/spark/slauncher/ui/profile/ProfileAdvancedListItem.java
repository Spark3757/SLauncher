package ru.spark.slauncher.ui.profile;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.construct.AdvancedListItem;

public class ProfileAdvancedListItem extends AdvancedListItem {
    private ObjectProperty<Profile> profile = new SimpleObjectProperty<Profile>() {

        @Override
        protected void invalidated() {
            Profile profile = get();
            if (profile == null) {
            } else {
                setTitle(Profiles.getProfileDisplayName(profile));
                setSubtitle(profile.getGameDir().toString());
            }
        }
    };

    public ProfileAdvancedListItem() {
        setImage(new Image("/assets/img/craft_table.png"));
        setRightGraphic(SVG.viewList(Theme.blackFillBinding(), -1, -1));
    }

    public ObjectProperty<Profile> profileProperty() {
        return profile;
    }
}
