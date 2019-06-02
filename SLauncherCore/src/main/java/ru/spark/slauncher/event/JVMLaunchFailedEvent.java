package ru.spark.slauncher.event;

import ru.spark.slauncher.util.ToStringBuilder;
import ru.spark.slauncher.util.platform.ManagedProcess;

/**
 * This event gets fired when we launch the JVM and it got crashed.
 * <br>
 * This event is fired on the {@link EventBus#EVENT_BUS}
 *
 * @author Spark1337
 */
public class JVMLaunchFailedEvent extends Event {

    private final ManagedProcess process;

    /**
     * Constructor.
     *
     * @param source  {@link ru.spark.slauncher.launch.ExitWaiter}
     * @param process the crashed process.
     */
    public JVMLaunchFailedEvent(Object source, ManagedProcess process) {
        super(source);
        this.process = process;
    }

    public ManagedProcess getProcess() {
        return process;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("source", source)
                .append("process", process)
                .toString();
    }
}
