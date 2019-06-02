package ru.spark.slauncher.task;

import ru.spark.slauncher.util.function.ExceptionalRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Spark1337
 */
class SchedulerExecutorService extends Scheduler {

    private final ExecutorService executorService;

    public SchedulerExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public Future<?> schedule(ExceptionalRunnable<?> block) {
        if (executorService.isShutdown() || executorService.isTerminated())
            return Schedulers.NONE.schedule(block);
        else
            return executorService.submit(block.toCallable());
    }

}
