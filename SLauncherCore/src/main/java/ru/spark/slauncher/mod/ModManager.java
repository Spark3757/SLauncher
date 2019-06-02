package ru.spark.slauncher.mod;

import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.TreeSet;

public final class ModManager {
    public static final String DISABLED_EXTENSION = ".disabled";
    private final GameRepository repository;
    private final String id;
    private final TreeSet<ModInfo> modInfos = new TreeSet<>();
    private boolean loaded = false;

    public ModManager(GameRepository repository, String id) {
        this.repository = repository;
        this.id = id;
    }

    private Path getModsDirectory() {
        return repository.getRunDirectory(id).toPath().resolve("mods");
    }

    private void addModInfo(File file) {
        try {
            modInfos.add(getModInfo(file));
        } catch (IllegalArgumentException ignore) {
        }
    }

    public ModInfo getModInfo(File modFile) {
        File file = isDisabled(modFile) ? new File(modFile.getAbsoluteFile().getParentFile(), FileUtils.getNameWithoutExtension(modFile)) : modFile;
        String description, extension = FileUtils.getExtension(file);
        switch (extension) {
            case "zip":
            case "jar":
                try {
                    return ForgeModMetadata.fromFile(this, modFile);
                } catch (Exception ignore) {
                }

                try {
                    return FabricModMetadata.fromFile(this, modFile);
                } catch (Exception ignore) {
                }

                try {
                    return PackMcMeta.fromFile(this, modFile);
                } catch (Exception ignore) {
                }

                description = "";
                break;
            case "litemod":
                try {
                    return LiteModMetadata.fromFile(this, modFile);
                } catch (Exception ignore) {
                    description = "LiteLoader Mod";
                }
                break;
            default:
                throw new IllegalArgumentException("File " + modFile + " is not a mod file.");
        }
        return new ModInfo(this, modFile, FileUtils.getNameWithoutExtension(modFile), description);
    }

    public void refreshMods() throws IOException {
        modInfos.clear();
        if (Files.isDirectory(getModsDirectory())) {
            for (Path subitem : Files.newDirectoryStream(getModsDirectory())) {
                if (Files.isDirectory(subitem) && VersionNumber.isIntVersionNumber(FileUtils.getName(subitem))) {
                    // If the folder name is game version, forge will search mod in this subdirectory
                    for (Path subsubitem : Files.newDirectoryStream(subitem))
                        addModInfo(subsubitem.toFile());
                } else {
                    addModInfo(subitem.toFile());
                }
            }
        }
        loaded = true;
    }

    public Collection<ModInfo> getMods() throws IOException {
        if (!loaded)
            refreshMods();
        return modInfos;
    }

    public void addMod(File file) throws IOException {
        if (!isFileMod(file))
            throw new IllegalArgumentException("File " + file + " is not a valid mod file.");

        if (!loaded)
            refreshMods();

        File modsDirectory = new File(repository.getRunDirectory(id), "mods");
        if (!FileUtils.makeDirectory(modsDirectory))
            throw new IOException("Cannot make directory " + modsDirectory);

        File newFile = new File(modsDirectory, file.getName());
        FileUtils.copyFile(file, newFile);

        addModInfo(newFile);
    }

    public void removeMods(ModInfo... modInfos) throws IOException {
        for (ModInfo modInfo : modInfos) {
            Files.deleteIfExists(modInfo.getFile());
        }
    }

    public Path disableMod(Path file) throws IOException {
        Path disabled = file.getParent().resolve(StringUtils.addSuffix(FileUtils.getName(file), DISABLED_EXTENSION));
        if (Files.exists(file))
            Files.move(file, disabled, StandardCopyOption.REPLACE_EXISTING);
        return disabled;
    }

    public Path enableMod(Path file) throws IOException {
        Path enabled = file.getParent().resolve(StringUtils.removeSuffix(FileUtils.getName(file), DISABLED_EXTENSION));
        if (Files.exists(file))
            Files.move(file, enabled, StandardCopyOption.REPLACE_EXISTING);
        return enabled;
    }

    public boolean isDisabled(File file) {
        return file.getPath().endsWith(DISABLED_EXTENSION);
    }

    public boolean isFileMod(File file) {
        String name = file.getName();
        if (isDisabled(file))
            name = FileUtils.getNameWithoutExtension(file);
        return name.endsWith(".zip") || name.endsWith(".jar") || name.endsWith(".litemod");
    }

    /**
     * Check if "mods" directory has mod file named "fileName" no matter the mod is disabled or not
     *
     * @param fileName name of the file whose existence is being checked
     * @return true if the file exists
     */
    public boolean hasSimpleMod(String fileName) {
        return Files.exists(getModsDirectory().resolve(StringUtils.removeSuffix(fileName, DISABLED_EXTENSION)))
                || Files.exists(getModsDirectory().resolve(StringUtils.addSuffix(fileName, DISABLED_EXTENSION)));
    }

    public Path getSimpleModPath(String fileName) {
        return getModsDirectory().resolve(fileName);
    }
}
