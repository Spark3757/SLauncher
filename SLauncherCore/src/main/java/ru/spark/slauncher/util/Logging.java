package ru.spark.slauncher.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * @author Spark1337
 */
public final class Logging {

    public static final Logger LOG = Logger.getLogger("SLauncher");
    private static ByteArrayOutputStream storedLogs = new ByteArrayOutputStream();

    public static void start(Path logFolder) {
        LOG.setLevel(Level.ALL);
        LOG.setUseParentHandlers(false);

        try {
            Files.createDirectories(logFolder);
            FileHandler fileHandler = new FileHandler(logFolder.resolve("slauncher.log").toAbsolutePath().toString());
            fileHandler.setLevel(Level.FINEST);
            fileHandler.setFormatter(DefaultFormatter.INSTANCE);
            LOG.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Unable to create slauncher.log, " + e.getMessage());
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(DefaultFormatter.INSTANCE);
        consoleHandler.setLevel(Level.FINER);
        LOG.addHandler(consoleHandler);

        StreamHandler streamHandler = new StreamHandler(storedLogs, DefaultFormatter.INSTANCE) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        streamHandler.setLevel(Level.ALL);
        LOG.addHandler(streamHandler);
    }

    public static byte[] getRawLogs() {
        return storedLogs.toByteArray();
    }

    public static String getLogs() {
        return storedLogs.toString();
    }

    private static final class DefaultFormatter extends Formatter {

        static final DefaultFormatter INSTANCE = new DefaultFormatter();
        private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            String date = format.format(new Date(record.getMillis()));
            String log = String.format("[%s] [%s.%s/%s] %s%n",
                    date, record.getSourceClassName(), record.getSourceMethodName(),
                    record.getLevel().getName(), record.getMessage()
            );
            ByteArrayOutputStream builder = new ByteArrayOutputStream();
            if (record.getThrown() != null)
                try (PrintWriter writer = new PrintWriter(builder)) {
                    record.getThrown().printStackTrace(writer);
                }
            return log + builder.toString();
        }

    }
}
