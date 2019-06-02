package ru.spark.slauncher.ui.construct;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TwoLineListItem extends VBox {
    private static final String DEFAULT_STYLE_CLASS = "two-line-list-item";

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty subtitle = new SimpleStringProperty(this, "subtitle");

    public TwoLineListItem(String titleString, String subtitleString) {
        this();

        title.set(titleString);
        subtitle.set(subtitleString);
    }

    public TwoLineListItem() {
        setMouseTransparent(true);
        Label lblTitle = new Label();
        lblTitle.getStyleClass().add("title");
        lblTitle.textProperty().bind(title);

        Label lblSubtitle = new Label();
        lblSubtitle.getStyleClass().add("subtitle");
        lblSubtitle.textProperty().bind(subtitle);

        getChildren().setAll(lblTitle, lblSubtitle);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
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

    @Override
    public String toString() {
        return getTitle();
    }
}
