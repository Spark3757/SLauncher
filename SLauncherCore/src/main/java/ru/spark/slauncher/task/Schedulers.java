package ru.spark.slauncher.task;

import ru.spark.slauncher.util.Logging;

import java.util.concurrent.*;

/**
 * @author Spark1337
 */
public final class Schedulers {

    static final Scheduler NONE = new SchedulerImpl(any -> {
    });
    private static final Scheduler IMMEDIATE = new SchedulerImpl(Runnable::run);
    private static final Scheduler JAVAFX = new SchedulerImpl(javafx.application.Platform::runLater);
    private static final Scheduler SWING = new SchedulerImpl(javax.swing.SwingUtilities::invokeLater);
    private static volatile ExecutorService CACHED_EXECUTOR;
    private static volatile ExecutorService IO_EXECUTOR;
    private static volatile ExecutorService SINGLE_EXECUTOR;
    private static Scheduler NEW_THREAD;
    private static Scheduler IO;
    private static Scheduler COMPUTATION;

    private Schedulers() {
    }

    private static synchronized ExecutorService getCachedExecutorService() {
        if (CACHED_EXECUTOR == null)
            CACHED_EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60, TimeUnit.SECONDS, new SynchronousQueue<>(), Executors.defaultThreadFactory());

        return CACHED_EXECUTOR;
    }

    private static synchronized ExecutorService getIOExecutorService() {
        if (IO_EXECUTOR == null)
            IO_EXECUTOR = Executors.newFixedThreadPool(6, runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);
                return thread;
            });

        return IO_EXECUTOR;
    }

    private static synchronized ExecutorService getSingleExecutorService() {
        if (SINGLE_EXECUTOR == null)
            SINGLE_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);
                return thread;
            });

        return SINGLE_EXECUTOR;
    }

    public static Scheduler immediate() {
        return IMMEDIATE;
    }

    public static synchronized Scheduler newThread() {
        if (NEW_THREAD == null)
            NEW_THREAD = new SchedulerExecutorService(getCachedExecutorService());
        return NEW_THREAD;
    }

    public static synchronized Scheduler io() {
        if (IO == null)
            IO = new SchedulerExecutorService(getIOExecutorService());
        return IO;
    }

    public static synchronized Scheduler computation() {
        if (COMPUTATION == null)
            COMPUTATION = new SchedulerExecutorService(getSingleExecutorService());
        return COMPUTATION;
    }

    public static Scheduler javafx() {
        return JAVAFX;
    }

    public static Scheduler swing() {
        return SWING;
    }

    public static synchronized Scheduler defaultScheduler() {
        return newThread();
    }

    public static synchronized void shutdown() {
        Logging.LOG.info("Shutting down executor services.");

        if (CACHED_EXECUTOR != null)
            CACHED_EXECUTOR.shutdownNow();

        if (IO_EXECUTOR != null)
            IO_EXECUTOR.shutdownNow();

        if (SINGLE_EXECUTOR != null)
            SINGLE_EXECUTOR.shutdownNow();
    }
}
