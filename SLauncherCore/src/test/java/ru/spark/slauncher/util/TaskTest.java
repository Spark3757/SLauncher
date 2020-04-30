package ru.spark.slauncher.util;

import javafx.embed.swing.JFXPanel;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.util.platform.JavaVersion;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TaskTest {
    /**
     * TaskExecutor will not catch error and will be thrown to global handler.
     */
    @Test
    public void expectErrorUncaught() {
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> throwable.set(e));
        Assert.assertFalse(Task.composeAsync(() -> Task.allOf(
                Task.allOf(Task.runAsync(() -> {
                    throw new Error();
                }))
        )).whenComplete(Assert::assertNull).test());

        Assert.assertTrue("Error has not been thrown to uncaught exception handler", throwable.get() instanceof Error);
    }

    /**
     *
     */
    @Test
    public void testWhenComplete() {
        boolean result = Task.supplyAsync(() -> {
            throw new IllegalStateException();
        }).whenComplete(exception -> {
            Assert.assertTrue(exception instanceof IllegalStateException);
        }).test();

        Assert.assertFalse("Task should fail at this case", result);
    }

    @Test
    public void testWithCompose() {
        AtomicBoolean bool = new AtomicBoolean();
        boolean success = Task.supplyAsync(() -> {
            throw new IllegalStateException();
        }).withRunAsync(() -> {
            bool.set(true);
        }).test();

        Assert.assertTrue("Task should success because withRunAsync will ignore previous exception", success);
        Assert.assertTrue("withRunAsync should be executed", bool.get());
    }

    public void testThenAccept() {
        new JFXPanel(); // init JavaFX Toolkit
        AtomicBoolean flag = new AtomicBoolean();
        boolean result = Task.supplyAsync(JavaVersion::fromCurrentEnvironment)
                .thenAcceptAsync(Schedulers.javafx(), javaVersion -> {
                    flag.set(true);
                    Assert.assertEquals(javaVersion, JavaVersion.fromCurrentEnvironment());
                })
                .test();

        Assert.assertTrue("Task does not succeed", result);
        Assert.assertTrue("ThenAccept has not been executed", flag.get());
    }

    @Test
    public void testCancellation() throws InterruptedException {
        AtomicBoolean flag = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        Task<?> task = Task.runAsync(() -> {
            latch.countDown();
            Thread.sleep(200);
            // default executor cannot interrupt task.
            flag.getAndSet(true);
        }).thenRunAsync(() -> {
            System.out.println("No way!");
            Thread.sleep(200);
            Assert.fail("Cannot reach here");
        });
        TaskExecutor executor = task.executor();
        Lang.thread(() -> {
            try {
                latch.await();
                System.out.println("Main thread start waiting");
                Thread.sleep(100);
                System.out.println("Cancel");
                executor.cancel();
            } catch (InterruptedException e) {
                Assume.assumeNoException(e);
            }
        });
        System.out.println("Start");
        Assert.assertFalse("Task should fail because we have cancelled it", executor.test());
        Thread.sleep(3000);
        Assert.assertNull("CancellationException should not be recorded.", executor.getException());
        Assert.assertNull("CancellationException should not be recorded.", task.getException());
        Assert.assertTrue("Thread.sleep cannot be interrupted", flag.get());
    }

    @Test
    public void testCompletableFutureCancellation() throws Throwable {
        AtomicBoolean flag = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<?> task = CompletableFuture.runAsync(() -> {
            latch.countDown();
            System.out.println("Sleep");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // default executor cannot interrupt task.
            flag.getAndSet(true);
            System.out.println("End");
        }).thenComposeAsync(non -> {
            System.out.println("compose");
            return CompletableFuture.allOf(CompletableFuture.runAsync(() -> {
                System.out.println("No way!");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Assert.fail("Cannot reach here");
            }));
        });
        Lang.thread(() -> {
            try {
                latch.await();
                System.out.println("Main thread start waiting");
                Thread.sleep(100);
                System.out.println("Cancel");
                task.cancel(true);
            } catch (InterruptedException e) {
                Assume.assumeNoException(e);
            }
        });
        System.out.println("Start");
        try {
            task.get();
        } catch (CancellationException e) {
            System.out.println("Successfully cancelled");
        }
        //Assert.assertFalse("Task should fail because we have cancelled it", );
        Thread.sleep(4000);
        //Assert.assertNull("CancellationException should not be recorded.", executor.getException());
        //Assert.assertTrue("Thread.sleep cannot be interrupted", flag.get());
    }

    public void testRejectedExecutionException() {
        Schedulers.defaultScheduler();
        Schedulers.shutdown();

        Task<?> task = Task.runAsync(() -> {
            Thread.sleep(1000);
        });

        boolean result = task.test();

        Assert.assertFalse("Task should fail since ExecutorService is shut down and RejectedExecutionException should be thrown", result);
        Assert.assertTrue("RejectedExecutionException should be recorded", task.getException() instanceof RejectedExecutionException);
    }
}