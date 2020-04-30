package ru.spark.slauncher.ui.decorator;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;
import ru.spark.slauncher.ui.animation.ContainerAnimations;
import ru.spark.slauncher.ui.construct.Navigator;
import ru.spark.slauncher.ui.construct.TabControl;
import ru.spark.slauncher.ui.construct.TabHeader;

public abstract class DecoratorTabPage extends DecoratorTransitionPage implements TabControl {

    public DecoratorTabPage() {
        getSelectionModel().selectedItemProperty().addListener((a, b, newValue) -> {
            if (newValue.getNode() == null && newValue.getNodeSupplier() != null) {
                newValue.setNode(newValue.getNodeSupplier().get());
            }
            if (newValue.getNode() != null) {
                onNavigating(getCurrentPage());
                if (getCurrentPage() != null)
                    getCurrentPage().fireEvent(new Navigator.NavigationEvent(null, getCurrentPage(), Navigator.NavigationEvent.NAVIGATING));
                navigate(newValue.getNode(), ContainerAnimations.FADE.getAnimationProducer());
                onNavigated(getCurrentPage());
                if (getCurrentPage() != null)
                    getCurrentPage().fireEvent(new Navigator.NavigationEvent(null, getCurrentPage(), Navigator.NavigationEvent.NAVIGATED));
            }
        });
    }

    public DecoratorTabPage(TabHeader.Tab... tabs) {
        this();
        if (tabs != null) {
            getTabs().addAll(tabs);
        }
    }

    private ObservableList<TabHeader.Tab> tabs = FXCollections.observableArrayList();

    @Override
    public ObservableList<TabHeader.Tab> getTabs() {
        return tabs;
    }

    private final ObjectProperty<SingleSelectionModel<TabHeader.Tab>> selectionModel = new SimpleObjectProperty<>(this, "selectionModel", new TabControl.TabControlSelectionModel(this));

    public SingleSelectionModel<TabHeader.Tab> getSelectionModel() {
        return selectionModel.get();
    }

    public ObjectProperty<SingleSelectionModel<TabHeader.Tab>> selectionModelProperty() {
        return selectionModel;
    }

    public void setSelectionModel(SingleSelectionModel<TabHeader.Tab> selectionModel) {
        this.selectionModel.set(selectionModel);
    }
}
