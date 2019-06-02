package ru.spark.slauncher.download.liteloader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.VersionList;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Spark1337
 */
public final class LiteLoaderBMCLVersionList extends VersionList<LiteLoaderRemoteVersion> {

    public static final LiteLoaderBMCLVersionList INSTANCE = new LiteLoaderBMCLVersionList();
    public static final String LITELOADER_LIST = "http://dl.liteloader.com/versions/versions.json";

    private LiteLoaderBMCLVersionList() {
    }

    private static String getLatestSnapshotVersion(String repo) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(repo + "maven-metadata.xml");
        Element r = doc.getDocumentElement();
        Element snapshot = (Element) r.getElementsByTagName("snapshot").item(0);
        Node timestamp = snapshot.getElementsByTagName("timestamp").item(0);
        Node buildNumber = snapshot.getElementsByTagName("buildNumber").item(0);
        return timestamp.getTextContent() + "-" + buildNumber.getTextContent();
    }

    @Override
    public boolean hasType() {
        return false;
    }

    @Override
    public Task refreshAsync(DownloadProvider downloadProvider) {
        GetTask task = new GetTask(NetworkUtils.toURL(downloadProvider.injectURL(LITELOADER_LIST)));
        return new Task() {
            @Override
            public Collection<Task> getDependents() {
                return Collections.singleton(task);
            }

            @Override
            public void execute() {
                lock.writeLock().lock();

                try {
                    LiteLoaderVersionsRoot root = JsonUtils.GSON.fromJson(task.getResult(), LiteLoaderVersionsRoot.class);
                    versions.clear();

                    for (Map.Entry<String, LiteLoaderGameVersions> entry : root.getVersions().entrySet()) {
                        String gameVersion = entry.getKey();
                        LiteLoaderGameVersions liteLoader = entry.getValue();

                        String gg = VersionNumber.normalize(gameVersion);
                        doBranch(gg, gameVersion, liteLoader.getRepoitory(), liteLoader.getArtifacts(), false);
                        doBranch(gg, gameVersion, liteLoader.getRepoitory(), liteLoader.getSnapshots(), true);
                    }
                } finally {
                    lock.writeLock().unlock();
                }
            }

            private void doBranch(String key, String gameVersion, LiteLoaderRepository repository, LiteLoaderBranch branch, boolean snapshot) {
                if (branch == null || repository == null)
                    return;

                for (Map.Entry<String, LiteLoaderVersion> entry : branch.getLiteLoader().entrySet()) {
                    String branchName = entry.getKey();
                    LiteLoaderVersion v = entry.getValue();
                    if ("latest".equals(branchName))
                        continue;

                    String version = v.getVersion();
                    String url = "http://bmclapi2.bangbang93.com/liteloader/download?version=" + version;
                    if (snapshot) {
                        try {
                            version = version.replace("SNAPSHOT", getLatestSnapshotVersion(repository.getUrl() + "com/mumfrey/liteloader/" + v.getVersion() + "/"));
                            url = repository.getUrl() + "com/mumfrey/liteloader/" + v.getVersion() + "/liteloader-" + version + "-release.jar";
                        } catch (Exception ignore) {
                        }
                    }

                    versions.put(key, new LiteLoaderRemoteVersion(gameVersion,
                            version, downloadProvider.injectURL(url),
                            v.getTweakClass(), v.getLibraries()
                    ));
                }
            }
        };
    }
}
