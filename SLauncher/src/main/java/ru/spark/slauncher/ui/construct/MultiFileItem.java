package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.NamedArg;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class MultiFileItem<T> extends ComponentList {
    private final StringProperty customTitle = new SimpleStringProperty(this, "customTitle", i18n("selector.custom"));
    private final StringProperty chooserTitle = new SimpleStringProperty(this, "chooserTitle", i18n("selector.choose_file"));
    private final BooleanProperty directory = new SimpleBooleanProperty(this, "directory", false);
    private final ObjectProperty<T> selectedData = new SimpleObjectProperty<>(this, "selectedData");
    private final ObjectProperty<T> fallbackData = new SimpleObjectProperty<>(this, "fallbackData");
    private final ObservableList<FileChooser.ExtensionFilter> extensionFilters = FXCollections.observableArrayList();

    private final ToggleGroup group = new ToggleGroup();
    private final JFXTextField txtCustom = new JFXTextField();
    private final JFXButton btnSelect = new JFXButton();
    private final JFXRadioButton radioCustom = new JFXRadioButton();
    private final BorderPane custom = new BorderPane();
    private final VBox pane = new VBox();
    private final boolean hasCustom;

    private Consumer<Toggle> toggleSelectedListener;

    @SuppressWarnings("unchecked")
    public MultiFileItem(@NamedArg(value = "hasCustom", defaultValue = "true") boolean hasCustom) {
        this.hasCustom = hasCustom;

        BorderPane.setAlignment(txtCustom, Pos.CENTER_RIGHT);

        btnSelect.setGraphic(SVG.folderOpen(Theme.blackFillBinding(), 15, 15));
        btnSelect.setOnMouseClicked(e -> {
            if (isDirectory()) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.titleProperty().bind(chooserTitle);
                File dir = chooser.showDialog(Controllers.getStage());
                if (dir != null)
                    txtCustom.setText(dir.getAbsolutePath());
            } else {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().addAll(getExtensionFilters());
                chooser.titleProperty().bind(chooserTitle);
                File file = chooser.showOpenDialog(Controllers.getStage());
                if (file != null)
                    txtCustom.setText(file.getAbsolutePath());
            }
        });

        radioCustom.textProperty().bind(customTitleProperty());
        radioCustom.setToggleGroup(group);
        txtCustom.disableProperty().bind(radioCustom.selectedProperty().not());
        btnSelect.disableProperty().bind(radioCustom.selectedProperty().not());

        custom.setLeft(radioCustom);
        custom.setStyle("-fx-padding: 3;");
        HBox right = new HBox();
        right.setSpacing(3);
        right.getChildren().addAll(txtCustom, btnSelect);
        custom.setRight(right);
        FXUtils.setLimitHeight(custom, 20);

        pane.setStyle("-fx-padding: 0 0 10 0;");
        pane.setSpacing(8);

        if (hasCustom)
            pane.getChildren().add(custom);
        getContent().add(pane);

        group.selectedToggleProperty().addListener((a, b, newValue) -> {
            if (toggleSelectedListener != null)
                toggleSelectedListener.accept(newValue);

            selectedData.set((T) newValue.getUserData());
        });
        selectedData.addListener((a, b, newValue) -> {
            Optional<Toggle> selecting = group.getToggles().stream()
                    .filter(it -> it.getUserData() == newValue)
                    .findFirst();
            if (!selecting.isPresent()) {
                selecting = group.getToggles().stream()
                        .filter(it -> it.getUserData() == getFallbackData())
                        .findFirst();
            }

            selecting.ifPresent(toggle -> toggle.setSelected(true));
        });
    }

    public Node createChildren(String title) {
        return createChildren(title, null);
    }

    public Node createChildren(String title, T userData) {
        return createChildren(title, "", userData);
    }

    public Node createChildren(String title, String subtitle, T userData) {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(3));
        FXUtils.setLimitHeight(pane, 20);

        JFXRadioButton left = new JFXRadioButton(title);
        left.setToggleGroup(group);
        left.setUserData(userData);
        pane.setLeft(left);

        Label right = new Label(subtitle);
        right.getStyleClass().add("subtitle-label");
        right.setStyle("-fx-font-size: 10;");
        pane.setRight(right);

        return pane;
    }

    public void loadChildren(Collection<Node> list) {
        pane.getChildren().setAll(list);

        if (hasCustom)
            pane.getChildren().add(custom);
    }

    public void loadChildren(Collection<Node> list, T customUserData) {
        loadChildren(list);
        setCustomUserData(customUserData);
    }

    public ToggleGroup getGroup() {
        return group;
    }

    public String getCustomTitle() {
        return customTitle.get();
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle.set(customTitle);
    }

    public StringProperty customTitleProperty() {
        return customTitle;
    }

    public String getChooserTitle() {
        return chooserTitle.get();
    }

    public void setChooserTitle(String chooserTitle) {
        this.chooserTitle.set(chooserTitle);
    }

    public StringProperty chooserTitleProperty() {
        return chooserTitle;
    }

    public void setCustomUserData(T userData) {
        radioCustom.setUserData(userData);
    }

    public boolean isCustomToggle(Toggle toggle) {
        return radioCustom == toggle;
    }

    public void setToggleSelectedListener(Consumer<Toggle> consumer) {
        toggleSelectedListener = consumer;
    }

    public StringProperty customTextProperty() {
        return txtCustom.textProperty();
    }

    public String getCustomText() {
        return txtCustom.getText();
    }

    public void setCustomText(String customText) {
        txtCustom.setText(customText);
    }

    public JFXTextField getTxtCustom() {
        return txtCustom;
    }

    public boolean isDirectory() {
        return directory.get();
    }

    public void setDirectory(boolean directory) {
        this.directory.set(directory);
    }

    public BooleanProperty directoryProperty() {
        return directory;
    }

    public ObservableList<FileChooser.ExtensionFilter> getExtensionFilters() {
        return extensionFilters;
    }

    public T getSelectedData() {
        return selectedData.get();
    }

    public void setSelectedData(T selectedData) {
        this.selectedData.set(selectedData);
    }

    public ObjectProperty<T> selectedDataProperty() {
        return selectedData;
    }

    public T getFallbackData() {
        return fallbackData.get();
    }

    public void setFallbackData(T fallbackData) {
        this.fallbackData.set(fallbackData);
    }

    public ObjectProperty<T> fallbackDataProperty() {
        return fallbackData;
    }
}
