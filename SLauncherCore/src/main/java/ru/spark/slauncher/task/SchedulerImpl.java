package ru.spark.slauncher.task;

import ru.spark.slauncher.util.function.ExceptionalRunnable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author Spark1337
 */
class SchedulerImpl extends Scheduler {

    private final Consumer<Runnable> executor;

    public SchedulerImpl(Consumer<Runnable> executor) {
        this.executor = executor;
    }

    @Override
    public Future<?> schedule(ExceptionalRunnable<?> block) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> wrapper = new AtomicReference<>();

        executor.accept(() -> {
            try {
                block.run();
            } catch (Exception e) {
                wrapper.set(e);
            } finally {
                latch.countDown();
            }
            Thread.interrupted(); // clear the `interrupted` flag to prevent from interrupting EventDispatch thread.
        });

        return new Future<Void>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return latch.getCount() == 0;
            }

            private Void getImpl() throws ExecutionException {
                Exception e = wrapper.get();
                if (e != null)
                    throw new ExecutionException(e);
                return null;
            }

            @Override
            public Void get() throws InterruptedException, ExecutionException {
                latch.await();
                return getImpl();
            }

            @Override
            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if (!latch.await(timeout, unit))
                    throw new TimeoutException();
                return getImpl();
            }
        };
    }

}
