package ru.spark.slauncher.game;

import ru.spark.slauncher.mod.multimc.MultiMCInstanceConfiguration;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.VersionSetting;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;

import java.util.Objects;

public final class MultiMCInstallVersionSettingTask extends Task<Void> {
    private final Profile profile;
    private final MultiMCInstanceConfiguration manifest;
    private final String version;

    public MultiMCInstallVersionSettingTask(Profile profile, MultiMCInstanceConfiguration manifest, String version) {
        this.profile = profile;
        this.manifest = manifest;
        this.version = version;

        setExecutor(Schedulers.javafx());
    }

    @Override
    public void execute() {
        VersionSetting vs = Objects.requireNonNull(profile.getRepository().specializeVersionSetting(version));
        ModpackHelper.toVersionSetting(manifest, vs);
    }
}
