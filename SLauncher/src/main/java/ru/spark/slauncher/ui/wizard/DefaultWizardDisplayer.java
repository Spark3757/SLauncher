package ru.spark.slauncher.ui.wizard;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToolbar;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.animation.TransitionHandler;
import ru.spark.slauncher.util.StringUtils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultWizardDisplayer extends StackPane implements AbstractWizardDisplayer {

    private final String prefix;
    private final WizardController wizardController;
    private final Queue<Object> cancelQueue = new ConcurrentLinkedQueue<>();

    private Node nowPage;

    private TransitionHandler transitionHandler;

    @FXML
    private StackPane root;
    @FXML
    private JFXButton backButton;
    @FXML
    private JFXToolbar toolbar;
    @FXML
    private JFXButton refreshButton;
    @FXML
    private Label titleLabel;

    public DefaultWizardDisplayer(String prefix, WizardProvider wizardProvider) {
        this.prefix = prefix;

        FXUtils.loadFXML(this, "/assets/fxml/wizard.fxml");
        toolbar.setEffect(null);

        wizardController = new WizardController(this);
        wizardController.setProvider(wizardProvider);
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
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void navigateTo(Node page, Navigation.NavigationDirection nav) {
        backButton.setDisable(!wizardController.canPrev());
        transitionHandler.setContent(page, nav.getAnimation().getAnimationProducer());
        String title = StringUtils.isBlank(prefix) ? "" : prefix + " - ";
        if (page instanceof WizardPage)
            titleLabel.setText(title + ((WizardPage) page).getTitle());
        refreshButton.setVisible(page instanceof Refreshable);
        nowPage = page;
    }

    @FXML
    private void initialize() {
        transitionHandler = new TransitionHandler(root);
        wizardController.onStart();
    }

    @FXML
    private void back() {
        wizardController.onPrev(true);
    }

    @FXML
    private void close() {
        wizardController.onCancel();
    }

    @FXML
    private void refresh() {
        ((Refreshable) nowPage).refresh();
    }
}
