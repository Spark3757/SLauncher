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
    private final StringProperty subtitle = new SimpleStringProperty(this, "subtitle");
    private final BooleanProperty actionButtonVisible = new SimpleBooleanProperty(this, "actionButtonVisible", true);
    private ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<EventHandler<ActionEvent>>(this, "onAction") {
        @Override
        protected void invalidated() {
            setEventHandler(ActionEvent.ACTION, get());
        }
    };

    public AdvancedListItem() {
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> fireEvent(new ActionEvent()));
    }

    public Image getImage() {
        return image.get();
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public Node getRightGraphic() {
        return rightGraphic.get();
    }

    public void setRightGraphic(Node rightGraphic) {
        this.rightGraphic.set(rightGraphic);
    }

    public ObjectProperty<Node> rightGraphicProperty() {
        return rightGraphic;
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

    public String getSubtitle() {
        return subtitle.get();
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.set(subtitle);
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public boolean isActionButtonVisible() {
        return actionButtonVisible.get();
    }

    public void setActionButtonVisible(boolean actionButtonVisible) {
        this.actionButtonVisible.set(actionButtonVisible);
    }

    public BooleanProperty actionButtonVisibleProperty() {
        return actionButtonVisible;
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onActionProperty().get();
    }

    public final void setOnAction(EventHandler<ActionEvent> value) {
        onActionProperty().set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AdvancedListItemSkin(this);
    }
}
