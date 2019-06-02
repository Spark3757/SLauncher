package ru.spark.slauncher.ui;

import javafx.application.Platform;
import javafx.scene.layout.Region;
import ru.spark.slauncher.event.EventBus;
import ru.spark.slauncher.event.RefreshedVersionsEvent;
import ru.spark.slauncher.game.ModpackHelper;
import ru.spark.slauncher.game.SLauncherGameRepository;
import ru.spark.slauncher.setting.Accounts;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.ui.account.AccountAdvancedListItem;
import ru.spark.slauncher.ui.account.AddAccountPane;
import ru.spark.slauncher.ui.construct.AdvancedListBox;
import ru.spark.slauncher.ui.construct.AdvancedListItem;
import ru.spark.slauncher.ui.profile.ProfileAdvancedListItem;
import ru.spark.slauncher.ui.versions.GameAdvancedListItem;
import ru.spark.slauncher.ui.versions.Versions;
import ru.spark.slauncher.util.io.CompressingUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import static ru.spark.slauncher.ui.FXUtils.newImage;
import static ru.spark.slauncher.ui.FXUtils.runInFX;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class LeftPaneController extends AdvancedListBox {

    private boolean checkedModpack = false;

    public LeftPaneController() {

        AccountAdvancedListItem accountListItem = new AccountAdvancedListItem();
        accountListItem.setOnAction(e -> Controllers.navigate(Controllers.getAccountListPage()));
        accountListItem.accountProperty().bind(Accounts.selectedAccountProperty());

        GameAdvancedListItem gameListItem = new GameAdvancedListItem();
        gameListItem.actionButtonVisibleProperty().bind(Profiles.selectedVersionProperty().isNotNull());
        gameListItem.setOnAction(e -> {
            Profile profile = Profiles.getSelectedProfile();
            String version = Profiles.getSelectedVersion();
            if (version == null) {
                Controllers.navigate(Controllers.getGameListPage());
            } else {
                Versions.modifyGameSettings(profile, version);
            }
        });

        ProfileAdvancedListItem profileListItem = new ProfileAdvancedListItem();
        profileListItem.setOnAction(e -> Controllers.navigate(Controllers.getProfileListPage()));
        profileListItem.profileProperty().bind(Profiles.selectedProfileProperty());

        AdvancedListItem gameItem = new AdvancedListItem();
        gameItem.setImage(newImage("/assets/img/bookshelf.png"));
        gameItem.setTitle(i18n("version.manage"));
        gameItem.setOnAction(e -> Controllers.navigate(Controllers.getGameListPage()));

        AdvancedListItem launcherSettingsItem = new AdvancedListItem();
        launcherSettingsItem.setImage(newImage("/assets/img/command.png"));
        launcherSettingsItem.setTitle(i18n("settings.launcher"));
        launcherSettingsItem.setOnAction(e -> Controllers.navigate(Controllers.getSettingsPage()));

        this
                .startCategory(i18n("account").toUpperCase())
                .add(accountListItem)
                .startCategory(i18n("version").toUpperCase())
                .add(gameListItem)
                .add(gameItem)
                .startCategory(i18n("profile.title").toUpperCase())
                .add(profileListItem)
                .startCategory(i18n("launcher").toUpperCase())
                .add(launcherSettingsItem);

        EventBus.EVENT_BUS.channel(RefreshedVersionsEvent.class).register(event -> onRefreshedVersions((SLauncherGameRepository) event.getSource()));

        Profile profile = Profiles.getSelectedProfile();
        if (profile != null && profile.getRepository().isLoaded())
            onRefreshedVersions(Profiles.selectedProfileProperty().get().getRepository());
    }

    // ==== Accounts ====
    public void checkAccount() {
        if (Accounts.getAccounts().isEmpty())
            Platform.runLater(this::addNewAccount);
    }
    // ====

    private void addNewAccount() {
        Controllers.dialog(new AddAccountPane());
    }

    private void onRefreshedVersions(SLauncherGameRepository repository) {
        runInFX(() -> {
            if (!checkedModpack) {
                checkedModpack = true;

                if (repository.getVersionCount() == 0) {
                    File modpackFile = new File("modpack.zip").getAbsoluteFile();
                    if (modpackFile.exists()) {
                        Task.ofResult(() -> CompressingUtils.findSuitableEncoding(modpackFile.toPath()))
                                .thenApply(encoding -> ModpackHelper.readModpackManifest(modpackFile.toPath(), encoding))
                                .thenAccept(modpack -> {
                                    AtomicReference<Region> region = new AtomicReference<>();
                                    TaskExecutor executor = ModpackHelper.getInstallTask(repository.getProfile(), modpackFile, modpack.getName(), modpack)
                                            .with(Task.of(Schedulers.javafx(), this::checkAccount)).executor();
                                    region.set(Controllers.taskDialog(executor, i18n("modpack.installing")));
                                    executor.start();
                                }).start();
                    }
                }
            }

            checkAccount();
        });
    }
}
