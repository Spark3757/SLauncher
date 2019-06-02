package ru.spark.slauncher.launch;

import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.LaunchOptions;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.util.platform.ManagedProcess;

import java.io.File;
import java.io.IOException;

/**
 * @author Spark1337
 */
public abstract class Launcher {

    protected final GameRepository repository;
    protected final String versionId;
    protected final Version version;
    protected final AuthInfo authInfo;
    protected final LaunchOptions options;
    protected final ProcessListener listener;
    protected final boolean daemon;

    public Launcher(GameRepository repository, String versionId, AuthInfo authInfo, LaunchOptions options) {
        this(repository, versionId, authInfo, options, null);
    }

    public Launcher(GameRepository repository, String versionId, AuthInfo authInfo, LaunchOptions options, ProcessListener listener) {
        this(repository, versionId, authInfo, options, listener, true);
    }

    public Launcher(GameRepository repository, String versionId, AuthInfo authInfo, LaunchOptions options, ProcessListener listener, boolean daemon) {
        this.repository = repository;
        this.versionId = versionId;
        this.authInfo = authInfo;
        this.options = options;
        this.listener = listener;
        this.daemon = daemon;

        version = repository.getResolvedVersion(versionId);
    }

    /**
     * @param file the file path.
     */
    public abstract void makeLaunchScript(File file) throws IOException;

    public abstract ManagedProcess launch() throws IOException, InterruptedException;

}
