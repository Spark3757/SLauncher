package ru.spark.slauncher.ui.versions;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import ru.spark.slauncher.event.EventBus;
import ru.spark.slauncher.event.RefreshingVersionsEvent;
import ru.spark.slauncher.game.SLGameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.ui.*;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.ui.download.ModpackInstallWizardProvider;
import ru.spark.slauncher.ui.download.VanillaInstallWizardProvider;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static ru.spark.slauncher.ui.FXUtils.runInFX;

public class GameList extends ListPageBase<GameListItem> implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.fromTitle(I18n.i18n("version.manage")));

    private ToggleGroup toggleGroup;

    public GameList() {
        EventBus.EVENT_BUS.channel(RefreshingVersionsEvent.class).register(event -> {
            if (event.getSource() == Profiles.getSelectedProfile().getRepository())
                runInFX(() -> setLoading(true));
        });

        Profiles.registerVersionsListener(this::loadVersions);
    }

    private void loadVersions(Profile profile) {
        SLGameRepository repository = profile.getRepository();
        toggleGroup = new ToggleGroup();
        WeakListenerHolder listenerHolder = new WeakListenerHolder();
        toggleGroup.getProperties().put("ReferenceHolder", listenerHolder);
        List<GameListItem> children = repository.getVersions().parallelStream()
                .filter(version -> !version.isHidden())
                .sorted(Comparator.comparing((Version version) -> version.getReleaseTime() == null ? new Date(0L) : version.getReleaseTime())
                        .thenComparing(a -> VersionNumber.asVersion(a.getId())))
                .map(version -> new GameListItem(toggleGroup, profile, version.getId()))
                .collect(Collectors.toList());
        runInFX(() -> {
            if (profile == Profiles.getSelectedProfile()) {
                setLoading(false);
                itemsProperty().setAll(children);
                children.forEach(GameListItem::checkSelection);

                profile.selectedVersionProperty().addListener(listenerHolder.weak((a, b, newValue) -> {
                    FXUtils.checkFxUserThread();
                    children.forEach(it -> it.selectedProperty().set(false));
                    children.stream()
                            .filter(it -> it.getVersion().equals(newValue))
                            .findFirst()
                            .ifPresent(it -> it.selectedProperty().set(true));
                }));
            }
            toggleGroup.selectedToggleProperty().addListener((o, a, toggle) -> {
                if (toggle == null) return;
                GameListItem model = (GameListItem) toggle.getUserData();
                model.getProfile().setSelectedVersion(model.getVersion());
            });
        });
    }

    @Override
    protected GameListSkin createDefaultSkin() {
        return new GameListSkin();
    }

    public static void addNewGame() {
        Profile profile = Profiles.getSelectedProfile();
        if (profile.getRepository().isLoaded()) {
            Controllers.getDecorator().startWizard(new VanillaInstallWizardProvider(profile), I18n.i18n("install.new_game"));
        }
    }

    public static void importModpack() {
        Profile profile = Profiles.getSelectedProfile();
        if (profile.getRepository().isLoaded()) {
            Controllers.getDecorator().startWizard(new ModpackInstallWizardProvider(profile), I18n.i18n("install.modpack"));
        }
    }

    public static void refreshList() {
        Profiles.getSelectedProfile().getRepository().refreshVersionsAsync().start();
    }

    public void modifyGlobalGameSettings() {
        Versions.modifyGlobalSettings(Profiles.getSelectedProfile());
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    private class GameListSkin extends ToolbarListPageSkin<GameList> {

        public GameListSkin() {
            super(GameList.this);

            HBox hbox = new HBox(
                    createToolbarButton(I18n.i18n("install.new_game"), SVG::plus, GameList::addNewGame),
                    createToolbarButton(I18n.i18n("install.modpack"), SVG::importIcon, GameList::importModpack),
                    createToolbarButton(I18n.i18n("button.refresh"), SVG::refresh, GameList::refreshList),
                    createToolbarButton(I18n.i18n("settings.type.global.manage"), SVG::gear, GameList.this::modifyGlobalGameSettings)
            );
            hbox.setSpacing(0);
            hbox.setPickOnBounds(false);

            state.set(new State(I18n.i18n("version.manage"), hbox, true, false, true));
        }

        @Override
        protected List<Node> initializeToolbar(GameList skinnable) {
            return Collections.emptyList();
        }
    }
}
