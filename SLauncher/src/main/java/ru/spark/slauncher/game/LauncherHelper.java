package ru.spark.slauncher.game;

import javafx.application.Platform;
import javafx.stage.Stage;
import ru.spark.slauncher.Launcher;
import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.CredentialExpiredException;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.MaintainTask;
import ru.spark.slauncher.download.game.LibraryDownloadException;
import ru.spark.slauncher.launch.NotDecompressingNativesException;
import ru.spark.slauncher.launch.PermissionException;
import ru.spark.slauncher.launch.ProcessCreationException;
import ru.spark.slauncher.launch.ProcessListener;
import ru.spark.slauncher.mod.CurseCompletionException;
import ru.spark.slauncher.mod.CurseCompletionTask;
import ru.spark.slauncher.mod.ModpackConfiguration;
import ru.spark.slauncher.setting.LauncherVisibility;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.VersionSetting;
import ru.spark.slauncher.task.*;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.DialogController;
import ru.spark.slauncher.ui.LogWindow;
import ru.spark.slauncher.ui.construct.DialogCloseEvent;
import ru.spark.slauncher.ui.construct.MessageDialogPane;
import ru.spark.slauncher.ui.construct.TaskExecutorDialogPane;
import ru.spark.slauncher.util.*;
import ru.spark.slauncher.util.function.ExceptionalSupplier;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;
import ru.spark.slauncher.util.platform.CommandBuilder;
import ru.spark.slauncher.util.platform.JavaVersion;
import ru.spark.slauncher.util.platform.ManagedProcess;
import ru.spark.slauncher.util.platform.OperatingSystem;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class LauncherHelper {

    public static final Queue<ManagedProcess> PROCESSES = new ConcurrentLinkedQueue<>();
    private final Profile profile;
    private final Account account;
    private final String selectedVersion;
    private final VersionSetting setting;
    private final TaskExecutorDialogPane launchingStepsPane = new TaskExecutorDialogPane(it -> {
    });
    private File scriptFile;
    private LauncherVisibility launcherVisibility;
    private boolean showLogs;

    public LauncherHelper(Profile profile, Account account, String selectedVersion) {
        this.profile = Objects.requireNonNull(profile);
        this.account = Objects.requireNonNull(account);
        this.selectedVersion = Objects.requireNonNull(selectedVersion);
        this.setting = profile.getVersionSetting(selectedVersion);
        this.launcherVisibility = setting.getLauncherVisibility();
        this.showLogs = setting.isShowLogs();
    }

    private static void checkGameState(Profile profile, VersionSetting setting, Version version, Runnable onAccept) throws InterruptedException {
        if (setting.isNotCheckJVM()) {
            onAccept.run();
            return;
        }

        boolean flag = false;
        boolean java8required = false;
        boolean newJavaRequired = false;

        // Without onAccept called, the launching operation will be terminated.

        VersionNumber gameVersion = VersionNumber.asVersion(GameVersion.minecraftVersion(profile.getRepository().getVersionJar(version)).orElse("Unknown"));
        JavaVersion java = setting.getJavaVersion();
        if (java == null) {
            Controllers.dialog(i18n("launch.wrong_javadir"), i18n("message.warning"), MessageDialogPane.MessageType.WARNING, onAccept);
            setting.setJava(null);
            setting.setDefaultJavaPath(null);
            java = JavaVersion.fromCurrentEnvironment();
            flag = true;
        }

        // Game later than 1.7.2 accepts Java 8.
        if (!flag && java.getParsedVersion() < JavaVersion.JAVA_8 && gameVersion.compareTo(VersionNumber.asVersion("1.7.2")) > 0) {
            Optional<JavaVersion> java8 = JavaVersion.getJavas().stream()
                    .filter(javaVersion -> javaVersion.getParsedVersion() >= JavaVersion.JAVA_8)
                    .max(Comparator.comparing(JavaVersion::getVersionNumber));
            if (java8.isPresent()) {
                newJavaRequired = true;
                setting.setJavaVersion(java8.get());
            } else {
                if (gameVersion.compareTo(VersionNumber.asVersion("1.13")) >= 0) {
                    // Minecraft 1.13 and later versions only support Java 8 or later.
                    // Terminate launching operation.
                    Controllers.dialog(i18n("launch.advice.java8_1_13"), i18n("message.error"), MessageDialogPane.MessageType.ERROR, null);
                } else {
                    // Most mods require Java 8 or later version.
                    Controllers.dialog(i18n("launch.advice.newer_java"), i18n("message.warning"), MessageDialogPane.MessageType.WARNING, onAccept);
                }
                flag = true;
            }
        }

        // LaunchWrapper 1.12 will crash because of assuming the system class loader is an instance of URLClassLoader.
        if (!flag && java.getParsedVersion() >= JavaVersion.JAVA_9
                && version.getMainClass().contains("launchwrapper")
                && version.getLibraries().stream()
                .filter(library -> "launchwrapper".equals(library.getArtifactId()))
                .anyMatch(library -> VersionNumber.asVersion(library.getVersion()).compareTo(VersionNumber.asVersion("1.13")) < 0)) {
            Optional<JavaVersion> java8 = JavaVersion.getJavas().stream().filter(javaVersion -> javaVersion.getParsedVersion() == JavaVersion.JAVA_8).findAny();
            if (java8.isPresent()) {
                java8required = true;
                setting.setJavaVersion(java8.get());
                Controllers.dialog(i18n("launch.advice.java9") + "\n" + i18n("launch.advice.corrected"), i18n("message.info"), MessageDialogPane.MessageType.INFORMATION, onAccept);
                flag = true;
            } else {
                Controllers.dialog(i18n("launch.advice.java9") + "\n" + i18n("launch.advice.uncorrected"), i18n("message.error"), MessageDialogPane.MessageType.ERROR, null);
                flag = true;
            }
        }

        // Minecraft 1.13 may crash when generating world on Java 8 earlier than 1.8.0_51
        VersionNumber JAVA_8 = VersionNumber.asVersion("1.8.0_51");
        if (!flag && gameVersion.compareTo(VersionNumber.asVersion("1.13")) >= 0 && java.getParsedVersion() == JavaVersion.JAVA_8 && java.getVersionNumber().compareTo(JAVA_8) < 0) {
            Optional<JavaVersion> java8 = JavaVersion.getJavas().stream()
                    .filter(javaVersion -> javaVersion.getVersionNumber().compareTo(JAVA_8) >= 0)
                    .max(Comparator.comparing(JavaVersion::getVersionNumber));
            if (java8.isPresent()) {
                newJavaRequired = true;
                setting.setJavaVersion(java8.get());
            } else {
                Controllers.dialog(i18n("launch.advice.java8_51_1_13"), i18n("message.warning"), MessageDialogPane.MessageType.WARNING, onAccept);
                flag = true;
            }
        }

        if (!flag && java.getPlatform() == ru.spark.slauncher.util.platform.Platform.BIT_32 &&
                ru.spark.slauncher.util.platform.Platform.IS_64_BIT) {
            final JavaVersion java32 = java;

            // First find if same java version but whose platform is 64-bit installed.
            Optional<JavaVersion> java64 = JavaVersion.getJavas().stream()
                    .filter(javaVersion -> javaVersion.getPlatform() == ru.spark.slauncher.util.platform.Platform.PLATFORM)
                    .filter(javaVersion -> javaVersion.getParsedVersion() == java32.getParsedVersion())
                    .max(Comparator.comparing(JavaVersion::getVersionNumber));

            if (!java64.isPresent()) {
                final boolean java8requiredFinal = java8required, newJavaRequiredFinal = newJavaRequired;

                // Then find if other java version which satisfies requirements installed.
                java64 = JavaVersion.getJavas().stream()
                        .filter(javaVersion -> javaVersion.getPlatform() == ru.spark.slauncher.util.platform.Platform.PLATFORM)
                        .filter(javaVersion -> {
                            if (java8requiredFinal) return javaVersion.getParsedVersion() == JavaVersion.JAVA_8;
                            if (newJavaRequiredFinal) return javaVersion.getParsedVersion() >= JavaVersion.JAVA_8;
                            return true;
                        })
                        .max(Comparator.comparing(JavaVersion::getVersionNumber));
            }

            if (java64.isPresent()) {
                setting.setJavaVersion(java64.get());
            } else {
                Controllers.dialog(i18n("launch.advice.different_platform"), i18n("message.error"), MessageDialogPane.MessageType.ERROR, onAccept);
                flag = true;
            }
        }

        // 32-bit JVM cannot make use of too much memory.
        if (!flag && java.getPlatform() == ru.spark.slauncher.util.platform.Platform.BIT_32 &&
                setting.getMaxMemory() > 1.5 * 1024) {
            // 1.5 * 1024 is an inaccurate number.
            // Actual memory limit depends on operating system and memory.
            Controllers.dialog(i18n("launch.advice.too_large_memory_for_32bit"), i18n("message.error"), MessageDialogPane.MessageType.ERROR, onAccept);
            flag = true;
        }

        // Cannot allocate too much memory exceeding free space.
        if (!flag && OperatingSystem.TOTAL_MEMORY > 0 && OperatingSystem.TOTAL_MEMORY < setting.getMaxMemory()) {
            Controllers.dialog(i18n("launch.advice.not_enough_space", OperatingSystem.TOTAL_MEMORY), i18n("message.error"), MessageDialogPane.MessageType.ERROR, onAccept);
            flag = true;
        }

        // Forge 2760~2773 will crash game with LiteLoader.
        if (!flag) {
            boolean hasForge2760 = version.getLibraries().stream().filter(it -> it.is("net.minecraftforge", "forge"))
                    .anyMatch(it ->
                            VersionNumber.VERSION_COMPARATOR.compare("1.12.2-14.23.5.2760", it.getVersion()) <= 0 &&
                                    VersionNumber.VERSION_COMPARATOR.compare(it.getVersion(), "1.12.2-14.23.5.2773") < 0);
            boolean hasLiteLoader = version.getLibraries().stream().anyMatch(it -> it.is("com.mumfrey", "liteloader"));
            if (hasForge2760 && hasLiteLoader && gameVersion.compareTo(VersionNumber.asVersion("1.12.2")) == 0) {
                Controllers.dialog(i18n("launch.advice.forge2760_liteloader"), i18n("message.error"), MessageDialogPane.MessageType.ERROR, onAccept);
                flag = true;
            }
        }

        if (!flag)
            onAccept.run();
    }

    public static void stopManagedProcesses() {
        while (!PROCESSES.isEmpty())
            Optional.ofNullable(PROCESSES.poll()).ifPresent(ManagedProcess::stop);
    }

    public void setTestMode() {
        launcherVisibility = LauncherVisibility.KEEP;
        showLogs = true;
    }

    public void launch() {
        Logging.LOG.info("Launching game version: " + selectedVersion);

        GameRepository repository = profile.getRepository();
        Version version = repository.getResolvedVersion(selectedVersion);
        Analytics.recordMinecraftVersionLaunch(version);
        Platform.runLater(() -> {
            try {
                checkGameState(profile, setting, version, () -> {
                    Controllers.dialog(launchingStepsPane);
                    Schedulers.newThread().schedule(this::launch0);
                });
            } catch (InterruptedException ignore) {
            }
        });
    }

    public void makeLaunchScript(File scriptFile) {
        this.scriptFile = Objects.requireNonNull(scriptFile);

        launch();
    }

    private void launch0() {
        SLauncherGameRepository repository = profile.getRepository();
        DefaultDependencyManager dependencyManager = profile.getDependency();
        Version version = MaintainTask.maintain(repository.getResolvedVersion(selectedVersion));
        Optional<String> gameVersion = GameVersion.minecraftVersion(repository.getVersionJar(version));

        TaskExecutor executor = Task.of(Schedulers.javafx(), () -> emitStatus(LoadingState.DEPENDENCIES))
                .then(Task.of(() -> {
                    try {
                        File branderFile = Paths.get(OperatingSystem.getWorkingDirectory("SLauncher").toAbsolutePath() + "/brander.jar").toFile();
                        if (!branderFile.exists()) {
                            FileDownloadTask task = new FileDownloadTask(new URL("http://dl.slauncher.ru/brander.jar"), branderFile);
                            task.execute();
                        }

                    } catch (Exception e) {
                    }
                }))
                .then(() -> {
                    if (setting.isNotCheckGame())
                        return null;
                    else
                        return dependencyManager.checkGameCompletionAsync(version);
                })
                .then(Task.of(Schedulers.javafx(), () -> emitStatus(LoadingState.MODS)))
                .then(() -> {
                    try {
                        ModpackConfiguration<?> configuration = ModpackHelper.readModpackConfiguration(repository.getModpackConfiguration(selectedVersion));
                        if ("Curse".equals(configuration.getType()))
                            return new CurseCompletionTask(dependencyManager, selectedVersion);
                        else
                            return null;
                    } catch (IOException e) {
                        return null;
                    }
                })
                .then(Task.of(Schedulers.javafx(), () -> emitStatus(LoadingState.LOGGING_IN)))
                .thenCompose(() -> Task.ofResult(i18n("account.methods"), () -> {
                    try {
                        return account.logIn();
                    } catch (CredentialExpiredException e) {
                        Logging.LOG.info("Credential has expired: " + e);
                        return DialogController.logIn(account);
                    } catch (AuthenticationException e) {
                        Logging.LOG.warning("Authentication failed, try playing offline: " + e);
                        return account.playOffline().orElseThrow(() -> e);
                    }
                }))
                .thenApply(Schedulers.javafx(), authInfo -> {
                    emitStatus(LoadingState.LAUNCHING);
                    return authInfo;
                })
                .thenApply(authInfo -> new SLauncherGameLauncher(
                        repository,
                        selectedVersion,
                        authInfo,
                        setting.toLaunchOptions(profile.getGameDir()),
                        launcherVisibility == LauncherVisibility.CLOSE
                                ? null // Unnecessary to start listening to game process output when close launcher immediately after game launched.
                                : new SLauncherProcessListener(authInfo, setting, gameVersion.isPresent())
                ))
                .thenCompose(launcher -> { // launcher is prev task's result
                    if (scriptFile == null) {
                        return new LaunchTask<>(launcher::launch).setName(i18n("version.launch"));
                    } else {
                        return new LaunchTask<ManagedProcess>(() -> {
                            launcher.makeLaunchScript(scriptFile);
                            return null;
                        }).setName(i18n("version.launch_script"));
                    }
                })
                .thenAccept(process -> { // process is LaunchTask's result
                    if (scriptFile == null) {
                        PROCESSES.add(process);
                        if (launcherVisibility == LauncherVisibility.CLOSE)
                            Launcher.stopApplication();
                        else
                            launchingStepsPane.setCancel(it -> {
                                process.stop();
                                it.fireEvent(new DialogCloseEvent());
                            });
                    } else {
                        Platform.runLater(() -> {
                            launchingStepsPane.fireEvent(new DialogCloseEvent());
                            Controllers.dialog(i18n("version.launch_script.success", scriptFile.getAbsolutePath()));
                        });
                    }
                })
                .executor();

        launchingStepsPane.setExecutor(executor, false);
        executor.addTaskListener(new TaskListener() {
            final AtomicInteger finished = new AtomicInteger(0);

            @Override
            public void onFinished(Task task) {
                finished.incrementAndGet();
                int runningTasks = executor.getRunningTasks();
                Platform.runLater(() -> launchingStepsPane.setProgress(1.0 * finished.get() / runningTasks));
            }

            @Override
            public void onStop(boolean success, TaskExecutor executor) {
                if (!success && !Controllers.isStopped()) {
                    Platform.runLater(() -> {
                        // Check if the application has stopped
                        // because onStop will be invoked if tasks fail when the executor service shut down.
                        if (!Controllers.isStopped()) {
                            launchingStepsPane.fireEvent(new DialogCloseEvent());
                            Exception ex = executor.getLastException();
                            if (ex != null) {
                                String message;
                                if (ex instanceof CurseCompletionException) {
                                    if (ex.getCause() instanceof FileNotFoundException)
                                        message = i18n("modpack.type.curse.not_found");
                                    else
                                        message = i18n("modpack.type.curse.error");
                                } else if (ex instanceof PermissionException) {
                                    message = i18n("launch.failed.executable_permission");
                                } else if (ex instanceof ProcessCreationException) {
                                    message = i18n("launch.failed.creating_process") + ex.getLocalizedMessage();
                                } else if (ex instanceof NotDecompressingNativesException) {
                                    message = i18n("launch.failed.decompressing_natives") + ex.getLocalizedMessage();
                                } else if (ex instanceof LibraryDownloadException) {
                                    message = i18n("launch.failed.download_library", ((LibraryDownloadException) ex).getLibrary().getName()) + "\n" + StringUtils.getStackTrace(ex.getCause());
                                } else {
                                    message = StringUtils.getStackTrace(ex);
                                }
                                Controllers.dialog(message,
                                        scriptFile == null ? i18n("launch.failed") : i18n("version.launch_script.failed"),
                                        MessageDialogPane.MessageType.ERROR);
                            }
                        }
                    });
                }
                launchingStepsPane.setExecutor(null);
            }
        });

        executor.start();
    }

    public void emitStatus(LoadingState state) {
        if (state == LoadingState.DONE) {
            launchingStepsPane.fireEvent(new DialogCloseEvent());
        }

        launchingStepsPane.setTitle(state.getLocalizedMessage());
        launchingStepsPane.setSubtitle((state.ordinal() + 1) + " / " + LoadingState.values().length);
    }

    private void checkExit() {
        switch (launcherVisibility) {
            case HIDE_AND_REOPEN:
                Platform.runLater(() -> {
                    Optional.ofNullable(Controllers.getStage())
                            .ifPresent(Stage::show);
                });
                break;
            case KEEP:
                // No operations here
                break;
            case CLOSE:
                throw new Error("Never get to here");
            case HIDE:
                Platform.runLater(() -> {
                    // Shut down the platform when user closed log window.
                    Platform.setImplicitExit(true);
                    // If we use Launcher.stop(), log window will be halt immediately.
                    Launcher.stopWithoutPlatform();
                });
                break;
        }
    }

    private static class LaunchTask<T> extends TaskResult<T> {
        private final ExceptionalSupplier<T, Exception> supplier;

        public LaunchTask(ExceptionalSupplier<T, Exception> supplier) {
            this.supplier = supplier;
        }

        @Override
        public void execute() throws Exception {
            setResult(supplier.get());
        }
    }

    /**
     * The managed process listener.
     * Guarantee that one [JavaProcess], one [SLauncherProcessListener].
     * Because every time we launched a game, we generates a new [SLauncherProcessListener]
     */
    class SLauncherProcessListener implements ProcessListener {

        private final VersionSetting setting;
        private final Map<String, String> forbiddenTokens;
        private final boolean detectWindow;
        private final LinkedList<Pair<String, Log4jLevel>> logs;
        private final CountDownLatch latch = new CountDownLatch(1);
        private ManagedProcess process;
        private boolean lwjgl;
        private LogWindow logWindow;

        public SLauncherProcessListener(AuthInfo authInfo, VersionSetting setting, boolean detectWindow) {
            this.setting = setting;
            this.detectWindow = detectWindow;

            if (authInfo == null)
                forbiddenTokens = Collections.emptyMap();
            else
                forbiddenTokens = Lang.mapOf(
                        Pair.pair(authInfo.getAccessToken(), "<access token>"),
                        Pair.pair(UUIDTypeAdapter.fromUUID(authInfo.getUUID()), "<uuid>"),
                        Pair.pair(authInfo.getUsername(), "<player>")
                );

            logs = new LinkedList<>();
        }

        @Override
        public void setProcess(ManagedProcess process) {
            this.process = process;

            if (showLogs)
                Platform.runLater(() -> {
                    logWindow = new LogWindow();
                    logWindow.show();
                    latch.countDown();
                });
        }

        private void finishLaunch() {
            switch (launcherVisibility) {
                case HIDE_AND_REOPEN:
                    Platform.runLater(() -> {
                        // If application was stopped and execution services did not finish termination,
                        // these codes will be executed.
                        if (Controllers.getStage() != null) {
                            Controllers.getStage().hide();
                            emitStatus(LoadingState.DONE);
                        }
                    });
                    break;
                case CLOSE:
                    // Never come to here.
                    break;
                case KEEP:
                    Platform.runLater(() -> {
                        emitStatus(LoadingState.DONE);
                    });
                    break;
                case HIDE:
                    Platform.runLater(() -> {
                        // If application was stopped and execution services did not finish termination,
                        // these codes will be executed.
                        if (Controllers.getStage() != null) {
                            Controllers.getStage().close();
                            emitStatus(LoadingState.DONE);
                        }
                    });
                    break;
            }
        }

        @Override
        public synchronized void onLog(String log, Log4jLevel level) {
            String newLog = log;
            for (Map.Entry<String, String> entry : forbiddenTokens.entrySet())
                newLog = newLog.replace(entry.getKey(), entry.getValue());

            if (level.lessOrEqual(Log4jLevel.ERROR))
                System.err.print(log);
            else
                System.out.print(log);

            logs.add(Pair.pair(log, level));
            if (logs.size() > config().getLogLines())
                logs.removeFirst();

            if (showLogs) {
                try {
                    latch.await();
                    logWindow.waitForLoaded();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                Platform.runLater(() -> logWindow.logLine(log, level));
            }

            if (!lwjgl && (log.contains("LWJGL Version: ") || !detectWindow)) {
                lwjgl = true;
                finishLaunch();
            }
        }

        @Override
        public void onExit(int exitCode, ExitType exitType) {
            if (exitType == ExitType.INTERRUPTED)
                return;

            // Game crashed before opening the game window.
            if (!lwjgl) finishLaunch();

            if (exitType != ExitType.NORMAL && logWindow == null)
                Platform.runLater(() -> {
                    logWindow = new LogWindow();

                    switch (exitType) {
                        case JVM_ERROR:
                            logWindow.setTitle(i18n("launch.failed.cannot_create_jvm"));
                            break;
                        case APPLICATION_ERROR:
                            logWindow.setTitle(i18n("launch.failed.exited_abnormally"));
                            break;
                    }

                    logWindow.show();
                    logWindow.onDone.register(() -> {
                        logWindow.logLine("Command: " + new CommandBuilder().addAll(process.getCommands()).toString(), Log4jLevel.INFO);
                        for (Map.Entry<String, Log4jLevel> entry : logs)
                            logWindow.logLine(entry.getKey(), entry.getValue());
                    });
                });

            checkExit();
        }

    }
}
