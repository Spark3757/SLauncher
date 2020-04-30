package ru.spark.slauncher.mod;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author spark1337
 */
public final class ModInfo implements Comparable<ModInfo> {

    private Path file;
    private final String name;
    private final String description;
    private final String authors;
    private final String version;
    private final String gameVersion;
    private final String url;
    private final String fileName;
    private final BooleanProperty activeProperty;

    public ModInfo(ModManager modManager, File file, String name, String description) {
        this(modManager, file, name, description, "", "", "", "");
    }

    public ModInfo(ModManager modManager, File file, String name, String description, String authors, String version, String gameVersion, String url) {
        this.file = file.toPath();
        this.name = name;
        this.description = description;
        this.authors = authors;
        this.version = version;
        this.gameVersion = gameVersion;
        this.url = url;

        activeProperty = new SimpleBooleanProperty(this, "active", !modManager.isDisabled(file)) {
            @Override
            protected void invalidated() {
                Path path = ModInfo.this.file.toAbsolutePath();

                try {
                    if (get())
                        ModInfo.this.file = modManager.enableMod(path);
                    else
                        ModInfo.this.file = modManager.disableMod(path);
                } catch (IOException e) {
                    Logging.LOG.log(Level.SEVERE, "Unable to invert state of mod file " + path, e);
                }
            }
        };

        fileName = StringUtils.substringBeforeLast(isActive() ? file.getName() : FileUtils.getNameWithoutExtension(file), '.');
    }

    public Path getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthors() {
        return authors;
    }

    public String getVersion() {
        return version;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getUrl() {
        return url;
    }

    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    public boolean isActive() {
        return activeProperty.get();
    }

    public void setActive(boolean active) {
        activeProperty.set(active);
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public int compareTo(ModInfo o) {
        return getFileName().compareTo(o.getFileName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ModInfo && Objects.equals(getFileName(), ((ModInfo) obj).getFileName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileName());
    }
}
