package ru.spark.slauncher.ui.decorator;

import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.construct.Navigator;
import ru.spark.slauncher.ui.construct.PageCloseEvent;
import ru.spark.slauncher.ui.wizard.*;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DecoratorWizardDisplayer extends DecoratorTransitionPage implements WizardDisplayer {
    private final WizardController wizardController = new WizardController(this);
    private final WizardDisplayer displayer = new TaskExecutorDialogWizardDisplayer(new ConcurrentLinkedQueue<>()) {
        @Override
        public void onEnd() {
            super.onEnd();
            fireEvent(new PageCloseEvent());
        }

        @Override
        public void navigateTo(Node page, Navigation.NavigationDirection nav) {
        }
    };

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
    public void onStart() {
        displayer.onStart();
    }

    @Override
    public void onCancel() {
        displayer.onCancel();
    }

    @Override
    public void onEnd() {
        displayer.onEnd();
    }

    @Override
    public void navigateTo(Node page, Navigation.NavigationDirection nav) {
        displayer.navigateTo(page, nav);
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
    public void handleTask(Map<String, Object> settings, Task<?> task) {
        displayer.handleTask(settings, task);
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