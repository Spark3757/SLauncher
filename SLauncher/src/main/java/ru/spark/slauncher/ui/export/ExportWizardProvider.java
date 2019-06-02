package ru.spark.slauncher.ui.export;

import javafx.scene.Node;
import ru.spark.slauncher.Launcher;
import ru.spark.slauncher.game.SLauncherModpackExportTask;
import ru.spark.slauncher.game.SLauncherModpackManager;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.setting.Config;
import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardProvider;
import ru.spark.slauncher.util.io.Zipper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.spark.slauncher.setting.ConfigHolder.config;

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
        List<File> launcherJar = Launcher.getCurrentJarFiles();
        boolean includeLauncher = (Boolean) settings.get(ModpackInfoPage.MODPACK_INCLUDE_LAUNCHER) && launcherJar != null;

        return new Task() {
            Task dependency = null;

            @Override
            public void execute() throws Exception {
                File modpackFile = (File) settings.get(ModpackInfoPage.MODPACK_FILE);
                File tempModpack = includeLauncher ? Files.createTempFile("slauncher", ".zip").toFile() : modpackFile;

                dependency = new SLauncherModpackExportTask(profile.getRepository(), version, whitelist,
                        new Modpack(
                                (String) settings.get(ModpackInfoPage.MODPACK_NAME),
                                (String) settings.get(ModpackInfoPage.MODPACK_AUTHOR),
                                (String) settings.get(ModpackInfoPage.MODPACK_VERSION),
                                null,
                                (String) settings.get(ModpackInfoPage.MODPACK_DESCRIPTION),
                                StandardCharsets.UTF_8,
                                null
                        ), tempModpack);

                if (includeLauncher) {
                    dependency = dependency.then(Task.of(() -> {
                        try (Zipper zip = new Zipper(modpackFile.toPath())) {
                            Config exported = new Config();

                            exported.setBackgroundImageType(config().getBackgroundImageType());
                            exported.setBackgroundImage(config().getBackgroundImage());
                            exported.setTheme(config().getTheme());
                            exported.setDownloadType(config().getDownloadType());
                            exported.setPreferredLoginType(config().getPreferredLoginType());
                            exported.getAuthlibInjectorServers().setAll(config().getAuthlibInjectorServers());

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
                    }));
                }
            }

            @Override
            public Collection<? extends Task> getDependencies() {
                return Collections.singleton(dependency);
            }
        };
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        switch (step) {
            case 0:
                return new ModpackInfoPage(controller, version);
            case 1:
                return new ModpackFileSelectionPage(controller, profile, version, SLauncherModpackManager::suggestMod);
            default:
                throw new IllegalArgumentException("step");
        }
    }

    @Override
    public boolean cancel() {
        return true;
    }
}
