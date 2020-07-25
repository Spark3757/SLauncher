package ru.spark.slauncher.util.platform;

import ru.spark.slauncher.util.Lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public final class SystemUtils {
    private SystemUtils() {
    }

    public static final boolean JRE_CAPABILITY_PACK200 = Lang.test(() -> Class.forName("java.util.jar.Pack200"));

    public static int callExternalProcess(String... command) throws IOException, InterruptedException {
        return callExternalProcess(Arrays.asList(command));
    }

    public static int callExternalProcess(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                System.out.println(line);
            }
        }
        return process.waitFor();
    }
}
