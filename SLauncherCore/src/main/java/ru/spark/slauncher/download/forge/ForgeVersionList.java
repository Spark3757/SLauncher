package ru.spark.slauncher.download.forge;

import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.VersionList;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Spark1337
 */
public final class ForgeVersionList extends VersionList<ForgeRemoteVersion> {

    public static final ForgeVersionList INSTANCE = new ForgeVersionList();
    public static final String FORGE_LIST = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/json";

    private ForgeVersionList() {
    }

    @Override
    public boolean hasType() {
        return false;
    }

    @Override
    public Task refreshAsync(DownloadProvider downloadProvider) {
        final GetTask task = new GetTask(NetworkUtils.toURL(downloadProvider.injectURL(FORGE_LIST)));
        return new Task() {

            @Override
            public Collection<Task> getDependents() {
                return Collections.singleton(task);
            }

            @Override
            public void execute() {
                lock.writeLock().lock();

                try {
                    ForgeVersionRoot root = JsonUtils.GSON.fromJson(task.getResult(), ForgeVersionRoot.class);
                    if (root == null)
                        return;
                    versions.clear();

                    for (Map.Entry<String, int[]> entry : root.getGameVersions().entrySet()) {
                        String gameVersion = VersionNumber.normalize(entry.getKey());
                        for (int v : entry.getValue()) {
                            ForgeVersion version = root.getNumber().get(v);
                            if (version == null)
                                continue;
                            String jar = null;
                            for (String[] file : version.getFiles())
                                if (file.length > 1 && "installer".equals(file[1])) {
                                    String classifier = version.getGameVersion() + "-" + version.getVersion()
                                            + (StringUtils.isNotBlank(version.getBranch()) ? "-" + version.getBranch() : "");
                                    String fileName = root.getArtifact() + "-" + classifier + "-" + file[1] + "." + file[0];
                                    jar = downloadProvider.injectURL(root.getWebPath() + classifier + "/" + fileName);
                                }

                            if (jar == null)
                                continue;
                            versions.put(gameVersion, new ForgeRemoteVersion(
                                    version.getGameVersion(), version.getVersion(), jar
                            ));
                        }
                    }
                } finally {
                    lock.writeLock().unlock();
                }
            }

        };
    }
}
