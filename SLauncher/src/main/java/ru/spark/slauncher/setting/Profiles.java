package ru.spark.slauncher.setting;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.event.EventBus;
import ru.spark.slauncher.event.RefreshedVersionsEvent;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.util.i18n.I18n;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;

public final class Profiles {

    public static final String HOME_PROFILE = "Home";
    private static final ObservableList<Profile> profiles = observableArrayList(profile -> new Observable[]{profile});
    private static final ReadOnlyListWrapper<Profile> profilesWrapper = new ReadOnlyListWrapper<>(profiles);
    private static final ReadOnlyStringWrapper selectedVersion = new ReadOnlyStringWrapper();
    private static final List<Consumer<Profile>> versionsListeners = new LinkedList<>();
    /**
     * True if {@link #init()} hasn't been called.
     */
    private static boolean initialized = false;
    private static ObjectProperty<Profile> selectedProfile = new SimpleObjectProperty<Profile>() {
        {
            profiles.addListener(FXUtils.onInvalidating(this::invalidated));
        }

        @Override
        protected void invalidated() {
            if (!initialized)
                return;

            Profile profile = get();

            if (profiles.isEmpty()) {
                if (profile != null) {
                    set(null);
                    return;
                }
            } else {
                if (!profiles.contains(profile)) {
                    set(profiles.get(0));
                    return;
                }
            }

            ConfigHolder.config().setSelectedProfile(profile == null ? "" : profile.getName());
            if (profile != null) {
                if (profile.getRepository().isLoaded())
                    selectedVersion.bind(profile.selectedVersionProperty());
                else {
                    selectedVersion.unbind();
                    selectedVersion.set(null);
                    // bind when repository was reloaded.
                    profile.getRepository().refreshVersionsAsync().start();
                }
            } else {
                selectedVersion.unbind();
                selectedVersion.set(null);
            }
        }
    };

    static {
        profiles.addListener(FXUtils.onInvalidating(Profiles::updateProfileStorages));
        profiles.addListener(FXUtils.onInvalidating(Profiles::checkProfiles));

        selectedProfile.addListener((a, b, newValue) -> {
            if (newValue != null)
                newValue.getRepository().refreshVersionsAsync().start();
        });
    }

    private Profiles() {
    }

    public static String getProfileDisplayName(Profile profile) {
        switch (profile.getName()) {
            case Profiles.HOME_PROFILE:
                return I18n.i18n("profile.home");
            default:
                return profile.getName();
        }
    }

    private static void checkProfiles() {
        if (profiles.isEmpty()) {
            Profile home = new Profile(Profiles.HOME_PROFILE, Metadata.MINECRAFT_DIRECTORY.toFile());
            Platform.runLater(() -> profiles.add(home));
        }
    }

    private static void updateProfileStorages() {
        // don't update the underlying storage before data loading is completed
        // otherwise it might cause data loss
        if (!initialized)
            return;
        // update storage
        ConfigHolder.config().getConfigurations().clear();
        ConfigHolder.config().getConfigurations().putAll(profiles.stream().collect(Collectors.toMap(Profile::getName, it -> it)));
    }

    /**
     * Called when it's ready to load profiles from {@link ConfigHolder#config()}.
     */
    static void init() {
        if (initialized)
            throw new IllegalStateException("Already initialized");

        HashSet<String> names = new HashSet<>();
        ConfigHolder.config().getConfigurations().forEach((name, profile) -> {
            if (!names.add(name)) return;
            profiles.add(profile);
            profile.setName(name);
        });
        checkProfiles();

        // Platform.runLater is necessary or profiles will be empty
        // since checkProfiles adds 2 base profile later.
        Platform.runLater(() -> {
            initialized = true;

            selectedProfile.set(
                    profiles.stream()
                            .filter(it -> it.getName().equals(ConfigHolder.config().getSelectedProfile()))
                            .findFirst()
                            .orElse(profiles.get(0)));
        });

        EventBus.EVENT_BUS.channel(RefreshedVersionsEvent.class).registerWeak(event -> {
            FXUtils.runInFX(() -> {
                Profile profile = selectedProfile.get();
                if (profile != null && profile.getRepository() == event.getSource()) {
                    selectedVersion.bind(profile.selectedVersionProperty());
                    for (Consumer<Profile> listener : versionsListeners)
                        listener.accept(profile);
                }
            });
        });
    }

    public static ObservableList<Profile> getProfiles() {
        return profiles;
    }

    public static ReadOnlyListProperty<Profile> profilesProperty() {
        return profilesWrapper.getReadOnlyProperty();
    }

    public static Profile getSelectedProfile() {
        return selectedProfile.get();
    }

    public static void setSelectedProfile(Profile profile) {
        selectedProfile.set(profile);
    }

    public static ObjectProperty<Profile> selectedProfileProperty() {
        return selectedProfile;
    }

    public static ReadOnlyStringProperty selectedVersionProperty() {
        return selectedVersion.getReadOnlyProperty();
    }

    // Guaranteed that the repository is loaded.
    public static String getSelectedVersion() {
        return selectedVersion.get();
    }

    public static void registerVersionsListener(Consumer<Profile> listener) {
        Profile profile = getSelectedProfile();
        if (profile != null && profile.getRepository().isLoaded())
            listener.accept(profile);
        versionsListeners.add(listener);
    }
}
