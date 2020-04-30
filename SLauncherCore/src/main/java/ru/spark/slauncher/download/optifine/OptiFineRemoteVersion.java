package ru.spark.slauncher.download.optifine;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;

import java.util.List;

public class OptiFineRemoteVersion extends RemoteVersion {

    public OptiFineRemoteVersion(String gameVersion, String selfVersion, List<String> urls, boolean snapshot) {
        super(LibraryAnalyzer.LibraryType.OPTIFINE.getPatchId(), gameVersion, selfVersion, snapshot ? Type.SNAPSHOT : Type.RELEASE, urls);
    }

    @Override
    public Task<Version> getInstallTask(DefaultDependencyManager dependencyManager, Version baseVersion) {
        return new OptiFineInstallTask(dependencyManager, baseVersion, this);
    }
}
