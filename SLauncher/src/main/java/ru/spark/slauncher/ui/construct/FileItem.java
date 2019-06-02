package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class FileItem extends BorderPane {
    private final Label lblPath = new Label();

    private final SimpleStringProperty name = new SimpleStringProperty(this, "name");
    private final SimpleStringProperty title = new SimpleStringProperty(this, "title");
    private final SimpleStringProperty tooltip = new SimpleStringProperty(this, "tooltip");
    private final SimpleStringProperty path = new SimpleStringProperty(this, "path");
    private final SimpleBooleanProperty convertToRelativePath = new SimpleBooleanProperty(this, "convertToRelativePath");

    public FileItem() {
        VBox left = new VBox();
        Label name = new Label();
        name.textProperty().bind(nameProperty());
        lblPath.getStyleClass().addAll("subtitle-label");
        lblPath.textProperty().bind(path);
        left.getChildren().addAll(name, lblPath);
        setLeft(left);

        JFXButton right = new JFXButton();
        right.setGraphic(SVG.pencil(Theme.blackFillBinding(), 15, 15));
        right.getStyleClass().add("toggle-icon4");
        right.setOnMouseClicked(e -> onExplore());
        FXUtils.installFastTooltip(right, i18n("button.edit"));
        setRight(right);

        Tooltip tip = new Tooltip();
        tip.textProperty().bind(tooltipProperty());
        Tooltip.install(this, tip);

        convertToRelativePath.addListener(FXUtils.onInvalidating(() -> path.set(processPath(path.get()))));
    }

    /**
     * Converts the given path to absolute/relative(if possible) path according to {@link #convertToRelativePathProperty()}.
     */
    private String processPath(String path) {
        Path given = Paths.get(path).toAbsolutePath();
        if (isConvertToRelativePath()) {
            try {
                return Paths.get(".").normalize().toAbsolutePath().relativize(given).normalize().toString();
            } catch (IllegalArgumentException e) {
                // the given path can't be relativized against current path
            }
        }
        return given.normalize().toString();
    }

    public void onExplore() {
        DirectoryChooser chooser = new DirectoryChooser();
        if (path.get() != null) {
            File file = new File(path.get());
            if (file.exists()) {
                if (file.isFile())
                    file = file.getAbsoluteFile().getParentFile();
                else if (file.isDirectory())
                    file = file.getAbsoluteFile();
                chooser.setInitialDirectory(file);
            }
        }
        chooser.titleProperty().bind(titleProperty());
        File selectedDir = chooser.showDialog(Controllers.getStage());
        if (selectedDir != null) {
            path.set(processPath(selectedDir.toString()));
        }
        chooser.titleProperty().unbind();
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
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

    public String getTooltip() {
        return tooltip.get();
    }

    public void setTooltip(String tooltip) {
        this.tooltip.set(tooltip);
    }

    public StringProperty tooltipProperty() {
        return tooltip;
    }

    public String getPath() {
        return path.get();
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public StringProperty pathProperty() {
        return path;
    }

    public boolean isConvertToRelativePath() {
        return convertToRelativePath.get();
    }

    public void setConvertToRelativePath(boolean convertToRelativePath) {
        this.convertToRelativePath.set(convertToRelativePath);
    }

    public BooleanProperty convertToRelativePathProperty() {
        return convertToRelativePath;
    }
}
