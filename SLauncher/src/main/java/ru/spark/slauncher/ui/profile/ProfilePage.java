package ru.spark.slauncher.ui.profile;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.construct.FileItem;
import ru.spark.slauncher.ui.construct.PageCloseEvent;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;

import java.io.File;
import java.util.Optional;

public final class ProfilePage extends StackPane implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>();
    private final StringProperty location;
    private final Profile profile;

    @FXML
    private JFXTextField txtProfileName;
    @FXML
    private FileItem gameDir;
    @FXML
    private JFXButton btnSave;
    @FXML
    private JFXCheckBox toggleUseRelativePath;

    /**
     * @param profile null if creating a new profile.
     */
    public ProfilePage(Profile profile) {
        this.profile = profile;
        String profileDisplayName = Optional.ofNullable(profile).map(Profiles::getProfileDisplayName).orElse("");

        state.set(State.fromTitle(profile == null ? I18n.i18n("profile.new") : I18n.i18n("profile") + " - " + profileDisplayName));
        location = new SimpleStringProperty(this, "location",
                Optional.ofNullable(profile).map(Profile::getGameDir).map(File::getAbsolutePath).orElse(".minecraft"));

        FXUtils.loadFXML(this, "/assets/fxml/profile.fxml");

        txtProfileName.setText(profileDisplayName);
        txtProfileName.getValidators().add(new ValidatorBase() {
            {
                setMessage(I18n.i18n("profile.already_exists"));
            }

            @Override
            protected void eval() {
                JFXTextField control = (JFXTextField) this.getSrcControl();
                if (Profiles.getProfiles().stream().anyMatch(profile -> profile.getName().equals(control.getText())))
                    hasErrors.set(true);
                else
                    hasErrors.set(false);
            }
        });
        FXUtils.onChangeAndOperate(txtProfileName.textProperty(), it -> {
            btnSave.setDisable(!txtProfileName.validate() || StringUtils.isBlank(getLocation()));
        });
        gameDir.pathProperty().bindBidirectional(location);
        FXUtils.onChangeAndOperate(location, it -> {
            btnSave.setDisable(!txtProfileName.validate() || StringUtils.isBlank(getLocation()));
        });
        gameDir.convertToRelativePathProperty().bind(toggleUseRelativePath.selectedProperty());
        if (profile != null) {
            toggleUseRelativePath.setSelected(profile.isUseRelativePath());
        }
    }

    @FXML
    private void onSave() {
        if (profile != null) {
            profile.setName(txtProfileName.getText());
            profile.setUseRelativePath(toggleUseRelativePath.isSelected());
            if (StringUtils.isNotBlank(getLocation())) {
                profile.setGameDir(new File(getLocation()));
            }
        } else {
            if (StringUtils.isBlank(getLocation())) {
                gameDir.onExplore();
            }
            Profile newProfile = new Profile(txtProfileName.getText(), new File(getLocation()));
            newProfile.setUseRelativePath(toggleUseRelativePath.isSelected());
            Profiles.getProfiles().add(newProfile);
        }

        fireEvent(new PageCloseEvent());
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    public String getLocation() {
        return location.get();
    }

    public StringProperty locationProperty() {
        return location;
    }

    public void setLocation(String location) {
        this.location.set(location);
    }
}
