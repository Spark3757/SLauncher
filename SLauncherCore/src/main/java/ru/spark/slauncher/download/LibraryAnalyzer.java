package ru.spark.slauncher.download;

import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;

import java.util.Optional;

public final class LibraryAnalyzer {
    private final Library forge;
    private final Library liteLoader;
    private final Library optiFine;

    public LibraryAnalyzer(Library forge, Library liteLoader, Library optiFine) {
        this.forge = forge;
        this.liteLoader = liteLoader;
        this.optiFine = optiFine;
    }

    public static LibraryAnalyzer analyze(Version version) {
        Library forge = null, liteLoader = null, optiFine = null;

        for (Library library : version.getLibraries()) {
            String groupId = library.getGroupId();
            String artifactId = library.getArtifactId();
            if (groupId.equalsIgnoreCase("net.minecraftforge") && artifactId.equalsIgnoreCase("forge"))
                forge = library;

            if (groupId.equalsIgnoreCase("com.mumfrey") && artifactId.equalsIgnoreCase("liteloader"))
                liteLoader = library;

            if ((groupId.equalsIgnoreCase("optifine") || groupId.equalsIgnoreCase("net.optifine")) && artifactId.equalsIgnoreCase("optifine"))
                optiFine = library;
        }

        return new LibraryAnalyzer(forge, liteLoader, optiFine);
    }

    public Optional<Library> getForge() {
        return Optional.ofNullable(forge);
    }

    public boolean hasForge() {
        return forge != null;
    }

    public Optional<Library> getLiteLoader() {
        return Optional.ofNullable(liteLoader);
    }

    public boolean hasLiteLoader() {
        return liteLoader != null;
    }

    public Optional<Library> getOptiFine() {
        return Optional.ofNullable(optiFine);
    }

    public boolean hasOptiFine() {
        return optiFine != null;
    }
}
