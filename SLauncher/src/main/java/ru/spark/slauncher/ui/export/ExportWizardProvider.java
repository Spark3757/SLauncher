package ru.spark.slauncher.ui.export;

import javafx.scene.Node;
import ru.spark.slauncher.Launcher;
import ru.spark.slauncher.game.SLModpackExportTask;
import ru.spark.slauncher.mod.ModAdviser;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.mod.multimc.MultiMCInstanceConfiguration;
import ru.spark.slauncher.mod.multimc.MultiMCModpackExportTask;
import ru.spark.slauncher.mod.server.ServerModpackExportTask;
import ru.spark.slauncher.setting.Config;
import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.VersionSetting;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardProvider;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.io.Zipper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ExportWizardProvider implements WizardProvider {
    private final Profile profile;
    private final String version;

    public ExportWizardProvider(Profile profile, String version) {
        this.profile = profile;
        this.version = version;
    }

    @Override
    public void start(Map<String, Object> settings) {
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        @SuppressWarnings("unchecked")
        List<String> whitelist = (List<String>) settings.get(ModpackFileSelectionPage.MODPACK_FILE_SELECTION);
        File modpackFile = (File) settings.get(ModpackInfoPage.MODPACK_FILE);
        String modpackName = (String) settings.get(ModpackInfoPage.MODPACK_NAME);
        String modpackAuthor = (String) settings.get(ModpackInfoPage.MODPACK_AUTHOR);
        String modpackFileApi = (String) settings.get(ModpackInfoPage.MODPACK_FILE_API);
        String modpackVersion = (String) settings.get(ModpackInfoPage.MODPACK_VERSION);
        String modpackDescription = (String) settings.get(ModpackInfoPage.MODPACK_DESCRIPTION);
        String modpackType = (String) settings.get(ModpackTypeSelectionPage.MODPACK_TYPE);
        boolean includeLauncher = (Boolean) settings.get(ModpackInfoPage.MODPACK_INCLUDE_LAUNCHER);

        switch (modpackType) {
            case ModpackTypeSelectionPage.MODPACK_TYPE_SL:
                return exportAsSL(whitelist, modpackFile, modpackName, modpackAuthor, modpackVersion, modpackDescription, includeLauncher);
            case ModpackTypeSelectionPage.MODPACK_TYPE_MULTIMC:
                return exportAsMultiMC(whitelist, modpackFile, modpackName, modpackAuthor, modpackVersion, modpackDescription);
            case ModpackTypeSelectionPage.MODPACK_TYPE_SERVER:
                return exportAsServer(whitelist, modpackFile, modpackName, modpackAuthor, modpackVersion, modpackDescription, modpackFileApi);
            default:
                throw new IllegalStateException("Unrecognized modpack type " + modpackType);
        }
    }

    private Task<?> exportAsSL(List<String> whitelist, File modpackFile, String modpackName, String modpackAuthor, String modpackVersion, String modpackDescription, boolean includeLauncherRaw) {
        List<File> launcherJar = Launcher.getCurrentJarFiles();
        boolean includeLauncher = includeLauncherRaw && launcherJar != null;

        return new Task<Void>() {
            Task<?> dependency = null;

            @Override
            public void execute() throws Exception {
                File tempModpack = includeLauncher ? Files.createTempFile("slauncher", ".zip").toFile() : modpackFile;

                dependency = new SLModpackExportTask(profile.getRepository(), version, whitelist,
                        new Modpack(modpackName, modpackAuthor, modpackVersion, null, modpackDescription, StandardCharsets.UTF_8, null), tempModpack);

                if (includeLauncher) {
                    dependency = dependency.thenRunAsync(() -> {
                        try (Zipper zip = new Zipper(modpackFile.toPath())) {
                            Config exported = new Config();

                            exported.setBackgroundImageType(ConfigHolder.config().getBackgroundImageType());
                            exported.setBackgroundImage(ConfigHolder.config().getBackgroundImage());
                            exported.setTheme(ConfigHolder.config().getTheme());
                            exported.setDownloadType(ConfigHolder.config().getDownloadType());
                            exported.setPreferredLoginType(ConfigHolder.config().getPreferredLoginType());
                            exported.getAuthlibInjectorServers().setAll(ConfigHolder.config().getAuthlibInjectorServers());

                            zip.putTextFile(exported.toJson(), ConfigHolder.CONFIG_FILENAME);
                            zip.putFile(tempModpack, "modpack.zip");

                            File bg = new File("bg").getAbsoluteFile();
                            if (bg.isDirectory())
                                zip.putDirectory(bg.toPath(), "bg");

                            File background_png = new File("background.png").getAbsoluteFile();
                            if (background_png.isFile())
                                zip.putFile(background_png, "background.png");

                            File background_jpg = new File("background.jpg").getAbsoluteFile();
                            if (background_jpg.isFile())
                                zip.putFile(background_jpg, "background.jpg");

                            for (File jar : launcherJar)
                                zip.putFile(jar, jar.getName());
                        }
                    });
                }
            }

            @Override
            public Collection<Task<?>> getDependencies() {
                return Collections.singleton(dependency);
            }
        };
    }

    private Task<?> exportAsMultiMC(List<String> whitelist, File modpackFile, String modpackName, String modpackAuthor, String modpackVersion, String modpackDescription) {
        return new Task<Void>() {
            Task<?> dependency;

            @Override
            public void execute() {
                VersionSetting vs = profile.getVersionSetting(version);
                dependency = new MultiMCModpackExportTask(profile.getRepository(), version, whitelist,
                        new MultiMCInstanceConfiguration(
                                "OneSix",
                                modpackName + "-" + modpackVersion,
                                null,
                                Lang.toIntOrNull(vs.getPermSize()),
                                vs.getWrapper(),
                                vs.getPreLaunchCommand(),
                                null,
                                modpackDescription,
                                null,
                                vs.getJavaArgs(),
                                vs.isFullscreen(),
                                vs.getWidth(),
                                vs.getHeight(),
                                vs.getMaxMemory(),
                                vs.getMinMemory(),
                                vs.isShowLogs(),
                                /* showConsoleOnError */ true,
                                /* autoCloseConsole */ false,
                                /* overrideMemory */ true,
                                /* overrideJavaLocation */ false,
                                /* overrideJavaArgs */ true,
                                /* overrideConsole */ true,
                                /* overrideCommands */ true,
                                /* overrideWindow */ true
                        ), modpackFile);
            }

            @Override
            public Collection<Task<?>> getDependencies() {
                return Collections.singleton(dependency);
            }
        };
    }

    private Task<?> exportAsServer(List<String> whitelist, File modpackFile, String modpackName, String modpackAuthor, String modpackVersion, String modpackDescription, String modpackFileApi) {
        return new Task<Void>() {
            Task<?> dependency;

            @Override
            public void execute() {
                dependency = new ServerModpackExportTask(profile.getRepository(), version, whitelist, modpackName, modpackAuthor, modpackVersion, modpackDescription, modpackFileApi, modpackFile);
            }

            @Override
            public Collection<Task<?>> getDependencies() {
                return Collections.singleton(dependency);
            }
        };
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        switch (step) {
            case 0:
                return new ModpackTypeSelectionPage(controller);
            case 1:
                return new ModpackInfoPage(controller, version);
            case 2:
                return new ModpackFileSelectionPage(controller, profile, version, ModAdviser::suggestMod);
            default:
                throw new IllegalArgumentException("step");
        }
    }

    @Override
    public boolean cancel() {
        return true;
    }
}
