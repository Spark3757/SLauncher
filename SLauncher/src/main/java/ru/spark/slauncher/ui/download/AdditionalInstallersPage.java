package ru.spark.slauncher.ui.download;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.i18n.I18n;

import java.util.Map;
import java.util.Optional;

class AdditionalInstallersPage extends InstallersPage {
    protected final BooleanProperty compatible = new SimpleBooleanProperty();
    protected final GameRepository repository;
    protected final String gameVersion;
    protected final Version version;

    public AdditionalInstallersPage(String gameVersion, Version version, WizardController controller, GameRepository repository, InstallerWizardDownloadProvider downloadProvider) {
        super(controller, repository, gameVersion, downloadProvider);
        this.gameVersion = gameVersion;
        this.version = version;
        this.repository = repository;

        txtName.getValidators().clear();
        txtName.setText(version.getId());
        txtName.setEditable(false);

        installable.bind(Bindings.createBooleanBinding(
                () -> compatible.get() && txtName.validate(),
                txtName.textProperty(), compatible));

        InstallerPageItem[] libraries = new InstallerPageItem[]{game, fabric, forge, liteLoader, optiFine};

        for (InstallerPageItem library : libraries) {
            String libraryId = library.id;
            if (libraryId.equals("game")) continue;
            library.removeAction.set(e -> {
                controller.getSettings().put(libraryId, new UpdateInstallerWizardProvider.RemoveVersionAction(libraryId));
                reload();
            });
        }
    }

    @Override
    protected void onInstall() {
        controller.onFinish();
    }

    @Override
    public String getTitle() {
        return I18n.i18n("settings.tabs.installers");
    }

    private String getVersion(String id) {
        return Optional.ofNullable(controller.getSettings().get(id))
                .flatMap(it -> Lang.tryCast(it, RemoteVersion.class))
                .map(RemoteVersion::getSelfVersion).orElse(null);
    }

    @Override
    protected void reload() {
        LibraryAnalyzer analyzer = LibraryAnalyzer.analyze(version.resolvePreservingPatches(repository));
        String game = analyzer.getVersion(LibraryAnalyzer.LibraryType.MINECRAFT).orElse(null);
        String fabric = analyzer.getVersion(LibraryAnalyzer.LibraryType.FABRIC).orElse(null);
        String forge = analyzer.getVersion(LibraryAnalyzer.LibraryType.FORGE).orElse(null);
        String liteLoader = analyzer.getVersion(LibraryAnalyzer.LibraryType.LITELOADER).orElse(null);
        String optiFine = analyzer.getVersion(LibraryAnalyzer.LibraryType.OPTIFINE).orElse(null);

        InstallerPageItem[] libraries = new InstallerPageItem[]{this.game, this.fabric, this.forge, this.liteLoader, this.optiFine};
        String[] versions = new String[]{game, fabric, forge, liteLoader, optiFine};

        String currentGameVersion = Lang.nonNull(getVersion("game"), game);

        boolean compatible = true;
        for (int i = 0; i < libraries.length; ++i) {
            String libraryId = libraries[i].id;
            String libraryVersion = Lang.nonNull(getVersion(libraryId), versions[i]);
            boolean alreadyInstalled = versions[i] != null && !(controller.getSettings().get(libraryId) instanceof UpdateInstallerWizardProvider.RemoveVersionAction);
            if (!"game".equals(libraryId) && currentGameVersion != null && !currentGameVersion.equals(game) && getVersion(libraryId) == null && alreadyInstalled) {
                // For third-party libraries, if game version is being changed, and the library is not being reinstalled,
                // warns the user that we should update the library.
                libraries[i].label.set(I18n.i18n("install.installer.change_version", I18n.i18n("install.installer." + libraryId), libraryVersion));
                libraries[i].removable.set(true);
                compatible = false;
            } else if (alreadyInstalled || getVersion(libraryId) != null) {
                libraries[i].label.set(I18n.i18n("install.installer.version", I18n.i18n("install.installer." + libraryId), libraryVersion));
                libraries[i].removable.set(true);
            } else {
                libraries[i].label.set(I18n.i18n("install.installer.not_installed", I18n.i18n("install.installer." + libraryId)));
                libraries[i].removable.set(false);
            }
        }
        this.compatible.set(compatible);
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
    }
}
