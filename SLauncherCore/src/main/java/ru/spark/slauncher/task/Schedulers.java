package ru.spark.slauncher.task;

import javafx.application.Platform;
import ru.spark.slauncher.util.Logging;

import javax.swing.*;
import java.util.concurrent.*;

/**
 * @author spark1337
 */
public final class Schedulers {

    private Schedulers() {
    }

    private static volatile ExecutorService IO_EXECUTOR;

    /**
     * Get singleton instance of the thread pool for I/O operations,
     * usually for reading files from disk, or Internet connections.
     *
     * This thread pool has no more than 4 threads, and number of threads will get
     * reduced if concurrency is less than thread number.
     *
     * @return Thread pool for I/O operations.
     */
    public static ExecutorService io() {
        if (IO_EXECUTOR == null) {
            synchronized (Schedulers.class) {
                if (IO_EXECUTOR == null) {
                    IO_EXECUTOR = new ThreadPoolExecutor(0, 4, 10, TimeUnit.SECONDS, new SynchronousQueue<>(),
                            runnable -> {
                                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                                thread.setDaemon(true);
                                return thread;
                            });
                }
            }
        }

        return IO_EXECUTOR;
    }

    public static Executor javafx() {
        return Platform::runLater;
    }

    public static Executor swing() {
        return SwingUtilities::invokeLater;
    }

    public static Executor defaultScheduler() {
        return ForkJoinPool.commonPool();
    }

    public static synchronized void shutdown() {
        Logging.LOG.info("Shutting down executor services.");

        // shutdownNow will interrupt all threads.
        // So when we want to close the app, no threads need to be waited for finish.
        // Sometimes it resolves the problem that the app does not exit.

        if (IO_EXECUTOR != null)
            IO_EXECUTOR.shutdownNow();
    }

}