package ru.spark.slauncher.launch;

import ru.spark.slauncher.event.EventBus;
import ru.spark.slauncher.event.JVMLaunchFailedEvent;
import ru.spark.slauncher.event.ProcessExitedAbnormallyEvent;
import ru.spark.slauncher.event.ProcessStoppedEvent;
import ru.spark.slauncher.util.Log4jLevel;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.platform.ManagedProcess;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author Spark1337
 */
final class ExitWaiter implements Runnable {

    private final ManagedProcess process;
    private final Collection<Thread> joins;
    private final BiConsumer<Integer, ProcessListener.ExitType> watcher;

    /**
     * Constructor.
     *
     * @param process the process to wait for
     * @param watcher the callback that will be called after process stops.
     */
    public ExitWaiter(ManagedProcess process, Collection<Thread> joins, BiConsumer<Integer, ProcessListener.ExitType> watcher) {
        this.process = process;
        this.joins = joins;
        this.watcher = watcher;
    }

    @Override
    public void run() {
        try {
            int exitCode = process.getProcess().waitFor();

            for (Thread thread : joins)
                thread.join();

            List<String> errorLines = process.getLines().stream()
                    .filter(Log4jLevel::guessLogLineError).collect(Collectors.toList());
            ProcessListener.ExitType exitType;

            // LaunchWrapper will catch the exception logged and will exit normally.
            if (exitCode != 0 && StringUtils.containsOne(errorLines,
                    "Could not create the Java Virtual Machine.",
                    "Error occurred during initialization of VM",
                    "A fatal exception has occurred. Program will exit.")) {
                EventBus.EVENT_BUS.fireEvent(new JVMLaunchFailedEvent(this, process));
                exitType = ProcessListener.ExitType.JVM_ERROR;
            } else if (exitCode != 0 || StringUtils.containsOne(errorLines, "Unable to launch")) {
                EventBus.EVENT_BUS.fireEvent(new ProcessExitedAbnormallyEvent(this, process));
                exitType = ProcessListener.ExitType.APPLICATION_ERROR;
            } else
                exitType = ProcessListener.ExitType.NORMAL;

            EventBus.EVENT_BUS.fireEvent(new ProcessStoppedEvent(this, process));

            watcher.accept(exitCode, exitType);
        } catch (InterruptedException e) {
            watcher.accept(1, ProcessListener.ExitType.INTERRUPTED);
        }
    }

}
