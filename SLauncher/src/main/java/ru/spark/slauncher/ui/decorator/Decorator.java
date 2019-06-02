package ru.spark.slauncher.ui.decorator;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Decorator extends Control {
    private final ListProperty<Node> drawer = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Node> content = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Node> container = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<Background> contentBackground = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty drawerTitle = new SimpleStringProperty();
    private final ObjectProperty<Runnable> onCloseButtonAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> onCloseNavButtonAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> onBackNavButtonAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> onRefreshNavButtonAction = new SimpleObjectProperty<>();
    private final BooleanProperty closeNavButtonVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty canRefresh = new SimpleBooleanProperty(false);
    private final BooleanProperty canBack = new SimpleBooleanProperty(false);
    private final BooleanProperty canClose = new SimpleBooleanProperty(false);
    private final BooleanProperty showCloseAsHome = new SimpleBooleanProperty(false);
    private final Stage primaryStage;
    private StackPane drawerWrapper;

    public Decorator(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.initStyle(StageStyle.UNDECORATED);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public StackPane getDrawerWrapper() {
        return drawerWrapper;
    }

    void setDrawerWrapper(StackPane drawerWrapper) {
        this.drawerWrapper = drawerWrapper;
    }

    public ObservableList<Node> getDrawer() {
        return drawer.get();
    }

    public void setDrawer(ObservableList<Node> drawer) {
        this.drawer.set(drawer);
    }

    public ListProperty<Node> drawerProperty() {
        return drawer;
    }

    public ObservableList<Node> getContent() {
        return content.get();
    }

    public void setContent(ObservableList<Node> content) {
        this.content.set(content);
    }

    public ListProperty<Node> contentProperty() {
        return content;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getDrawerTitle() {
        return drawerTitle.get();
    }

    public void setDrawerTitle(String drawerTitle) {
        this.drawerTitle.set(drawerTitle);
    }

    public StringProperty drawerTitleProperty() {
        return drawerTitle;
    }

    public Runnable getOnCloseButtonAction() {
        return onCloseButtonAction.get();
    }

    public void setOnCloseButtonAction(Runnable onCloseButtonAction) {
        this.onCloseButtonAction.set(onCloseButtonAction);
    }

    public ObjectProperty<Runnable> onCloseButtonActionProperty() {
        return onCloseButtonAction;
    }

    public boolean isCloseNavButtonVisible() {
        return closeNavButtonVisible.get();
    }

    public void setCloseNavButtonVisible(boolean closeNavButtonVisible) {
        this.closeNavButtonVisible.set(closeNavButtonVisible);
    }

    public BooleanProperty closeNavButtonVisibleProperty() {
        return closeNavButtonVisible;
    }

    public ObservableList<Node> getContainer() {
        return container.get();
    }

    public void setContainer(ObservableList<Node> container) {
        this.container.set(container);
    }

    public ListProperty<Node> containerProperty() {
        return container;
    }

    public Background getContentBackground() {
        return contentBackground.get();
    }

    public void setContentBackground(Background contentBackground) {
        this.contentBackground.set(contentBackground);
    }

    public ObjectProperty<Background> contentBackgroundProperty() {
        return contentBackground;
    }

    public BooleanProperty canRefreshProperty() {
        return canRefresh;
    }

    public BooleanProperty canBackProperty() {
        return canBack;
    }

    public BooleanProperty canCloseProperty() {
        return canClose;
    }

    public BooleanProperty showCloseAsHomeProperty() {
        return showCloseAsHome;
    }

    public ObjectProperty<EventHandler<ActionEvent>> onBackNavButtonActionProperty() {
        return onBackNavButtonAction;
    }

    public ObjectProperty<EventHandler<ActionEvent>> onCloseNavButtonActionProperty() {
        return onCloseNavButtonAction;
    }

    public ObjectProperty<EventHandler<ActionEvent>> onRefreshNavButtonActionProperty() {
        return onRefreshNavButtonAction;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DecoratorSkin(this);
    }

    public void minimize() {
        primaryStage.setIconified(true);
    }

    public void close() {
        onCloseButtonAction.get().run();
    }
}
