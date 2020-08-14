package ru.spark.slauncher.ui.construct;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.util.StringUtils;

public class TwoLineListItem extends VBox {
    private static final String DEFAULT_STYLE_CLASS = "two-line-list-item";

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty tag = new SimpleStringProperty(this, "tag");
    private final StringProperty subtitle = new SimpleStringProperty(this, "subtitle");

    public TwoLineListItem(String titleString, String subtitleString) {
        this();

        title.set(titleString);
        subtitle.set(subtitleString);
    }

    public TwoLineListItem() {
        setMouseTransparent(true);

        HBox firstLine = new HBox();

        Label lblTitle = new Label();
        lblTitle.getStyleClass().add("title");
        lblTitle.textProperty().bind(title);

        Label lblTag = new Label();
        lblTag.getStyleClass().add("tag");
        lblTag.textProperty().bind(tag);

        lblTag.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> StringUtils.isNotBlank(tag.getValue()),
                tag));

        firstLine.getChildren().addAll(lblTitle, lblTag);

        Label lblSubtitle = new Label();
        lblSubtitle.getStyleClass().add("subtitle");
        lblSubtitle.textProperty().bind(subtitle);

        getChildren().setAll(firstLine, lblSubtitle);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
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

    public String getSubtitle() {
        return subtitle.get();
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.set(subtitle);
    }

    public String getTag() {
        return tag.get();
    }

    public StringProperty tagProperty() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag.set(tag);
    }


    @Override
    public String toString() {
        return getTitle();
    }
}
