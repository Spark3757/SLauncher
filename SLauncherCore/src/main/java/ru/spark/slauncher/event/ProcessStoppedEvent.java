package ru.spark.slauncher.event;

import ru.spark.slauncher.launch.ExitWaiter;
import ru.spark.slauncher.util.platform.ManagedProcess;

/**
 * This event gets fired when minecraft process exited successfully and the exit code is 0.
 * <br>
 * This event is fired on the {@link EventBus#EVENT_BUS}
 *
 * @author spark1337
 */
public class ProcessStoppedEvent extends Event {

    private final ManagedProcess process;

    /**
     * Constructor.
     *
     * @param source  {@link ExitWaiter}
     * @param process minecraft process
     */
    public ProcessStoppedEvent(Object source, ManagedProcess process) {
        super(source);
        this.process = process;
    }

    public ManagedProcess getProcess() {
        return process;
    }
}
