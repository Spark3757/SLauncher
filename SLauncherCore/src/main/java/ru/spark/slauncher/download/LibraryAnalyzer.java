package ru.spark.slauncher.download;

import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class LibraryAnalyzer {
    private final Map<LibraryType, Library> libraries;

    private LibraryAnalyzer(Map<LibraryType, Library> libraries) {
        this.libraries = libraries;
    }

    public static LibraryAnalyzer analyze(Version version) {
        Map<LibraryType, Library> libraries = new EnumMap<>(LibraryType.class);

        for (Library library : version.getLibraries()) {
            String groupId = library.getGroupId();
            String artifactId = library.getArtifactId();

            for (LibraryType type : LibraryType.values()) {
                if (type.group.matcher(groupId).matches() && type.artifact.matcher(artifactId).matches()) {
                    libraries.put(type, library);
                    break;
                }
            }
        }

        return new LibraryAnalyzer(libraries);
    }

    public Optional<Library> get(LibraryType type) {
        return Optional.ofNullable(libraries.get(type));
    }

    public boolean has(LibraryType type) {
        return libraries.containsKey(type);
    }

    public boolean hasModLoader() {
        return Arrays.stream(LibraryType.values())
                .filter(LibraryType::isModLoader)
                .anyMatch(this::has);
    }

    public enum LibraryType {
        FORGE(true, Pattern.compile("net\\.minecraftforge"), Pattern.compile("forge")),
        LITELOADER(true, Pattern.compile("com\\.mumfrey"), Pattern.compile("liteloader")),
        OPTIFINE(false, Pattern.compile("(net\\.)?optifine"), Pattern.compile(".*")),
        FABRIC(true, Pattern.compile("net\\.fabricmc"), Pattern.compile("fabric-loader"));

        private final Pattern group, artifact;
        private final boolean modLoader;

        LibraryType(boolean modLoader, Pattern group, Pattern artifact) {
            this.modLoader = modLoader;
            this.group = group;
            this.artifact = artifact;
        }

        public boolean isModLoader() {
            return modLoader;
        }
    }
}