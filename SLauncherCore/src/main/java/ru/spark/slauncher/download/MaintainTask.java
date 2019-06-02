package ru.spark.slauncher.download;

import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.game.VersionLibraryBuilder;
import ru.spark.slauncher.task.TaskResult;

public class MaintainTask extends TaskResult<Version> {

    private final Version version;

    public MaintainTask(Version version) {
        this.version = version;
    }

    public static Version maintain(Version version) {
        if (version.getMainClass().contains("launchwrapper")) {
            return maintainGameWithLaunchWrapper(version);
        } else {
            // Vanilla Minecraft does not need maintain
            // Forge 1.13 support not implemented, not compatible with OptiFine currently.
            return version;
        }
    }

    private static Version maintainGameWithLaunchWrapper(Version version) {
        LibraryAnalyzer libraryAnalyzer = LibraryAnalyzer.analyze(version);
        VersionLibraryBuilder builder = new VersionLibraryBuilder(version);

        if (!libraryAnalyzer.hasForge()) {
            builder.removeTweakClass("forge");
        }

        // Installing Forge will override the Minecraft arguments in json, so LiteLoader and OptiFine Tweaker are being re-added.

        builder.removeTweakClass("liteloader");
        if (libraryAnalyzer.hasLiteLoader()) {
            builder.addArgument("--tweakClass", "com.mumfrey.liteloader.launch.LiteLoaderTweaker");
        }

        builder.removeTweakClass("optifine");
        if (libraryAnalyzer.hasOptiFine()) {
            if (!libraryAnalyzer.hasLiteLoader() && !libraryAnalyzer.hasForge()) {
                builder.addArgument("--tweakClass", "optifine.OptiFineTweaker");
            } else {
                // If forge or LiteLoader installed, OptiFine Forge Tweaker is needed.
                builder.addArgument("--tweakClass", "optifine.OptiFineForgeTweaker");
            }
        }

        return builder.build();
    }

    @Override
    public void execute() {
        setResult(maintain(version));
    }
}
