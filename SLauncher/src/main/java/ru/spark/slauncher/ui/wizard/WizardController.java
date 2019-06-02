package ru.spark.slauncher.ui.wizard;

import javafx.scene.Node;
import ru.spark.slauncher.task.Task;

import java.util.*;

public class WizardController implements Navigation {
    private final WizardDisplayer displayer;
    private final Map<String, Object> settings = new HashMap<>();
    private final Stack<Node> pages = new Stack<>();
    private WizardProvider provider = null;

    public WizardController(WizardDisplayer displayer) {
        this.displayer = displayer;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public WizardDisplayer getDisplayer() {
        return displayer;
    }

    public void setProvider(WizardProvider provider) {
        this.provider = provider;
    }

    public List<Node> getPages() {
        return Collections.unmodifiableList(pages);
    }

    @Override
    public void onStart() {
        Objects.requireNonNull(provider);

        settings.clear();
        provider.start(settings);

        pages.clear();
        Node page = navigatingTo(0);
        pages.push(page);

        if (page instanceof WizardPage)
            ((WizardPage) page).onNavigate(settings);

        displayer.onStart();
        displayer.navigateTo(page, NavigationDirection.START);
    }

    @Override
    public void onNext() {
        onNext(navigatingTo(pages.size()));
    }

    public void onNext(Node page) {
        pages.push(page);

        if (page instanceof WizardPage)
            ((WizardPage) page).onNavigate(settings);

        displayer.navigateTo(page, NavigationDirection.NEXT);
    }

    @Override
    public void onPrev(boolean cleanUp) {
        if (!canPrev()) {
            if (provider.cancelIfCannotGoBack()) {
                onCancel();
                return;
            } else {
                throw new IllegalStateException("Cannot go backward since this is the back page. Pages: " + pages);
            }
        }

        Node page = pages.pop();
        if (cleanUp && page instanceof WizardPage)
            ((WizardPage) page).cleanup(settings);

        Node prevPage = pages.peek();
        if (prevPage instanceof WizardPage)
            ((WizardPage) prevPage).onNavigate(settings);

        displayer.navigateTo(prevPage, NavigationDirection.PREVIOUS);
    }

    @Override
    public boolean canPrev() {
        return pages.size() > 1;
    }

    @Override
    public void onFinish() {
        Object result = provider.finish(settings);
        if (result instanceof Summary)
            displayer.navigateTo(((Summary) result).getComponent(), NavigationDirection.NEXT);
        else if (result instanceof Task) displayer.handleTask(settings, ((Task) result));
        else if (result != null) throw new IllegalStateException("Unrecognized wizard result: " + result);
    }

    @Override
    public void onEnd() {
        settings.clear();
        pages.clear();
        displayer.onEnd();
    }

    @Override
    public void onCancel() {
        displayer.onCancel();
        onEnd();
    }

    protected Node navigatingTo(int step) {
        return provider.createPage(this, step, settings);
    }
}
