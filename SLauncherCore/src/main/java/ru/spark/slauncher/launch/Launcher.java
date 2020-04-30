package ru.spark.slauncher.launch;

import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.LaunchOptions;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.util.platform.ManagedProcess;

import java.io.File;
import java.io.IOException;

/**
 * @author spark1337
 */
public abstract class Launcher {

    protected final GameRepository repository;
    protected final Version version;
    protected final AuthInfo authInfo;
    protected final LaunchOptions options;
    protected final ProcessListener listener;
    protected final boolean daemon;

    public Launcher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options) {
        this(repository, version, authInfo, options, null);
    }

    public Launcher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options, ProcessListener listener) {
        this(repository, version, authInfo, options, listener, true);
    }

    public Launcher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options, ProcessListener listener, boolean daemon) {
        this.repository = repository;
        this.version = version;
        this.authInfo = authInfo;
        this.options = options;
        this.listener = listener;
        this.daemon = daemon;
    }

    /**
     * @param file the file path.
     */
    public abstract void makeLaunchScript(File file) throws IOException;

    public abstract ManagedProcess launch() throws IOException, InterruptedException;

}
