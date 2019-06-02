package ru.spark.slauncher.launch;

import ru.spark.slauncher.util.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Pump the given input stream.
 *
 * @author Spark1337
 */
final class StreamPump implements Runnable {

    private final InputStream inputStream;
    private final Consumer<String> callback;

    public StreamPump(InputStream inputStream) {
        this(inputStream, s -> {
        });
    }

    public StreamPump(InputStream inputStream, Consumer<String> callback) {
        this.inputStream = inputStream;
        this.callback = callback;
    }

    @Override
    public void run() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                    break;
                }

                callback.accept(line);
            }
        } catch (IOException e) {
            Logging.LOG.log(Level.SEVERE, "An error occurred when reading stream", e);
        }
    }

}
