package ru.spark.slauncher.ui;

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
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.construct.PopupMenu;
import ru.spark.slauncher.ui.construct.TwoLineListItem;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.ui.versions.Versions;
import ru.spark.slauncher.upgrade.RemoteVersion;
import ru.spark.slauncher.upgrade.UpdateChecker;
import ru.spark.slauncher.upgrade.UpdateHandler;
import ru.spark.slauncher.util.Analytics;

import static ru.spark.slauncher.ui.FXUtils.SINE;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class MainPage extends StackPane implements DecoratorPage {
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title", i18n("main_page"));

    private final PopupMenu menu = new PopupMenu();
    private final JFXPopup popup = new JFXPopup(menu);

    private final StringProperty currentGame = new SimpleStringProperty(this, "currentGame");
    private final BooleanProperty showUpdate = new SimpleBooleanProperty(this, "showUpdate");
    private final StringProperty latestVersion = new SimpleStringProperty(this, "latestVersion");
    private final ObservableList<Node> versions = FXCollections.observableArrayList();

    private StackPane updatePane;
    private JFXButton menuButton;

    {
        setPadding(new Insets(25));

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
                prompt.setSubtitle(i18n("update.bubble.subtitle"));
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
        launchPane.setMaxWidth(230);
        launchPane.setMaxHeight(55);
        StackPane.setAlignment(launchPane, Pos.BOTTOM_RIGHT);
        {
            JFXButton launchButton = new JFXButton();
            launchButton.setPrefWidth(230);
            launchButton.setPrefHeight(55);
            launchButton.setButtonType(JFXButton.ButtonType.RAISED);
            launchButton.getStyleClass().add("jfx-button-raised");
            launchButton.setOnMouseClicked(e -> launch());
            launchButton.setClip(new Rectangle(-100, -100, 310, 200));
            {
                VBox graphic = new VBox();
                graphic.setAlignment(Pos.CENTER);
                graphic.setTranslateX(-7);
                graphic.setMaxWidth(200);
                Label launchLabel = new Label(i18n("version.launch"));
                launchLabel.setStyle("-fx-font-size: 16px;");
                Label currentLabel = new Label();
                currentLabel.setStyle("-fx-font-size: 12px;");
                currentLabel.textProperty().bind(currentGameProperty());
                graphic.getChildren().setAll(launchLabel, currentLabel);

                launchButton.setGraphic(graphic);
            }

            Rectangle separator = new Rectangle();
            separator.getStyleClass().add("darker-fill");
            separator.setWidth(1);
            separator.setHeight(57);
            separator.setTranslateX(95);
            separator.setMouseTransparent(true);

            menuButton = new JFXButton();
            menuButton.setPrefHeight(55);
            menuButton.setPrefWidth(230);
            menuButton.setButtonType(JFXButton.ButtonType.RAISED);
            menuButton.getStyleClass().add("jfx-button-raised");
            menuButton.setStyle("-fx-font-size: 15px;");
            menuButton.setOnMouseClicked(e -> onMenu());
            menuButton.setClip(new Rectangle(211, -100, 100, 200));
            StackPane graphic = new StackPane();
            Node svg = SVG.triangle(Theme.whiteFillBinding(), 10, 10);
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
        Bindings.bindContent(menu.getContent(), versions);
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
        Analytics.recordLauncherUpgrade(target);
        UpdateHandler.updateFrom(target);
    }

    private void closeUpdateBubble() {
        showUpdate.unbind();
        showUpdate.set(false);
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    public String getCurrentGame() {
        return currentGame.get();
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame.set(currentGame);
    }

    public StringProperty currentGameProperty() {
        return currentGame;
    }

    public boolean isShowUpdate() {
        return showUpdate.get();
    }

    public void setShowUpdate(boolean showUpdate) {
        this.showUpdate.set(showUpdate);
    }

    public BooleanProperty showUpdateProperty() {
        return showUpdate;
    }

    public String getLatestVersion() {
        return latestVersion.get();
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion.set(latestVersion);
    }

    public StringProperty latestVersionProperty() {
        return latestVersion;
    }

    public ObservableList<Node> getVersions() {
        return versions;
    }
}
