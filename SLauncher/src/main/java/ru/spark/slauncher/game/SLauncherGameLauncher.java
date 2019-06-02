package ru.spark.slauncher.game;

import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.launch.DefaultLauncher;
import ru.spark.slauncher.launch.ProcessListener;

import java.util.Map;

/**
 * @author Spark1337
 */
public final class SLauncherGameLauncher extends DefaultLauncher {

    public SLauncherGameLauncher(GameRepository repository, String versionId, AuthInfo authInfo, LaunchOptions options) {
        this(repository, versionId, authInfo, options, null);
    }

    public SLauncherGameLauncher(GameRepository repository, String versionId, AuthInfo authInfo, LaunchOptions options, ProcessListener listener) {
        this(repository, versionId, authInfo, options, listener, true);
    }

    public SLauncherGameLauncher(GameRepository repository, String versionId, AuthInfo authInfo, LaunchOptions options, ProcessListener listener, boolean daemon) {
        super(repository, versionId, authInfo, options, listener, daemon);
    }

    @Override
    protected Map<String, String> getConfigurations() {
        Map<String, String> res = super.getConfigurations();
        res.put("${launcher_name}", Metadata.NAME);
        res.put("${launcher_version}", Metadata.VERSION);
        return res;
    }
}
