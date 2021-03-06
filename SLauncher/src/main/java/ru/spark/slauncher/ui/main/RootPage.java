package ru.spark.slauncher.ui.main;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import ru.spark.slauncher.event.EventBus;
import ru.spark.slauncher.event.RefreshedVersionsEvent;
import ru.spark.slauncher.game.ModpackHelper;
import ru.spark.slauncher.game.SLGameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.Accounts;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.account.AccountAdvancedListItem;
import ru.spark.slauncher.ui.account.AccountList;
import ru.spark.slauncher.ui.account.AddAccountPane;
import ru.spark.slauncher.ui.construct.AdvancedListBox;
import ru.spark.slauncher.ui.construct.AdvancedListItem;
import ru.spark.slauncher.ui.construct.TabControl;
import ru.spark.slauncher.ui.construct.TabHeader;
import ru.spark.slauncher.ui.decorator.DecoratorTabPage;
import ru.spark.slauncher.ui.download.ModpackInstallWizardProvider;
import ru.spark.slauncher.ui.profile.ProfileAdvancedListItem;
import ru.spark.slauncher.ui.profile.ProfileList;
import ru.spark.slauncher.ui.versions.GameAdvancedListItem;
import ru.spark.slauncher.ui.versions.GameList;
import ru.spark.slauncher.ui.versions.Versions;
import ru.spark.slauncher.upgrade.UpdateChecker;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.javafx.BindingMapping;
import ru.spark.slauncher.util.versioning.VersionNumber;

import static ru.spark.slauncher.ui.FXUtils.newImage;
import static ru.spark.slauncher.ui.FXUtils.runInFX;

public class RootPage extends DecoratorTabPage {
    private final TabHeader.Tab mainTab = new TabHeader.Tab("main");
    private final TabHeader.Tab settingsTab = new TabHeader.Tab("settings");
    private final TabHeader.Tab gameTab = new TabHeader.Tab("game");
    private final TabHeader.Tab accountTab = new TabHeader.Tab("account");
    private final TabHeader.Tab profileTab = new TabHeader.Tab("profile");
    private MainPage mainPage = null;
    private SettingsPage settingsPage = null;
    private GameList gameListPage = null;
    private AccountList accountListPage = null;
    private ProfileList profileListPage = null;
    private boolean checkedAccont = false;
    private boolean checkedModpack = false;

    public RootPage() {
        setLeftPaneWidth(200);

        EventBus.EVENT_BUS.channel(RefreshedVersionsEvent.class).register(event -> onRefreshedVersions((SLGameRepository) event.getSource()));

        Profile profile = Profiles.getSelectedProfile();
        if (profile != null && profile.getRepository().isLoaded())
            onRefreshedVersions(Profiles.selectedProfileProperty().get().getRepository());

        mainTab.setNodeSupplier(this::getMainPage);
        settingsTab.setNodeSupplier(this::getSettingsPage);
        gameTab.setNodeSupplier(this::getGameListPage);
        accountTab.setNodeSupplier(this::getAccountListPage);
        profileTab.setNodeSupplier(this::getProfileListPage);
        getTabs().setAll(mainTab, settingsTab, gameTab, accountTab, profileTab);
    }

    @Override
    public boolean back() {
        if (mainTab.isSelected()) return true;
        else {
            getSelectionModel().select(mainTab);
            return false;
        }
    }

    @Override
    protected void onNavigated(Node to) {
        backableProperty().set(!(to instanceof MainPage));
        setTitleBarTransparent(to instanceof MainPage);

        super.onNavigated(to);
    }

    @Override
    protected Skin createDefaultSkin() {
        return new Skin(this);
    }

    private MainPage getMainPage() {
        if (mainPage == null) {
            MainPage mainPage = new MainPage();
            FXUtils.applyDragListener(mainPage, it -> "zip".equals(FileUtils.getExtension(it)), modpacks -> {
                File modpack = modpacks.get(0);
                Controllers.getDecorator().startWizard(new ModpackInstallWizardProvider(Profiles.getSelectedProfile(), modpack), I18n.i18n("install.modpack"));
            });

            FXUtils.onChangeAndOperate(Profiles.selectedVersionProperty(), mainPage::setCurrentGame);
            mainPage.showUpdateProperty().bind(UpdateChecker.outdatedProperty());
            mainPage.latestVersionProperty().bind(
                    BindingMapping.of(UpdateChecker.latestVersionProperty())
                            .map(version -> version == null ? "" : I18n.i18n("update.bubble.title", version.getVersion())));

            Profiles.registerVersionsListener(profile -> {
                SLGameRepository repository = profile.getRepository();
                List<Version> children = repository.getVersions().parallelStream()
                        .filter(version -> !version.isHidden())
                        .sorted(Comparator.comparing((Version version) -> version.getReleaseTime() == null ? new Date(0L) : version.getReleaseTime())
                                .thenComparing(a -> VersionNumber.asVersion(a.getId())))
                        .collect(Collectors.toList());
                runInFX(() -> {
                    if (profile == Profiles.getSelectedProfile())
                        mainPage.initVersions(profile, children);
                });
            });
            this.mainPage = mainPage;
        }
        return mainPage;
    }

    private SettingsPage getSettingsPage() {
        if (settingsPage == null)
            settingsPage = new SettingsPage();
        return settingsPage;
    }

    private GameList getGameListPage() {
        if (gameListPage == null) {
            gameListPage = new GameList();
            FXUtils.applyDragListener(gameListPage, it -> "zip".equals(FileUtils.getExtension(it)), modpacks -> {
                File modpack = modpacks.get(0);
                Controllers.getDecorator().startWizard(new ModpackInstallWizardProvider(Profiles.getSelectedProfile(), modpack), I18n.i18n("install.modpack"));
            });
        }
        return gameListPage;
    }

    private AccountList getAccountListPage() {
        if (accountListPage == null) {
            accountListPage = new AccountList();
            accountListPage.selectedAccountProperty().bindBidirectional(Accounts.selectedAccountProperty());
            accountListPage.accountsProperty().bindContent(Accounts.accountsProperty());
        }
        return accountListPage;
    }

    private ProfileList getProfileListPage() {
        if (profileListPage == null) {
            profileListPage = new ProfileList();
            profileListPage.selectedProfileProperty().bindBidirectional(Profiles.selectedProfileProperty());
            profileListPage.profilesProperty().bindContent(Profiles.profilesProperty());
        }
        return profileListPage;
    }

    public TabControl.Tab getMainTab() {
        return mainTab;
    }

    public TabControl.Tab getSettingsTab() {
        return settingsTab;
    }

    public TabControl.Tab getGameTab() {
        return gameTab;
    }

    public TabControl.Tab getAccountTab() {
        return accountTab;
    }

    public TabControl.Tab getProfileTab() {
        return profileTab;
    }

    // ==== Accounts ====

    private void selectPage(TabControl.Tab tab) {
        if (getSelectionModel().getSelectedItem() == tab) {
            getSelectionModel().select(getMainTab());
        } else {
            getSelectionModel().select(tab);
        }
    }

    public void checkAccount() {
        if (checkedAccont) return;
        checkedAccont = true;
        if (Accounts.getAccounts().isEmpty())
            Platform.runLater(this::addNewAccount);
    }

    private void addNewAccount() {
        Controllers.dialog(new AddAccountPane());
    }
    // ====

    private void onRefreshedVersions(SLGameRepository repository) {
        runInFX(() -> {
            if (!checkedModpack) {
                checkedModpack = true;

                if (repository.getVersionCount() == 0) {
                    File modpackFile = new File("modpack.zip").getAbsoluteFile();
                    if (modpackFile.exists()) {
                        Task.supplyAsync(() -> CompressingUtils.findSuitableEncoding(modpackFile.toPath()))
                                .thenApplyAsync(encoding -> ModpackHelper.readModpackManifest(modpackFile.toPath(), encoding))
                                .thenApplyAsync(modpack -> ModpackHelper.getInstallTask(repository.getProfile(), modpackFile, modpack.getName(), modpack)
                                        .withRunAsync(Schedulers.javafx(), this::checkAccount).executor())
                                .thenAcceptAsync(Schedulers.javafx(), executor -> {
                                    Controllers.taskDialog(executor, I18n.i18n("modpack.installing"));
                                    executor.start();
                                }).start();
                    }
                }
            }

            checkAccount();
        });
    }

    private static class Skin extends SkinBase<RootPage> {

        protected Skin(RootPage control) {
            super(control);

            // first item in left sidebar
            AccountAdvancedListItem accountListItem = new AccountAdvancedListItem();
            accountListItem.activeProperty().bind(control.accountTab.selectedProperty());
            accountListItem.setOnAction(e -> control.selectPage(control.accountTab));
            accountListItem.accountProperty().bind(Accounts.selectedAccountProperty());

            // second item in left sidebar
            GameAdvancedListItem gameListItem = new GameAdvancedListItem();
            gameListItem.actionButtonVisibleProperty().bind(Profiles.selectedVersionProperty().isNotNull());
            gameListItem.setOnAction(e -> {
                Profile profile = Profiles.getSelectedProfile();
                String version = Profiles.getSelectedVersion();
                if (version == null) {
                    control.selectPage(control.gameTab);
                } else {
                    Versions.modifyGameSettings(profile, version);
                }
            });

            // third item in left sidebar
            AdvancedListItem gameItem = new AdvancedListItem();
            gameItem.activeProperty().bind(control.gameTab.selectedProperty());
            gameItem.setImage(newImage("/assets/img/bookshelf.png"));
            gameItem.setTitle(I18n.i18n("version.manage"));
            gameItem.setOnAction(e -> control.selectPage(control.gameTab));

            // forth item in left sidebar
            ProfileAdvancedListItem profileListItem = new ProfileAdvancedListItem();
            profileListItem.activeProperty().bind(control.profileTab.selectedProperty());
            profileListItem.setOnAction(e -> control.selectPage(control.profileTab));
            profileListItem.profileProperty().bind(Profiles.selectedProfileProperty());

            // fifth item in left sidebar
            AdvancedListItem launcherSettingsItem = new AdvancedListItem();
            launcherSettingsItem.activeProperty().bind(control.settingsTab.selectedProperty());
            launcherSettingsItem.setImage(newImage("/assets/img/command.png"));
            launcherSettingsItem.setTitle(I18n.i18n("settings.launcher"));
            launcherSettingsItem.setOnAction(e -> control.selectPage(control.settingsTab));

            // the left sidebar
            AdvancedListBox sideBar = new AdvancedListBox()
                    .startCategory(I18n.i18n("account").toUpperCase())
                    .add(accountListItem)
                    .startCategory(I18n.i18n("version").toUpperCase())
                    .add(gameListItem)
                    .add(gameItem)
                    .startCategory(I18n.i18n("profile.title").toUpperCase())
                    .add(profileListItem)
                    .startCategory(I18n.i18n("launcher").toUpperCase())
                    .add(launcherSettingsItem);

            // the root page, with the sidebar in left, navigator in center.
            BorderPane root = new BorderPane();
            sideBar.setPrefWidth(200);
            root.setLeft(sideBar);

            {
                control.transitionPane.getStyleClass().add("jfx-decorator-content-container");
                control.transitionPane.getChildren().setAll(getSkinnable().getMainPage());
                FXUtils.setOverflowHidden(control.transitionPane, 8);
                root.setCenter(control.transitionPane);
            }

            getChildren().setAll(root);
        }

    }
}
