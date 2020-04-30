package ru.spark.slauncher.game;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ru.spark.slauncher.mod.*;
import ru.spark.slauncher.mod.curse.CurseCompletionException;
import ru.spark.slauncher.mod.curse.CurseInstallTask;
import ru.spark.slauncher.mod.curse.CurseManifest;
import ru.spark.slauncher.mod.multimc.MultiMCInstanceConfiguration;
import ru.spark.slauncher.mod.multimc.MultiMCModpackInstallTask;
import ru.spark.slauncher.mod.server.ServerModpackLocalInstallTask;
import ru.spark.slauncher.mod.server.ServerModpackManifest;
import ru.spark.slauncher.mod.server.ServerModpackRemoteInstallTask;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.VersionSetting;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.function.ExceptionalConsumer;
import ru.spark.slauncher.util.function.ExceptionalRunnable;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;

public final class ModpackHelper {
    private ModpackHelper() {
    }

    public static Modpack readModpackManifest(Path file, Charset charset) throws UnsupportedModpackException {
        try {
            return CurseManifest.readCurseForgeModpackManifest(file, charset);
        } catch (Exception e) {
            // ignore it, not a valid CurseForge modpack.
        }

        try {
            return SLModpackManager.readSLModpackManifest(file, charset);
        } catch (Exception e) {
            // ignore it, not a valid SLauncher modpack.
        }

        try {
            return MultiMCInstanceConfiguration.readMultiMCModpackManifest(file, charset);
        } catch (Exception e) {
            // ignore it, not a valid MultiMC modpack.
        }

        try {
            return ServerModpackManifest.readManifest(file, charset);
        } catch (Exception e) {
            // ignore it, not a valid Server modpack.
        }

        throw new UnsupportedModpackException(file.toString());
    }

    public static ModpackConfiguration<?> readModpackConfiguration(File file) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException(file.getPath());
        else
            try {
                return JsonUtils.GSON.fromJson(FileUtils.readText(file), new TypeToken<ModpackConfiguration<?>>() {
                }.getType());
            } catch (JsonParseException e) {
                throw new IOException("Malformed modpack configuration");
            }
    }

    private static String getManifestType(Object manifest) throws UnsupportedModpackException {
        if (manifest instanceof SLModpackManifest)
            return SLModpackInstallTask.MODPACK_TYPE;
        else if (manifest instanceof MultiMCInstanceConfiguration)
            return MultiMCModpackInstallTask.MODPACK_TYPE;
        else if (manifest instanceof CurseManifest)
            return CurseInstallTask.MODPACK_TYPE;
        else
            throw new UnsupportedModpackException();
    }

    public static Task<Void> getInstallTask(Profile profile, ServerModpackManifest manifest, String name, Modpack modpack) {
        profile.getRepository().markVersionAsModpack(name);

        ExceptionalRunnable<?> success = () -> {
            SLGameRepository repository = profile.getRepository();
            repository.refreshVersions();
            VersionSetting vs = repository.specializeVersionSetting(name);
            repository.undoMark(name);
            if (vs != null)
                vs.setGameDirType(GameDirectoryType.VERSION_FOLDER);
        };

        ExceptionalConsumer<Exception, ?> failure = ex -> {
            if (ex instanceof CurseCompletionException && !(ex.getCause() instanceof FileNotFoundException)) {
                success.run();
                // This is tolerable and we will not delete the game
            }
        };

        return new ServerModpackRemoteInstallTask(profile.getDependency(), manifest, name)
                .whenComplete(Schedulers.defaultScheduler(), success, failure);
    }

    public static Task<Void> getInstallTask(Profile profile, File zipFile, String name, Modpack modpack) {
        profile.getRepository().markVersionAsModpack(name);

        ExceptionalRunnable<?> success = () -> {
            SLGameRepository repository = profile.getRepository();
            repository.refreshVersions();
            VersionSetting vs = repository.specializeVersionSetting(name);
            repository.undoMark(name);
            if (vs != null)
                vs.setGameDirType(GameDirectoryType.VERSION_FOLDER);
        };

        ExceptionalConsumer<Exception, ?> failure = ex -> {
            if (ex instanceof CurseCompletionException && !(ex.getCause() instanceof FileNotFoundException)) {
                success.run();
                // This is tolerable and we will not delete the game
            }
        };

        if (modpack.getManifest() instanceof CurseManifest)
            return new CurseInstallTask(profile.getDependency(), zipFile, modpack, ((CurseManifest) modpack.getManifest()), name)
                    .whenComplete(Schedulers.defaultScheduler(), success, failure);
        else if (modpack.getManifest() instanceof SLModpackManifest)
            return new SLModpackInstallTask(profile, zipFile, modpack, name)
                    .whenComplete(Schedulers.defaultScheduler(), success, failure);
        else if (modpack.getManifest() instanceof MultiMCInstanceConfiguration)
            return new MultiMCModpackInstallTask(profile.getDependency(), zipFile, modpack, ((MultiMCInstanceConfiguration) modpack.getManifest()), name)
                    .whenComplete(Schedulers.defaultScheduler(), success, failure)
                    .thenComposeAsync(new MultiMCInstallVersionSettingTask(profile, ((MultiMCInstanceConfiguration) modpack.getManifest()), name));
        else if (modpack.getManifest() instanceof ServerModpackManifest)
            return new ServerModpackLocalInstallTask(profile.getDependency(), zipFile, modpack, ((ServerModpackManifest) modpack.getManifest()), name)
                    .whenComplete(Schedulers.defaultScheduler(), success, failure);
        else throw new IllegalArgumentException("Unrecognized modpack: " + modpack.getManifest());
    }

    public static Task<Void> getUpdateTask(Profile profile, File zipFile, Charset charset, String name, ModpackConfiguration<?> configuration) throws UnsupportedModpackException, MismatchedModpackTypeException {
        Modpack modpack = ModpackHelper.readModpackManifest(zipFile.toPath(), charset);

        switch (configuration.getType()) {
            case CurseInstallTask.MODPACK_TYPE:
                if (!(modpack.getManifest() instanceof CurseManifest))
                    throw new MismatchedModpackTypeException(CurseInstallTask.MODPACK_TYPE, getManifestType(modpack.getManifest()));

                return new ModpackUpdateTask(profile.getRepository(), name, new CurseInstallTask(profile.getDependency(), zipFile, modpack, (CurseManifest) modpack.getManifest(), name));
            case MultiMCModpackInstallTask.MODPACK_TYPE:
                if (!(modpack.getManifest() instanceof MultiMCInstanceConfiguration))
                    throw new MismatchedModpackTypeException(MultiMCModpackInstallTask.MODPACK_TYPE, getManifestType(modpack.getManifest()));

                return new ModpackUpdateTask(profile.getRepository(), name, new MultiMCModpackInstallTask(profile.getDependency(), zipFile, modpack, (MultiMCInstanceConfiguration) modpack.getManifest(), name));
            case SLModpackInstallTask.MODPACK_TYPE:
                if (!(modpack.getManifest() instanceof SLModpackManifest))
                    throw new MismatchedModpackTypeException(SLModpackInstallTask.MODPACK_TYPE, getManifestType(modpack.getManifest()));

                return new ModpackUpdateTask(profile.getRepository(), name, new SLModpackInstallTask(profile, zipFile, modpack, name));
            default:
                throw new UnsupportedModpackException();
        }
    }

    public static void toVersionSetting(MultiMCInstanceConfiguration c, VersionSetting vs) {
        vs.setUsesGlobal(false);
        vs.setGameDirType(GameDirectoryType.VERSION_FOLDER);

        if (c.isOverrideJavaLocation()) {
            vs.setJavaDir(Lang.nonNull(c.getJavaPath(), ""));
        }

        if (c.isOverrideMemory()) {
            vs.setPermSize(Optional.ofNullable(c.getPermGen()).map(Object::toString).orElse(""));
            if (c.getMaxMemory() != null)
                vs.setMaxMemory(c.getMaxMemory());
            vs.setMinMemory(c.getMinMemory());
        }

        if (c.isOverrideCommands()) {
            vs.setWrapper(Lang.nonNull(c.getWrapperCommand(), ""));
            vs.setPreLaunchCommand(Lang.nonNull(c.getPreLaunchCommand(), ""));
        }

        if (c.isOverrideJavaArgs()) {
            vs.setJavaArgs(Lang.nonNull(c.getJvmArgs(), ""));
        }

        if (c.isOverrideConsole()) {
            vs.setShowLogs(c.isShowConsole());
        }

        if (c.isOverrideWindow()) {
            vs.setFullscreen(c.isFullscreen());
            if (c.getWidth() != null)
                vs.setWidth(c.getWidth());
            if (c.getHeight() != null)
                vs.setHeight(c.getHeight());
        }
    }


}
