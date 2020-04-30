package ru.spark.slauncher.game;

import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.launch.DefaultLauncher;
import ru.spark.slauncher.launch.ProcessListener;

import java.util.Map;

/**
 * @author spark1337
 */
public final class SLGameLauncher extends DefaultLauncher {

    public SLGameLauncher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options) {
        this(repository, version, authInfo, options, null);
    }

    public SLGameLauncher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options, ProcessListener listener) {
        this(repository, version, authInfo, options, listener, true);
    }

    public SLGameLauncher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options, ProcessListener listener, boolean daemon) {
        super(repository, version, authInfo, options, listener, daemon);
    }

    @Override
    protected Map<String, String> getConfigurations() {
        Map<String, String> res = super.getConfigurations();
        res.put("${launcher_name}", Metadata.NAME);
        res.put("${launcher_version}", Metadata.VERSION);
        return res;
    }
}
