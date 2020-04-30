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

    private static volatile ThreadPoolExecutor CACHED_EXECUTOR;

    public static synchronized ThreadPoolExecutor newThread() {
        if (CACHED_EXECUTOR == null)
            CACHED_EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60, TimeUnit.SECONDS, new SynchronousQueue<>(), Executors.defaultThreadFactory());

        return CACHED_EXECUTOR;
    }

    private static volatile ExecutorService IO_EXECUTOR;

    public static synchronized ExecutorService io() {
        if (IO_EXECUTOR == null) {
            int threads = Math.min(Runtime.getRuntime().availableProcessors() * 4, 64);
            IO_EXECUTOR = Executors.newFixedThreadPool(threads,
                    runnable -> {
                        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                        thread.setDaemon(true);
                        return thread;
                    });
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
        return newThread();
    }

    public static synchronized void shutdown() {
        Logging.LOG.info("Shutting down executor services.");

        // shutdownNow will interrupt all threads.
        // So when we want to close the app, no threads need to be waited for finish.
        // Sometimes it resolves the problem that the app does not exit.

        if (CACHED_EXECUTOR != null)
            CACHED_EXECUTOR.shutdownNow();

        if (IO_EXECUTOR != null)
            IO_EXECUTOR.shutdownNow();
    }

    public static Future<?> schedule(Executor executor, Runnable command) {
        FutureTask<?> future = new FutureTask<Void>(command, null);
        executor.execute(future);
        return future;
    }
}
