package ru.spark.slauncher.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import ru.spark.slauncher.Launcher;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.setting.EnumCommonDirectory;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.ui.account.AuthlibInjectorServersPage;
import ru.spark.slauncher.ui.animation.ContainerAnimations;
import ru.spark.slauncher.ui.construct.InputDialogPane;
import ru.spark.slauncher.ui.construct.MessageDialogPane;
import ru.spark.slauncher.ui.construct.PromptDialogPane;
import ru.spark.slauncher.ui.construct.TaskExecutorDialogPane;
import ru.spark.slauncher.ui.decorator.DecoratorController;
import ru.spark.slauncher.ui.main.RootPage;
import ru.spark.slauncher.ui.versions.VersionPage;
import ru.spark.slauncher.util.FutureCallback;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.platform.JavaVersion;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static ru.spark.slauncher.ui.FXUtils.newImage;

public final class Controllers {
    private static DoubleProperty stageWidth = new SimpleDoubleProperty();
    private static DoubleProperty stageHeight = new SimpleDoubleProperty();

    private static Scene scene;
    private static Stage stage;
    private static VersionPage versionPage = null;
    private static AuthlibInjectorServersPage serversPage = null;
    private static RootPage rootPage;
    private static DecoratorController decorator;

    public static Scene getScene() {
        return scene;
    }

    public static Stage getStage() {
        return stage;
    }

    // FXThread
    public static VersionPage getVersionPage() {
        if (versionPage == null)
            versionPage = new VersionPage();
        return versionPage;
    }

    // FXThread
    public static RootPage getRootPage() {
        if (rootPage == null)
            rootPage = new RootPage();
        return rootPage;
    }

    // FXThread
    public static AuthlibInjectorServersPage getServersPage() {
        if (serversPage == null)
            serversPage = new AuthlibInjectorServersPage();
        return serversPage;
    }

    // FXThread
    public static DecoratorController getDecorator() {
        return decorator;
    }

    public static void onApplicationStop() {
        ConfigHolder.config().setHeight(stageHeight.get());
        ConfigHolder.config().setWidth(stageWidth.get());
    }

    public static void initialize(Stage stage) {
        Logging.LOG.info("Start initializing application");

        Controllers.stage = stage;

        stage.setHeight(ConfigHolder.config().getHeight());
        stageHeight.bind(stage.heightProperty());
        stage.setWidth(ConfigHolder.config().getWidth());
        stageWidth.bind(stage.widthProperty());

        stage.setOnCloseRequest(e -> Launcher.stopApplication());

        decorator = new DecoratorController(stage, getRootPage());

        if (ConfigHolder.config().getCommonDirType() == EnumCommonDirectory.CUSTOM &&
                !FileUtils.canCreateDirectory(ConfigHolder.config().getCommonDirectory())) {
            ConfigHolder.config().setCommonDirType(EnumCommonDirectory.DEFAULT);
            dialog(I18n.i18n("launcher.cache_directory.invalid"));
        }

        Task.runAsync(JavaVersion::initialize).start();

        scene = new Scene(decorator.getDecorator(), 802, 482);
        stage.setMinHeight(482);
        stage.setMinWidth(802);
        decorator.getDecorator().prefWidthProperty().bind(scene.widthProperty());
        decorator.getDecorator().prefHeightProperty().bind(scene.heightProperty());
        scene.getStylesheets().setAll(ConfigHolder.config().getTheme().getStylesheets());

        stage.getIcons().add(newImage("/assets/img/icon.png"));
        stage.setTitle(Metadata.TITLE);
        stage.setScene(scene);
    }

    public static void dialog(Region content) {
        if (decorator != null)
            decorator.showDialog(content);
    }

    public static void dialog(String text) {
        dialog(text, null);
    }

    public static void dialog(String text, String title) {
        dialog(text, title, MessageDialogPane.MessageType.INFORMATION);
    }

    public static void dialog(String text, String title, MessageDialogPane.MessageType type) {
        dialog(text, title, type, null);
    }

    public static void dialog(String text, String title, MessageDialogPane.MessageType type, Runnable onAccept) {
        dialog(new MessageDialogPane(text, title, type, onAccept));
    }

    public static void confirm(String text, String title, Runnable onAccept, Runnable onCancel) {
        dialog(new MessageDialogPane(text, title, onAccept, onCancel));
    }

    public static CompletableFuture<String> prompt(String title, FutureCallback<String> onResult) {
        return prompt(title, onResult, "");
    }

    public static CompletableFuture<String> prompt(String title, FutureCallback<String> onResult, String initialValue) {
        InputDialogPane pane = new InputDialogPane(title, initialValue, onResult);
        dialog(pane);
        return pane.getCompletableFuture();
    }

    public static CompletableFuture<List<PromptDialogPane.Builder.Question<?>>> prompt(PromptDialogPane.Builder builder) {
        PromptDialogPane pane = new PromptDialogPane(builder);
        dialog(pane);
        return pane.getCompletableFuture();
    }

    public static TaskExecutorDialogPane taskDialog(TaskExecutor executor, String title) {
        return taskDialog(executor, title, null);
    }

    public static TaskExecutorDialogPane taskDialog(TaskExecutor executor, String title, Consumer<Region> onCancel) {
        TaskExecutorDialogPane pane = new TaskExecutorDialogPane(onCancel);
        pane.setTitle(title);
        pane.setExecutor(executor);
        dialog(pane);
        return pane;
    }

    public static void navigate(Node node) {
        decorator.getNavigator().navigate(node, ContainerAnimations.FADE.getAnimationProducer());
    }

    public static boolean isStopped() {
        return decorator == null;
    }

    public static void shutdown() {
        rootPage = null;
        versionPage = null;
        serversPage = null;
        decorator = null;
        stage = null;
        scene = null;
    }
}
