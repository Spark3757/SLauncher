package ru.spark.slauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.upgrade.UpdateChecker;
import ru.spark.slauncher.util.Analytics;
import ru.spark.slauncher.util.CrashReporter;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.platform.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ru.spark.slauncher.util.Logging.LOG;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class Launcher extends Application {


    public static final CrashReporter CRASH_REPORTER = new CrashReporter();

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(CRASH_REPORTER);

        try {
            LOG.info("*** " + Metadata.TITLE + " ***");
            LOG.info("Operating System: " + System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION);
            LOG.info("Java Version: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
            LOG.info("Java VM Version: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
            LOG.info("Java Home: " + System.getProperty("java.home"));
            LOG.info("Current Directory: " + Paths.get("").toAbsolutePath());
            LOG.info("SLauncher Directory: " + Metadata.SL_DIRECTORY);
            LOG.info("Memory: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB");
            ManagementFactory.getMemoryPoolMXBeans().stream().filter(bean -> bean.getName().equals("Metaspace")).findAny()
                    .ifPresent(bean -> LOG.info("Metaspace: " + bean.getUsage().getUsed() / 1024 / 1024 + "MB"));

            launch(args);
        } catch (Throwable e) { // Fucking JavaFX will suppress the exception and will break our crash reporter.
            CRASH_REPORTER.uncaughtException(Thread.currentThread(), e);
        }
    }

    public static void stopApplication() {
        LOG.info("Stopping application.\n" + StringUtils.getStackTrace(Thread.currentThread().getStackTrace()));

        FXUtils.runInFX(() -> {
            if (Controllers.getStage() == null)
                return;
            Controllers.getStage().close();
            Schedulers.shutdown();
            Controllers.shutdown();
            Platform.exit();
        });
    }

    public static void stopWithoutPlatform() {
        LOG.info("Stopping application without JavaFX Toolkit.\n" + StringUtils.getStackTrace(Thread.currentThread().getStackTrace()));

        FXUtils.runInFX(() -> {
            if (Controllers.getStage() == null)
                return;
            Controllers.getStage().close();
            Schedulers.shutdown();
            Controllers.shutdown();
            Lang.executeDelayed(OperatingSystem::forceGC, TimeUnit.SECONDS, 5, true);
        });
    }

    public static List<File> getCurrentJarFiles() {
        List<File> result = new LinkedList<>();
        if (Launcher.class.getClassLoader() instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) Launcher.class.getClassLoader()).getURLs();
            for (URL u : urls)
                try {
                    File f = new File(u.toURI());
                    if (f.isFile() && (f.getName().endsWith(".exe") || f.getName().endsWith(".jar")))
                        result.add(f);
                } catch (URISyntaxException e) {
                    return null;
                }
        }
        if (result.isEmpty())
            return null;
        else
            return result;
    }

    @Override
    public void start(Stage primaryStage) {

        try {
            try {
                ConfigHolder.init();
            } catch (IOException e) {
                Main.showErrorAndExit(i18n("fatal.config_loading_failure", Paths.get("").toAbsolutePath().normalize()));
            }

            Analytics.init();
            // runLater to ensure ConfigHolder.init() finished initialization
            Platform.runLater(() -> {
                // When launcher visibility is set to "hide and reopen" without Platform.implicitExit = false,
                // Stage.show() cannot work again because JavaFX Toolkit have already shut down.
                Platform.setImplicitExit(false);
                Controllers.initialize(primaryStage);

                UpdateChecker.init();

                primaryStage.show();
            });
            Analytics.recordLauncherStart();
        } catch (Throwable e) {
            CRASH_REPORTER.uncaughtException(Thread.currentThread(), e);
        }

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Controllers.onApplicationStop();
    }

}
