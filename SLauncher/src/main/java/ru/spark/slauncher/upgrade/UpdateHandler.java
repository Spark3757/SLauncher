package ru.spark.slauncher.upgrade;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import javafx.application.Platform;
import ru.spark.slauncher.Main;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.UpgradeDialog;
import ru.spark.slauncher.ui.construct.MessageDialogPane.MessageType;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.io.JarUtils;
import ru.spark.slauncher.util.platform.JavaVersion;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class UpdateHandler {
    private UpdateHandler() {
    }

    /**
     * @return whether to exit
     */
    public static boolean processArguments(String[] args) {
        breakForceUpdateFeature();

        if (isNestedApplication()) {
            // updated from old versions
            try {
                performMigration();
            } catch (IOException e) {
                Logging.LOG.log(Level.WARNING, "Failed to perform migration", e);
                JOptionPane.showMessageDialog(null, i18n("fatal.apply_update_failure", Metadata.PUBLISH_URL) + "\n" + StringUtils.getStackTrace(e), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return true;
        }

        if (args.length == 2 && args[0].equals("--apply-to")) {
            try {
                applyUpdate(Paths.get(args[1]));
            } catch (IOException e) {
                Logging.LOG.log(Level.WARNING, "Failed to apply update", e);
                JOptionPane.showMessageDialog(null, i18n("fatal.apply_update_failure", Metadata.PUBLISH_URL) + "\n" + StringUtils.getStackTrace(e), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return true;
        }

        if (isFirstLaunchAfterUpgrade()) {
            JOptionPane.showMessageDialog(null, i18n("fatal.migration_requires_manual_reboot"), "Info", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }

        return false;
    }

    public static void updateFrom(RemoteVersion version) {
        FXUtils.checkFxUserThread();

        Controllers.dialog(new UpgradeDialog(() -> {
            Path downloaded;
            try {
                downloaded = Files.createTempFile("slauncher-update-", ".jar");
            } catch (IOException e) {
                Logging.LOG.log(Level.WARNING, "Failed to create temp file", e);
                return;
            }

            Task<?> task = new SLauncherDownloadTask(version, downloaded);

            TaskExecutor executor = task.executor();
            Controllers.taskDialog(executor, i18n("message.downloading"));
            Lang.thread(() -> {
                boolean success = executor.test();

                if (success) {
                    try {
                        requestUpdate(downloaded, getCurrentLocation());
                        System.exit(0);
                    } catch (IOException e) {
                        Logging.LOG.log(Level.WARNING, "Failed to update to " + version, e);
                        Platform.runLater(() -> Controllers.dialog(StringUtils.getStackTrace(e), i18n("update.failed"), MessageType.ERROR));
                    }

                } else {
                    Exception e = executor.getException();
                    Logging.LOG.log(Level.WARNING, "Failed to update to " + version, e);
                    Platform.runLater(() -> Controllers.dialog(e.toString(), i18n("update.failed"), MessageType.ERROR));
                }
            });
        }));
    }

    private static void applyUpdate(Path target) throws IOException {
        Logging.LOG.info("Applying update to " + target);

        Path self = getCurrentLocation();
        ExecutableHeaderHelper.copyWithHeader(self, target);

        Optional<Path> newFilename = tryRename(target, Metadata.VERSION);
        if (newFilename.isPresent()) {
            Logging.LOG.info("Move " + target + " to " + newFilename.get());
            try {
                Files.move(target, newFilename.get());
                target = newFilename.get();
            } catch (IOException e) {
                Logging.LOG.log(Level.WARNING, "Failed to move target", e);
            }
        }

        startJava(target);
    }

    private static void requestUpdate(Path updateTo, Path self) throws IOException {
        startJava(updateTo, "--apply-to", self.toString());
    }

    private static void startJava(Path jar, String... appArgs) throws IOException {
        List<String> commandline = new ArrayList<>();
        commandline.add(JavaVersion.fromCurrentEnvironment().getBinary().toString());
        commandline.add("-jar");
        commandline.add(jar.toAbsolutePath().toString());
        commandline.addAll(Arrays.asList(appArgs));
        Logging.LOG.info("Starting process: " + commandline);
        new ProcessBuilder(commandline)
                .directory(Paths.get("").toAbsolutePath().toFile())
                .inheritIO()
                .start();
    }

    private static Optional<Path> tryRename(Path path, String newVersion) {
        String filename = path.getFileName().toString();
        Matcher matcher = Pattern.compile("^(?<prefix>[hH][mM][cC][lL][.-])(?<version>\\d+(?:\\.\\d+)*)(?<suffix>\\.[^.]+)$").matcher(filename);
        if (matcher.find()) {
            String newFilename = matcher.group("prefix") + newVersion + matcher.group("suffix");
            if (!newFilename.equals(filename)) {
                return Optional.of(path.resolveSibling(newFilename));
            }
        }
        return Optional.empty();
    }

    private static Path getCurrentLocation() throws IOException {
        return JarUtils.thisJar().orElseThrow(() -> new IOException("Failed to find current SLauncher location"));
    }

    // ==== support for old versions ===
    private static void performMigration() throws IOException {
        Logging.LOG.info("Migrating from old versions");

        Path location = getParentApplicationLocation()
                .orElseThrow(() -> new IOException("Failed to get parent application location"));

        requestUpdate(getCurrentLocation(), location);
    }

    /**
     * This method must be called from the main thread.
     */
    private static boolean isNestedApplication() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stacktrace.length; i++) {
            StackTraceElement element = stacktrace[i];
            if (Main.class.getName().equals(element.getClassName())) {
                // we've reached the main method
                return i + 1 != stacktrace.length;
            }
        }
        return false;
    }

    private static Optional<Path> getParentApplicationLocation() {
        String command = System.getProperty("sun.java.command");
        if (command != null) {
            Path path = Paths.get(command);
            if (Files.isRegularFile(path)) {
                return Optional.of(path.toAbsolutePath());
            }
        }
        return Optional.empty();
    }

    private static boolean isFirstLaunchAfterUpgrade() {
        Optional<Path> currentPath = JarUtils.thisJar();
        if (currentPath.isPresent()) {
            Path updated = Metadata.SL_DIRECTORY.resolve("SLauncher-" + Metadata.VERSION + ".jar");
            if (currentPath.get().toAbsolutePath().equals(updated.toAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    private static void breakForceUpdateFeature() {
        Path slauncherVersionJson = Metadata.SL_DIRECTORY.resolve("slauncherver.json");
        if (Files.isRegularFile(slauncherVersionJson)) {
            try {
                Map<?, ?> content = new Gson().fromJson(FileUtils.readText(slauncherVersionJson), Map.class);
                Object ver = content.get("ver");
                if (ver instanceof String && ((String) ver).startsWith("3.")) {
                    Files.delete(slauncherVersionJson);
                    Logging.LOG.info("Successfully broke the force update feature");
                }
            } catch (IOException e) {
                Logging.LOG.log(Level.WARNING, "Failed to break the force update feature", e);
            } catch (JsonParseException e) {
                slauncherVersionJson.toFile().delete();
            }
        }
    }
    // ====
}
