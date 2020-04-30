package ru.spark.slauncher.download.fabric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.game.Arguments;
import ru.spark.slauncher.game.Artifact;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.gson.JsonUtils;

import java.util.*;

/**
 * <b>Note</b>: Fabric should be installed first.
 *
 * @author spark1337
 */
public final class FabricInstallTask extends Task<Version> {

    private final DefaultDependencyManager dependencyManager;
    private final Version version;
    private final FabricRemoteVersion remote;
    private final GetTask launchMetaTask;
    private final List<Task<?>> dependencies = new LinkedList<>();

    public FabricInstallTask(DefaultDependencyManager dependencyManager, Version version, FabricRemoteVersion remoteVersion) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.remote = remoteVersion;

        launchMetaTask = new GetTask(dependencyManager.getDownloadProvider().injectURLsWithCandidates(remoteVersion.getUrls()));
        launchMetaTask.setCacheRepository(dependencyManager.getCacheRepository());
    }

    @Override
    public boolean doPreExecute() {
        return true;
    }

    @Override
    public void preExecute() throws Exception {
        if (!Objects.equals("net.minecraft.client.main.Main", version.resolve(dependencyManager.getGameRepository()).getMainClass()))
            throw new UnsupportedFabricInstallationException();
    }

    @Override
    public Collection<Task<?>> getDependents() {
        return Collections.singleton(launchMetaTask);
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean isRelyingOnDependencies() {
        return false;
    }

    @Override
    public void execute() {
        setResult(getPatch(JsonUtils.GSON.fromJson(launchMetaTask.getResult(), FabricInfo.class), remote.getGameVersion(), remote.getSelfVersion()));

        dependencies.add(dependencyManager.checkLibraryCompletionAsync(getResult(), true));
    }

    private Version getPatch(FabricInfo fabricInfo, String gameVersion, String loaderVersion) {
        JsonObject launcherMeta = fabricInfo.launcherMeta;
        Arguments arguments = new Arguments();

        String mainClass;
        if (!launcherMeta.get("mainClass").isJsonObject()) {
            mainClass = launcherMeta.get("mainClass").getAsString();
        } else {
            mainClass = launcherMeta.get("mainClass").getAsJsonObject().get("client").getAsString();
        }

        if (launcherMeta.has("launchwrapper")) {
            String clientTweaker = launcherMeta.get("launchwrapper").getAsJsonObject().get("tweakers").getAsJsonObject().get("client").getAsJsonArray().get(0).getAsString();
            arguments = arguments.addGameArguments("--tweakClass", clientTweaker);
        }

        JsonObject librariesObject = launcherMeta.getAsJsonObject("libraries");
        List<Library> libraries = new ArrayList<>();

        // "common, server" is hard coded in fabric installer.
        // Don't know the purpose of ignoring client libraries.
        for (String side : new String[]{"common", "server"}) {
            for (JsonElement element : librariesObject.getAsJsonArray(side)) {
                libraries.add(JsonUtils.GSON.fromJson(element, Library.class));
            }
        }

        libraries.add(new Library(Artifact.fromDescriptor(fabricInfo.intermediary.maven), "https://maven.fabricmc.net/", null));
        libraries.add(new Library(Artifact.fromDescriptor(fabricInfo.loader.maven), "https://maven.fabricmc.net/", null));

        return new Version(LibraryAnalyzer.LibraryType.FABRIC.getPatchId(), loaderVersion, 30000, arguments, mainClass, libraries);
    }

    public static class FabricInfo {
        private final LoaderInfo loader;
        private final IntermediaryInfo intermediary;
        private final JsonObject launcherMeta;

        public FabricInfo(LoaderInfo loader, IntermediaryInfo intermediary, JsonObject launcherMeta) {
            this.loader = loader;
            this.intermediary = intermediary;
            this.launcherMeta = launcherMeta;
        }

        public LoaderInfo getLoader() {
            return loader;
        }

        public IntermediaryInfo getIntermediary() {
            return intermediary;
        }

        public JsonObject getLauncherMeta() {
            return launcherMeta;
        }
    }

    public static class LoaderInfo {
        private final String separator;
        private final int build;
        private final String maven;
        private final String version;
        private final boolean stable;

        public LoaderInfo(String separator, int build, String maven, String version, boolean stable) {
            this.separator = separator;
            this.build = build;
            this.maven = maven;
            this.version = version;
            this.stable = stable;
        }

        public String getSeparator() {
            return separator;
        }

        public int getBuild() {
            return build;
        }

        public String getMaven() {
            return maven;
        }

        public String getVersion() {
            return version;
        }

        public boolean isStable() {
            return stable;
        }
    }

    public static class IntermediaryInfo {
        private final String maven;
        private final String version;
        private final boolean stable;

        public IntermediaryInfo(String maven, String version, boolean stable) {
            this.maven = maven;
            this.version = version;
            this.stable = stable;
        }

        public String getMaven() {
            return maven;
        }

        public String getVersion() {
            return version;
        }

        public boolean isStable() {
            return stable;
        }
    }

    public static class UnsupportedFabricInstallationException extends Exception {
    }
}
