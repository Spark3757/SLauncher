package ru.spark.slauncher.download.optifine;

import com.google.gson.reflect.TypeToken;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.VersionList;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.util.*;

/**
 * @author Spark1337
 */
public final class OptiFineBMCLVersionList extends VersionList<OptiFineRemoteVersion> {

    public static final OptiFineBMCLVersionList INSTANCE = new OptiFineBMCLVersionList();

    private OptiFineBMCLVersionList() {
    }

    @Override
    public boolean hasType() {
        return true;
    }

    @Override
    public Task refreshAsync(DownloadProvider downloadProvider) {
        GetTask task = new GetTask(NetworkUtils.toURL("http://bmclapi2.bangbang93.com/optifine/versionlist"));
        return new Task() {
            @Override
            public Collection<Task> getDependents() {
                return Collections.singleton(task);
            }

            @Override
            public void execute() {
                versions.clear();
                Set<String> duplicates = new HashSet<>();
                List<OptiFineVersion> root = JsonUtils.GSON.fromJson(task.getResult(), new TypeToken<List<OptiFineVersion>>() {
                }.getType());
                for (OptiFineVersion element : root) {
                    String version = element.getType() + "_" + element.getPatch();
                    String mirror = "http://bmclapi2.bangbang93.com/optifine/" + element.getGameVersion() + "/" + element.getType() + "/" + element.getPatch();
                    if (!duplicates.add(mirror))
                        continue;

                    boolean isPre = element.getPatch() != null && (element.getPatch().startsWith("pre") || element.getPatch().startsWith("alpha"));

                    if (StringUtils.isBlank(element.getGameVersion()))
                        continue;

                    String gameVersion = VersionNumber.normalize(element.getGameVersion());
                    versions.put(gameVersion, new OptiFineRemoteVersion(gameVersion, version, () -> mirror, isPre));
                }
            }
        };
    }

}
