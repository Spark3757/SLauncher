package ru.spark.slauncher.ui.construct;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

public class AdvancedListItem extends Control {
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");
    private final ObjectProperty<Node> rightGraphic = new SimpleObjectProperty<>(this, "rightGraphic");
    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final BooleanProperty active = new SimpleBooleanProperty(this, "active");
    private final StringProperty subtitle = new SimpleStringProperty(this, "subtitle");
    private final BooleanProperty actionButtonVisible = new SimpleBooleanProperty(this, "actionButtonVisible", true);

    public AdvancedListItem() {
        getStyleClass().add("advanced-list-item");
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> fireEvent(new ActionEvent()));
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    public Node getRightGraphic() {
        return rightGraphic.get();
    }

    public ObjectProperty<Node> rightGraphicProperty() {
        return rightGraphic;
    }

    public void setRightGraphic(Node rightGraphic) {
        this.rightGraphic.set(rightGraphic);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public String getSubtitle() {
        return subtitle.get();
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.set(subtitle);
    }

    public boolean isActionButtonVisible() {
        return actionButtonVisible.get();
    }

    public BooleanProperty actionButtonVisibleProperty() {
        return actionButtonVisible;
    }

    public void setActionButtonVisible(boolean actionButtonVisible) {
        this.actionButtonVisible.set(actionButtonVisible);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public final void setOnAction(EventHandler<ActionEvent> value) {
        onActionProperty().set(value);
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onActionProperty().get();
    }

    private ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<EventHandler<ActionEvent>>(this, "onAction") {
        @Override
        protected void invalidated() {
            setEventHandler(ActionEvent.ACTION, get());
        }
    };

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AdvancedListItemSkin(this);
    }
}
