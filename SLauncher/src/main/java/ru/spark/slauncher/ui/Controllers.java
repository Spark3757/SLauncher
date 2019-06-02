package ru.spark.slauncher.ui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import ru.spark.slauncher.Launcher;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.game.SLauncherGameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.Accounts;
import ru.spark.slauncher.setting.EnumCommonDirectory;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.ui.account.AccountList;
import ru.spark.slauncher.ui.account.AuthlibInjectorServersPage;
import ru.spark.slauncher.ui.construct.InputDialogPane;
import ru.spark.slauncher.ui.construct.MessageDialogPane;
import ru.spark.slauncher.ui.construct.MessageDialogPane.MessageType;
import ru.spark.slauncher.ui.construct.PopupMenu;
import ru.spark.slauncher.ui.construct.TaskExecutorDialogPane;
import ru.spark.slauncher.ui.decorator.DecoratorController;
import ru.spark.slauncher.ui.download.ModpackInstallWizardProvider;
import ru.spark.slauncher.ui.profile.ProfileList;
import ru.spark.slauncher.ui.versions.GameItem;
import ru.spark.slauncher.ui.versions.GameList;
import ru.spark.slauncher.ui.versions.VersionPage;
import ru.spark.slauncher.upgrade.UpdateChecker;
import ru.spark.slauncher.util.FutureCallback;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.javafx.BindingMapping;
import ru.spark.slauncher.util.platform.JavaVersion;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.ui.FXUtils.runInFX;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class Controllers {

    private static Scene scene;
    private static Stage stage;
    private static MainPage mainPage = null;
    private static SettingsPage settingsPage = null;
    private static VersionPage versionPage = null;
    private static GameList gameListPage = null;
    private static AccountList accountListPage = null;
    private static ProfileList profileListPage = null;
    private static AuthlibInjectorServersPage serversPage = null;
    private static LeftPaneController leftPaneController;
    private static DecoratorController decorator;

    public static Scene getScene() {
        return scene;
    }

    public static Stage getStage() {
        return stage;
    }

    // FXThread
    public static SettingsPage getSettingsPage() {
        if (settingsPage == null)
            settingsPage = new SettingsPage();
        return settingsPage;
    }

    // FXThread
    public static GameList getGameListPage() {
        if (gameListPage == null) {
            gameListPage = new GameList();
            FXUtils.applyDragListener(gameListPage, it -> "zip".equals(FileUtils.getExtension(it)), modpacks -> {
                File modpack = modpacks.get(0);
                Controllers.getDecorator().startWizard(new ModpackInstallWizardProvider(Profiles.getSelectedProfile(), modpack), i18n("install.modpack"));
            });
        }
        return gameListPage;
    }

    // FXThread
    public static AccountList getAccountListPage() {
        if (accountListPage == null) {
            AccountList accountListPage = new AccountList();
            accountListPage.selectedAccountProperty().bindBidirectional(Accounts.selectedAccountProperty());
            accountListPage.accountsProperty().bindContent(Accounts.accountsProperty());
            Controllers.accountListPage = accountListPage;
        }
        return accountListPage;
    }

    // FXThread
    public static ProfileList getProfileListPage() {
        if (profileListPage == null) {
            ProfileList profileListPage = new ProfileList();
            profileListPage.selectedProfileProperty().bindBidirectional(Profiles.selectedProfileProperty());
            profileListPage.profilesProperty().bindContent(Profiles.profilesProperty());
            Controllers.profileListPage = profileListPage;
        }
        return profileListPage;
    }

    // FXThread
    public static VersionPage getVersionPage() {
        if (versionPage == null)
            versionPage = new VersionPage();
        return versionPage;
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

    public static MainPage getMainPage() {
        if (mainPage == null) {
            MainPage mainPage = new MainPage();
            FXUtils.applyDragListener(mainPage, it -> "zip".equals(FileUtils.getExtension(it)), modpacks -> {
                File modpack = modpacks.get(0);
                Controllers.getDecorator().startWizard(new ModpackInstallWizardProvider(Profiles.getSelectedProfile(), modpack), i18n("install.modpack"));
            });

            FXUtils.onChangeAndOperate(Profiles.selectedVersionProperty(), version -> {
                if (version != null) {
                    mainPage.setCurrentGame(version);
                } else {
                    mainPage.setCurrentGame(i18n("version.empty"));
                }
            });
            mainPage.showUpdateProperty().bind(UpdateChecker.outdatedProperty());
            mainPage.latestVersionProperty().bind(
                    BindingMapping.of(UpdateChecker.latestVersionProperty())
                            .map(version -> version == null ? "" : i18n("update.bubble.title", version.getVersion())));

            Profiles.registerVersionsListener(profile -> {
                SLauncherGameRepository repository = profile.getRepository();
                List<Node> children = repository.getVersions().parallelStream()
                        .filter(version -> !version.isHidden())
                        .sorted(Comparator.comparing((Version version) -> version.getReleaseTime() == null ? new Date(0L) : version.getReleaseTime())
                                .thenComparing(a -> VersionNumber.asVersion(a.getId())))
                        .map(version -> {
                            Node node = PopupMenu.wrapPopupMenuItem(new GameItem(profile, version.getId()));
                            node.setOnMouseClicked(e -> profile.setSelectedVersion(version.getId()));
                            return node;
                        })
                        .collect(Collectors.toList());
                runInFX(() -> {
                    if (profile == Profiles.getSelectedProfile())
                        mainPage.getVersions().setAll(children);
                });
            });
            Controllers.mainPage = mainPage;
        }
        return mainPage;
    }

    public static LeftPaneController getLeftPaneController() {
        return leftPaneController;
    }

    public static void initialize(Stage stage) {
        Logging.LOG.info("Start initializing application");

        Controllers.stage = stage;

        stage.setOnCloseRequest(e -> Launcher.stopApplication());

        decorator = new DecoratorController(stage, getMainPage());
        leftPaneController = new LeftPaneController();
        decorator.getDecorator().drawerProperty().setAll(leftPaneController);

        if (config().getCommonDirType() == EnumCommonDirectory.CUSTOM &&
                !FileUtils.canCreateDirectory(config().getCommonDirectory())) {
            config().setCommonDirType(EnumCommonDirectory.DEFAULT);
            dialog(i18n("launcher.cache_directory.invalid"));
        }

        Task.of(JavaVersion::initialize).start();

        scene = new Scene(decorator.getDecorator(), 800, 519);
        scene.getStylesheets().setAll(config().getTheme().getStylesheets());

        stage.getIcons().add(new Image("/assets/img/icon.png"));
        stage.setTitle(Metadata.TITLE);
    }

    public static void dialog(Region content) {
        if (decorator != null)
            decorator.showDialog(content);
    }

    public static void dialog(String text) {
        dialog(text, null);
    }

    public static void dialog(String text, String title) {
        dialog(text, title, MessageType.INFORMATION);
    }

    public static void dialog(String text, String title, MessageType type) {
        dialog(text, title, type, null);
    }

    public static void dialog(String text, String title, MessageType type, Runnable onAccept) {
        dialog(new MessageDialogPane(text, title, type, onAccept));
    }

    public static void confirmDialog(String text, String title, Runnable onAccept, Runnable onCancel) {
        dialog(new MessageDialogPane(text, title, onAccept, onCancel));
    }

    public static InputDialogPane inputDialog(String text, FutureCallback<String> onResult) {
        InputDialogPane pane = new InputDialogPane(text, onResult);
        dialog(pane);
        return pane;
    }

    public static Region taskDialog(TaskExecutor executor, String title) {
        return taskDialog(executor, title, "");
    }

    public static Region taskDialog(TaskExecutor executor, String title, String subtitle) {
        return taskDialog(executor, title, subtitle, null);
    }

    public static Region taskDialog(TaskExecutor executor, String title, String subtitle, Consumer<Region> onCancel) {
        TaskExecutorDialogPane pane = new TaskExecutorDialogPane(onCancel);
        pane.setTitle(title);
        pane.setSubtitle(subtitle);
        pane.setExecutor(executor);
        dialog(pane);
        return pane;
    }

    public static void navigate(Node node) {
        decorator.getNavigator().navigate(node);
    }

    public static boolean isStopped() {
        return decorator == null;
    }

    public static void shutdown() {
        mainPage = null;
        settingsPage = null;
        versionPage = null;
        serversPage = null;
        decorator = null;
        stage = null;
        scene = null;
        gameListPage = null;
        accountListPage = null;
        profileListPage = null;
    }
}
