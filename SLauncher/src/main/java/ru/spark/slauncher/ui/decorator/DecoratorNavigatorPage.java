package ru.spark.slauncher.ui.decorator;

import javafx.scene.Node;
import ru.spark.slauncher.ui.animation.AnimationProducer;
import ru.spark.slauncher.ui.construct.Navigator;

public abstract class DecoratorNavigatorPage extends DecoratorTransitionPage {
    protected final Navigator navigator = new Navigator();

    {
        this.navigator.setOnNavigating(this::onNavigating);
        this.navigator.setOnNavigated(this::onNavigated);
        backableProperty().bind(navigator.backableProperty());
    }

    @Override
    protected void navigate(Node page, AnimationProducer animationProducer) {
        navigator.navigate(page, animationProducer);
    }

    @Override
    public boolean back() {
        if (navigator.canGoBack()) {
            navigator.close();
            return false;
        } else {
            return true;
        }
    }

    private void onNavigating(Navigator.NavigationEvent event) {
        if (event.getSource() != this.navigator) return;
        onNavigating(event.getNode());
    }

    private void onNavigated(Navigator.NavigationEvent event) {
        if (event.getSource() != this.navigator) return;
        onNavigated(event.getNode());
    }
}
