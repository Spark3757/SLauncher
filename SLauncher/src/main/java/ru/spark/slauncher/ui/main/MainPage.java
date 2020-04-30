package ru.spark.slauncher.ui.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.construct.PopupMenu;
import ru.spark.slauncher.ui.construct.TwoLineListItem;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.ui.versions.GameItem;
import ru.spark.slauncher.ui.versions.Versions;
import ru.spark.slauncher.upgrade.RemoteVersion;
import ru.spark.slauncher.upgrade.UpdateChecker;
import ru.spark.slauncher.upgrade.UpdateHandler;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.javafx.MappedObservableList;

import java.util.List;
import java.util.stream.IntStream;

import static ru.spark.slauncher.ui.FXUtils.SINE;

public final class MainPage extends StackPane implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.fromTitle("SLauncher " + Metadata.VERSION));

    private final PopupMenu menu = new PopupMenu();
    private final JFXPopup popup = new JFXPopup(menu);

    private final StringProperty currentGame = new SimpleStringProperty(this, "currentGame");
    private final BooleanProperty showUpdate = new SimpleBooleanProperty(this, "showUpdate");
    private final StringProperty latestVersion = new SimpleStringProperty(this, "latestVersion");
    private final ObservableList<Version> versions = FXCollections.observableArrayList();
    private final ObservableList<Node> versionNodes;
    private Profile profile;

    private StackPane updatePane;
    private JFXButton menuButton;

    {
        setPadding(new Insets(20));

        updatePane = new StackPane();
        updatePane.setVisible(false);
        updatePane.getStyleClass().add("bubble");
        FXUtils.setLimitWidth(updatePane, 230);
        FXUtils.setLimitHeight(updatePane, 55);
        StackPane.setAlignment(updatePane, Pos.TOP_RIGHT);
        updatePane.setOnMouseClicked(e -> onUpgrade());
        FXUtils.onChange(showUpdateProperty(), this::doAnimation);

        {
            HBox hBox = new HBox();
            hBox.setSpacing(12);
            hBox.setAlignment(Pos.CENTER_LEFT);
            StackPane.setAlignment(hBox, Pos.CENTER_LEFT);
            StackPane.setMargin(hBox, new Insets(9, 12, 9, 16));
            {
                Label lblIcon = new Label();
                lblIcon.setGraphic(SVG.update(Theme.whiteFillBinding(), 20, 20));

                TwoLineListItem prompt = new TwoLineListItem();
                prompt.setSubtitle(I18n.i18n("update.bubble.subtitle"));
                prompt.setPickOnBounds(false);
                prompt.titleProperty().bind(latestVersionProperty());

                hBox.getChildren().setAll(lblIcon, prompt);
            }

            JFXButton closeUpdateButton = new JFXButton();
            closeUpdateButton.setGraphic(SVG.close(Theme.whiteFillBinding(), 10, 10));
            StackPane.setAlignment(closeUpdateButton, Pos.TOP_RIGHT);
            closeUpdateButton.getStyleClass().add("toggle-icon-tiny");
            StackPane.setMargin(closeUpdateButton, new Insets(5));
            closeUpdateButton.setOnMouseClicked(e -> closeUpdateBubble());

            updatePane.getChildren().setAll(hBox, closeUpdateButton);
        }

        StackPane launchPane = new StackPane();
        launchPane.getStyleClass().add("launch-pane");
        launchPane.setMaxWidth(230);
        launchPane.setMaxHeight(55);
        launchPane.setOnScroll(event -> {
            int index = IntStream.range(0, versions.size())
                    .filter(i -> versions.get(i).getId().equals(getCurrentGame()))
                    .findFirst().orElse(-1);
            if (index < 0) return;
            if (event.getDeltaY() > 0) {
                index--;
            } else {
                index++;
            }
            profile.setSelectedVersion(versions.get((index + versions.size()) % versions.size()).getId());
        });
        StackPane.setAlignment(launchPane, Pos.BOTTOM_RIGHT);
        {
            JFXButton launchButton = new JFXButton();
            launchButton.setPrefWidth(230);
            launchButton.setPrefHeight(55);
            //launchButton.setButtonType(JFXButton.ButtonType.RAISED);
            launchButton.setOnAction(e -> launch());
            launchButton.setDefaultButton(true);
            launchButton.setClip(new Rectangle(-100, -100, 310, 200));
            {
                VBox graphic = new VBox();
                graphic.setAlignment(Pos.CENTER);
                graphic.setTranslateX(-7);
                graphic.setMaxWidth(200);
                Label launchLabel = new Label(I18n.i18n("version.launch"));
                launchLabel.setStyle("-fx-font-size: 16px;");
                Label currentLabel = new Label();
                currentLabel.setStyle("-fx-font-size: 12px;");
                currentLabel.textProperty().bind(Bindings.createStringBinding(() -> {
                    if (getCurrentGame() == null) {
                        return I18n.i18n("version.empty");
                    } else {
                        return getCurrentGame();
                    }
                }, currentGameProperty()));
                graphic.getChildren().setAll(launchLabel, currentLabel);

                launchButton.setGraphic(graphic);
            }

            Rectangle separator = new Rectangle();
            separator.setWidth(1);
            separator.setHeight(57);
            separator.setTranslateX(95);
            separator.setMouseTransparent(true);

            menuButton = new JFXButton();
            menuButton.setPrefHeight(55);
            menuButton.setPrefWidth(230);
            //menuButton.setButtonType(JFXButton.ButtonType.RAISED);
            menuButton.setStyle("-fx-font-size: 15px;");
            menuButton.setOnMouseClicked(e -> onMenu());
            menuButton.setClip(new Rectangle(211, -100, 100, 200));
            StackPane graphic = new StackPane();
            Node svg = SVG.triangle(Theme.foregroundFillBinding(), 10, 10);
            StackPane.setAlignment(svg, Pos.CENTER_RIGHT);
            graphic.getChildren().setAll(svg);
            graphic.setTranslateX(12);
            menuButton.setGraphic(graphic);

            launchPane.getChildren().setAll(launchButton, separator, menuButton);
        }

        getChildren().setAll(updatePane, launchPane);

        menu.setMaxHeight(365);
        menu.setMaxWidth(545);
        menu.setAlwaysShowingVBar(true);
        menu.setOnMouseClicked(e -> popup.hide());
        versionNodes = MappedObservableList.create(versions, version -> {
            Node node = PopupMenu.wrapPopupMenuItem(new GameItem(profile, version.getId()));
            node.setOnMouseClicked(e -> profile.setSelectedVersion(version.getId()));
            return node;
        });
        Bindings.bindContent(menu.getContent(), versionNodes);
    }

    private void doAnimation(boolean show) {
        Duration duration = Duration.millis(320);
        Timeline nowAnimation = new Timeline();
        nowAnimation.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(updatePane.translateXProperty(), show ? 260 : 0, SINE)),
                new KeyFrame(duration,
                        new KeyValue(updatePane.translateXProperty(), show ? 0 : 260, SINE)));
        if (show) nowAnimation.getKeyFrames().add(
                new KeyFrame(Duration.ZERO, e -> updatePane.setVisible(true)));
        else nowAnimation.getKeyFrames().add(
                new KeyFrame(duration, e -> updatePane.setVisible(false)));
        nowAnimation.play();
    }

    private void launch() {
        Profile profile = Profiles.getSelectedProfile();
        Versions.launch(profile, profile.getSelectedVersion());
    }

    private void onMenu() {
        popup.show(menuButton, JFXPopup.PopupVPosition.BOTTOM, JFXPopup.PopupHPosition.RIGHT, 0, -menuButton.getHeight());
    }

    private void onUpgrade() {
        RemoteVersion target = UpdateChecker.getLatestVersion();
        if (target == null) {
            return;
        }
        UpdateHandler.updateFrom(target);
    }

    private void closeUpdateBubble() {
        showUpdate.unbind();
        showUpdate.set(false);
    }

    @Override
    public ReadOnlyObjectWrapper<State> stateProperty() {
        return state;
    }

    public String getCurrentGame() {
        return currentGame.get();
    }

    public StringProperty currentGameProperty() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame.set(currentGame);
    }

    public boolean isShowUpdate() {
        return showUpdate.get();
    }

    public BooleanProperty showUpdateProperty() {
        return showUpdate;
    }

    public void setShowUpdate(boolean showUpdate) {
        this.showUpdate.set(showUpdate);
    }

    public String getLatestVersion() {
        return latestVersion.get();
    }

    public StringProperty latestVersionProperty() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion.set(latestVersion);
    }

    public void initVersions(Profile profile, List<Version> versions) {
        FXUtils.checkFxUserThread();
        this.profile = profile;
        this.versions.setAll(versions);
    }
}
