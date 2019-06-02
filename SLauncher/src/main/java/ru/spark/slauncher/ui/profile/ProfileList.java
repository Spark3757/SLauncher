package ru.spark.slauncher.ui.profile;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.ListPage;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.util.javafx.ExtendedProperties;
import ru.spark.slauncher.util.javafx.MappedObservableList;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class ProfileList extends ListPage<ProfileListItem> implements DecoratorPage {
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(i18n("profile.manage"));
    private final ListProperty<Profile> profiles = new SimpleListProperty<>(FXCollections.observableArrayList());
    private ObjectProperty<Profile> selectedProfile;

    public ProfileList() {
        setItems(MappedObservableList.create(profilesProperty(), ProfileListItem::new));
        selectedProfile = ExtendedProperties.createSelectedItemPropertyFor(getItems(), Profile.class);
    }

    public ObjectProperty<Profile> selectedProfileProperty() {
        return selectedProfile;
    }

    public ListProperty<Profile> profilesProperty() {
        return profiles;
    }

    @Override
    public void add() {
        Controllers.navigate(new ProfilePage(null));
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }
}
