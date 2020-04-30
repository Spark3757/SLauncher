package ru.spark.slauncher.download.optifine;

import com.google.gson.reflect.TypeToken;
import ru.spark.slauncher.download.VersionList;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.util.*;

/**
 * @author spark1337
 */
public final class OptiFineBMCLVersionList extends VersionList<OptiFineRemoteVersion> {
    private final String apiRoot;

    /**
     * @param apiRoot API Root of BMCLAPI implementations
     */
    public OptiFineBMCLVersionList(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    public boolean hasType() {
        return true;
    }

    @Override
    public Task<?> refreshAsync() {
        GetTask task = new GetTask(NetworkUtils.toURL(apiRoot + "/optifine/versionlist"));
        return new Task<Void>() {
            @Override
            public Collection<Task<?>> getDependents() {
                return Collections.singleton(task);
            }

            @Override
            public void execute() {
                lock.writeLock().lock();

                try {
                    versions.clear();
                    Set<String> duplicates = new HashSet<>();
                    List<OptiFineVersion> root = JsonUtils.GSON.fromJson(task.getResult(), new TypeToken<List<OptiFineVersion>>() {
                    }.getType());
                    for (OptiFineVersion element : root) {
                        String version = element.getType() + "_" + element.getPatch();
                        String mirror = "https://bmclapi2.bangbang93.com/optifine/" + element.getGameVersion() + "/" + element.getType() + "/" + element.getPatch();
                        if (!duplicates.add(mirror))
                            continue;

                        boolean isPre = element.getPatch() != null && (element.getPatch().startsWith("pre") || element.getPatch().startsWith("alpha"));

                        if (StringUtils.isBlank(element.getGameVersion()))
                            continue;

                        String gameVersion = VersionNumber.normalize(element.getGameVersion());
                        versions.put(gameVersion, new OptiFineRemoteVersion(gameVersion, version, Collections.singletonList(mirror), isPre));
                    }
                } finally {
                    lock.writeLock().unlock();
                }
            }
        };
    }

}
