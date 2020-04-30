package ru.spark.slauncher.ui.decorator;

import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import ru.spark.slauncher.ui.construct.Navigator;
import ru.spark.slauncher.ui.construct.PageCloseEvent;
import ru.spark.slauncher.ui.wizard.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DecoratorWizardDisplayer extends DecoratorTransitionPage implements TaskExecutorDialogWizardDisplayer {
    private final WizardController wizardController = new WizardController(this);
    private final Queue<Object> cancelQueue = new ConcurrentLinkedQueue<>();

    private final String category;

    public DecoratorWizardDisplayer(WizardProvider provider) {
        this(provider, null);
    }

    public DecoratorWizardDisplayer(WizardProvider provider, String category) {
        this.category = category;

        wizardController.setProvider(provider);
        wizardController.onStart();

        addEventHandler(Navigator.NavigationEvent.NAVIGATED, this::onDecoratorPageNavigating);
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
        navigate(page, nav.getAnimation().getAnimationProducer());

        String prefix = category == null ? "" : category + " - ";

        String title;
        if (page instanceof WizardPage)
            title = prefix + ((WizardPage) page).getTitle();
        else
            title = "";
        state.set(new State(title, null, true, refreshableProperty().get(), true));

        if (page instanceof Refreshable) {
            refreshableProperty().bind(((Refreshable) page).refreshableProperty());
        } else {
            refreshableProperty().unbind();
            refreshableProperty().set(false);
        }
    }

    @Override
    public boolean isPageCloseable() {
        return true;
    }

    @Override
    public void closePage() {
        wizardController.onCancel();
    }

    @Override
    public boolean back() {
        if (wizardController.canPrev()) {
            wizardController.onPrev(true);
            return false;
        } else
            return true;
    }

    @Override
    public void refresh() {
        ((Refreshable) getCurrentPage()).refresh();
    }

    @Override
    protected Skin createDefaultSkin() {
        return new Skin(this);
    }

    private static class Skin extends SkinBase<DecoratorWizardDisplayer> {

        protected Skin(DecoratorWizardDisplayer control) {
            super(control);

            getChildren().setAll(control.transitionPane);
        }
    }
}
