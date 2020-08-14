package ru.spark.slauncher.ui.versions;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.game.GameVersion;
import ru.spark.slauncher.mod.ModpackConfiguration;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static ru.spark.slauncher.download.LibraryAnalyzer.LibraryType.MINECRAFT;
import static ru.spark.slauncher.util.Lang.handleUncaught;
import static ru.spark.slauncher.util.Lang.threadPool;
import static ru.spark.slauncher.util.Logging.LOG;
import static ru.spark.slauncher.util.StringUtils.removePrefix;
import static ru.spark.slauncher.util.StringUtils.removeSuffix;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class GameItem extends Control {

    private static final ThreadPoolExecutor POOL_VERSION_RESOLVE = threadPool("VersionResolve", true, 1, 1, TimeUnit.SECONDS);

    private final Profile profile;
    private final String version;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty tag = new SimpleStringProperty();
    private final StringProperty subtitle = new SimpleStringProperty();
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    public GameItem(Profile profile, String id) {
        this.profile = profile;
        this.version = id;

        // GameVersion.minecraftVersion() is a time-costing job (up to ~200 ms)
        CompletableFuture.supplyAsync(() -> GameVersion.minecraftVersion(profile.getRepository().getVersionJar(id)).orElse(i18n("message.unknown")), POOL_VERSION_RESOLVE)
                .thenAcceptAsync(game -> {
                    StringBuilder libraries = new StringBuilder(game);
                    LibraryAnalyzer analyzer = LibraryAnalyzer.analyze(profile.getRepository().getResolvedPreservingPatchesVersion(id));
                    for (LibraryAnalyzer.LibraryMark mark : analyzer) {
                        String libraryId = mark.getLibraryId();
                        String libraryVersion = mark.getLibraryVersion();
                        if (libraryId.equals(MINECRAFT.getPatchId())) continue;
                        if (I18n.hasKey("install.installer." + libraryId)) {
                            libraries.append(", ").append(i18n("install.installer." + libraryId));
                            if (libraryVersion != null)
                                libraries.append(": ").append(modifyVersion("", libraryVersion.replaceAll("(?i)" + libraryId, "")));
                        }
                    }

                    subtitle.set(libraries.toString());
                }, Platform::runLater)
                .exceptionally(handleUncaught);

        CompletableFuture.runAsync(() -> {
            try {
                ModpackConfiguration<Void> config = profile.getRepository().readModpackConfiguration(version);
                if (config == null) return;
                tag.set(config.getVersion());
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to read modpack configuration from ", e);
            }
        }, Platform::runLater)
                .exceptionally(handleUncaught);

        title.set(id);
        image.set(profile.getRepository().getVersionIconImage(version));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new GameItemSkin(this);
    }

    public Profile getProfile() {
        return profile;
    }

    public String getVersion() {
        return version;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty tagProperty() {
        return tag;
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    private static String modifyVersion(String gameVersion, String version) {
        return removeSuffix(removePrefix(removeSuffix(removePrefix(version.replace(gameVersion, "").trim(), "-"), "-"), "_"), "_");
    }
}
