package ru.spark.slauncher.launch;

import ru.spark.slauncher.util.Log4jLevel;
import ru.spark.slauncher.util.platform.ManagedProcess;

/**
 * @author Spark1337
 */
public interface ProcessListener {

    /**
     * When a game launched, this method will be called to get the new process.
     * You should not override this method when your ProcessListener is shared with all processes.
     */
    default void setProcess(ManagedProcess process) {
    }

    /**
     * Called when receiving a log from stdout/stderr.
     * <p>
     * Does not guarantee that this method is thread safe.
     *
     * @param log the log
     */
    void onLog(String log, Log4jLevel level);

    /**
     * Called when the game process stops.
     *
     * @param exitCode the exit code
     */
    void onExit(int exitCode, ExitType exitType);

    enum ExitType {
        JVM_ERROR,
        APPLICATION_ERROR,
        NORMAL,
        INTERRUPTED
    }
}
