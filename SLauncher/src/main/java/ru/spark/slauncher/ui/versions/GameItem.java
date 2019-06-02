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
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.StringUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class GameItem extends Control {

    private static final ThreadPoolExecutor POOL_VERSION_RESOLVE = Lang.threadPool("VersionResolve", true, 1, 1, TimeUnit.SECONDS);

    private final Profile profile;
    private final String version;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty subtitle = new SimpleStringProperty();
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    public GameItem(Profile profile, String id) {
        this.profile = profile;
        this.version = id;

        // GameVersion.minecraftVersion() is a time-costing job (up to ~200 ms)
        CompletableFuture.supplyAsync(() -> GameVersion.minecraftVersion(profile.getRepository().getVersionJar(id)).orElse("Unknown"), POOL_VERSION_RESOLVE)
                .thenAcceptAsync(game -> {
                    StringBuilder libraries = new StringBuilder(game);
                    LibraryAnalyzer analyzer = LibraryAnalyzer.analyze(profile.getRepository().getVersion(id));
                    analyzer.getForge().ifPresent(library -> libraries.append(", ").append(i18n("install.installer.forge")).append(": ").append(modifyVersion(game, library.getVersion().replaceAll("(?i)forge", ""))));
                    analyzer.getLiteLoader().ifPresent(library -> libraries.append(", ").append(i18n("install.installer.liteloader")).append(": ").append(modifyVersion(game, library.getVersion().replaceAll("(?i)liteloader", ""))));
                    analyzer.getOptiFine().ifPresent(library -> libraries.append(", ").append(i18n("install.installer.optifine")).append(": ").append(modifyVersion(game, library.getVersion().replaceAll("(?i)optifine", ""))));
                    subtitle.set(libraries.toString());
                }, Platform::runLater)
                .exceptionally(Lang.handleUncaught);

        title.set(id);
        image.set(profile.getRepository().getVersionIconImage(version));
    }

    private static String modifyVersion(String gameVersion, String version) {
        return StringUtils.removeSuffix(StringUtils.removePrefix(StringUtils.removeSuffix(StringUtils.removePrefix(version.replace(gameVersion, "").trim(), "-"), "-"), "_"), "_");
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

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }
}
