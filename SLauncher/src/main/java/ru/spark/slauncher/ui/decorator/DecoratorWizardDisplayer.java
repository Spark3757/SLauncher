package ru.spark.slauncher.ui.decorator;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.ui.animation.TransitionHandler;
import ru.spark.slauncher.ui.construct.PageCloseEvent;
import ru.spark.slauncher.ui.wizard.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DecoratorWizardDisplayer extends StackPane implements TaskExecutorDialogWizardDisplayer, Refreshable, DecoratorPage {
    private final StringProperty title = new SimpleStringProperty();
    private final BooleanProperty canRefresh = new SimpleBooleanProperty();

    private final TransitionHandler transitionHandler = new TransitionHandler(this);
    private final WizardController wizardController = new WizardController(this);
    private final Queue<Object> cancelQueue = new ConcurrentLinkedQueue<>();

    private final String category;

    private Node nowPage;

    public DecoratorWizardDisplayer(WizardProvider provider) {
        this(provider, null);
    }

    public DecoratorWizardDisplayer(WizardProvider provider, String category) {
        this.category = category;

        wizardController.setProvider(provider);
        wizardController.onStart();

        getStyleClass().add("white-background");
    }

    @Override
    public StringProperty titleProperty() {
        return title;
    }

    @Override
    public BooleanProperty canRefreshProperty() {
        return canRefresh;
    }

    @Override
    public WizardController getWizardController() {
        return wizardController;
    }

    @Override
    public Queue<Object> getCancelQueue() {
        return cancelQueue;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onEnd() {
        fireEvent(new PageCloseEvent());
    }

    @Override
    public void navigateTo(Node page, Navigation.NavigationDirection nav) {
        nowPage = page;

        transitionHandler.setContent(page, nav.getAnimation().getAnimationProducer());

        canRefresh.set(page instanceof Refreshable);

        String prefix = category == null ? "" : category + " - ";

        if (page instanceof WizardPage)
            title.set(prefix + ((WizardPage) page).getTitle());
    }

    @Override
    public boolean canForceToClose() {
        return true;
    }

    @Override
    public void onForceToClose() {
        wizardController.onCancel();
    }

    @Override
    public boolean onClose() {
        if (wizardController.canPrev()) {
            wizardController.onPrev(true);
            return false;
        } else
            return true;
    }

    @Override
    public void refresh() {
        ((Refreshable) nowPage).refresh();
    }
}
