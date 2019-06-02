package ru.spark.slauncher.setting;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.event.EventBus;
import ru.spark.slauncher.event.EventPriority;
import ru.spark.slauncher.event.RefreshedVersionsEvent;
import ru.spark.slauncher.game.SLauncherCacheRepository;
import ru.spark.slauncher.game.SLauncherGameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.WeakListenerHolder;
import ru.spark.slauncher.util.ToStringBuilder;
import ru.spark.slauncher.util.javafx.ObservableHelper;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author Spark1337
 */
@JsonAdapter(Profile.Serializer.class)
public final class Profile implements Observable {
    private final WeakListenerHolder listenerHolder = new WeakListenerHolder();
    private final SLauncherGameRepository repository;

    private final StringProperty selectedVersion = new SimpleStringProperty();
    private final ObjectProperty<File> gameDir;
    private final ReadOnlyObjectWrapper<VersionSetting> global = new ReadOnlyObjectWrapper<>(this, "global");
    private final SimpleStringProperty name;
    private BooleanProperty useRelativePath = new SimpleBooleanProperty(this, "useRelativePath", false);
    private ObservableHelper observableHelper = new ObservableHelper(this);

    public Profile(String name) {
        this(name, new File(".minecraft"));
    }

    public Profile(String name, File initialGameDir) {
        this(name, initialGameDir, new VersionSetting());
    }

    public Profile(String name, File initialGameDir, VersionSetting global) {
        this(name, initialGameDir, global, null, false);
    }

    public Profile(String name, File initialGameDir, VersionSetting global, String selectedVersion, boolean useRelativePath) {
        this.name = new SimpleStringProperty(this, "name", name);
        gameDir = new SimpleObjectProperty<>(this, "gameDir", initialGameDir);
        repository = new SLauncherGameRepository(this, initialGameDir);
        this.global.set(global == null ? new VersionSetting() : global);
        this.selectedVersion.set(selectedVersion);
        this.useRelativePath.set(useRelativePath);

        gameDir.addListener((a, b, newValue) -> repository.changeDirectory(newValue));
        this.selectedVersion.addListener(o -> checkSelectedVersion());
        listenerHolder.add(EventBus.EVENT_BUS.channel(RefreshedVersionsEvent.class).registerWeak(event -> checkSelectedVersion(), EventPriority.HIGHEST));

        addPropertyChangedListener(FXUtils.onInvalidating(this::invalidate));
    }

    public StringProperty selectedVersionProperty() {
        return selectedVersion;
    }

    public String getSelectedVersion() {
        return selectedVersion.get();
    }

    public void setSelectedVersion(String selectedVersion) {
        this.selectedVersion.set(selectedVersion);
    }

    public ObjectProperty<File> gameDirProperty() {
        return gameDir;
    }

    public File getGameDir() {
        return gameDir.get();
    }

    public void setGameDir(File gameDir) {
        this.gameDir.set(gameDir);
    }

    public ReadOnlyObjectProperty<VersionSetting> globalProperty() {
        return global.getReadOnlyProperty();
    }

    public VersionSetting getGlobal() {
        return global.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public BooleanProperty useRelativePathProperty() {
        return useRelativePath;
    }

    public boolean isUseRelativePath() {
        return useRelativePath.get();
    }

    public void setUseRelativePath(boolean useRelativePath) {
        this.useRelativePath.set(useRelativePath);
    }

    private void checkSelectedVersion() {
        FXUtils.runInFX(() -> {
            if (!repository.isLoaded()) return;
            String newValue = selectedVersion.get();
            if (!repository.hasVersion(newValue)) {
                Optional<String> version = repository.getVersions().stream().findFirst().map(Version::getId);
                if (version.isPresent())
                    selectedVersion.setValue(version.get());
                else if (newValue != null)
                    selectedVersion.setValue(null);
            }
        });
    }

    public SLauncherGameRepository getRepository() {
        return repository;
    }

    public DefaultDependencyManager getDependency() {
        return new DefaultDependencyManager(repository, DownloadProviders.getDownloadProvider(), SLauncherCacheRepository.REPOSITORY);
    }

    public VersionSetting getVersionSetting(String id) {
        VersionSetting vs = repository.getVersionSetting(id);
        if (vs == null || vs.isUsesGlobal()) {
            getGlobal().setGlobal(true); // always keep global.isGlobal = true
            getGlobal().setUsesGlobal(true);
            return getGlobal();
        } else
            return vs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("gameDir", getGameDir())
                .append("name", getName())
                .append("useRelativePath", isUseRelativePath())
                .toString();
    }

    private void addPropertyChangedListener(InvalidationListener listener) {
        name.addListener(listener);
        global.addListener(listener);
        gameDir.addListener(listener);
        useRelativePath.addListener(listener);
        global.get().addPropertyChangedListener(listener);
        selectedVersion.addListener(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        observableHelper.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        observableHelper.removeListener(listener);
    }

    private void invalidate() {
        Platform.runLater(observableHelper::invalidate);
    }

    public static final class Serializer implements JsonSerializer<Profile>, JsonDeserializer<Profile> {
        @Override
        public JsonElement serialize(Profile src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null)
                return JsonNull.INSTANCE;

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("global", context.serialize(src.getGlobal()));
            jsonObject.addProperty("gameDir", src.getGameDir().getPath());
            jsonObject.addProperty("useRelativePath", src.isUseRelativePath());
            jsonObject.addProperty("selectedMinecraftVersion", src.getSelectedVersion());

            return jsonObject;
        }

        @Override
        public Profile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json == JsonNull.INSTANCE || !(json instanceof JsonObject)) return null;
            JsonObject obj = (JsonObject) json;
            String gameDir = Optional.ofNullable(obj.get("gameDir")).map(JsonElement::getAsString).orElse("");

            return new Profile("Default",
                    new File(gameDir),
                    context.deserialize(obj.get("global"), VersionSetting.class),
                    Optional.ofNullable(obj.get("selectedMinecraftVersion")).map(JsonElement::getAsString).orElse(""),
                    Optional.ofNullable(obj.get("useRelativePath")).map(JsonElement::getAsBoolean).orElse(false));
        }

    }
}
