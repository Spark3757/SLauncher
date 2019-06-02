package ru.spark.slauncher.ui.account;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorServer;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.animation.ContainerAnimations;
import ru.spark.slauncher.ui.animation.TransitionHandler;
import ru.spark.slauncher.ui.construct.DialogAware;
import ru.spark.slauncher.ui.construct.DialogCloseEvent;
import ru.spark.slauncher.ui.construct.SpinnerPane;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.io.IOException;
import java.util.logging.Level;

import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class AddAuthlibInjectorServerPane extends StackPane implements DialogAware {

    @FXML
    private StackPane addServerContainer;
    @FXML
    private Label lblServerUrl;
    @FXML
    private Label lblServerName;
    @FXML
    private Label lblCreationWarning;
    @FXML
    private Label lblServerWarning;
    @FXML
    private JFXTextField txtServerUrl;
    @FXML
    private JFXDialogLayout addServerPane;
    @FXML
    private JFXDialogLayout confirmServerPane;
    @FXML
    private SpinnerPane nextPane;
    @FXML
    private JFXButton btnAddNext;

    private TransitionHandler transitionHandler;

    private AuthlibInjectorServer serverBeingAdded;

    public AddAuthlibInjectorServerPane(String url) {
        this();
        txtServerUrl.setText(url);
        onAddNext();
    }

    public AddAuthlibInjectorServerPane() {
        FXUtils.loadFXML(this, "/assets/fxml/authlib-injector-server-add.fxml");
        transitionHandler = new TransitionHandler(addServerContainer);
        transitionHandler.setContent(addServerPane, ContainerAnimations.NONE.getAnimationProducer());

        btnAddNext.disableProperty().bind(txtServerUrl.textProperty().isEmpty());
        nextPane.hideSpinner();
    }

    @Override
    public void onDialogShown() {
        txtServerUrl.requestFocus();
    }

    private String resolveFetchExceptionMessage(Throwable exception) {
        if (exception instanceof IOException) {
            return i18n("account.failed.connect_injector_server");
        } else {
            return exception.getClass().getName() + ": " + exception.getLocalizedMessage();
        }
    }

    @FXML
    private void onAddCancel() {
        fireEvent(new DialogCloseEvent());
    }

    @FXML
    private void onAddNext() {
        if (btnAddNext.isDisabled())
            return;

        lblCreationWarning.setText("");

        String url = txtServerUrl.getText();

        nextPane.showSpinner();
        addServerPane.setDisable(true);

        Task.of(() -> {
            serverBeingAdded = AuthlibInjectorServer.locateServer(url);
        }).whenComplete(Schedulers.javafx(), (isDependentSucceeded, exception) -> {
            addServerPane.setDisable(false);
            nextPane.hideSpinner();

            if (isDependentSucceeded) {
                lblServerName.setText(serverBeingAdded.getName());
                lblServerUrl.setText(serverBeingAdded.getUrl());

                lblServerWarning.setVisible("http".equals(NetworkUtils.toURL(serverBeingAdded.getUrl()).getProtocol()));

                transitionHandler.setContent(confirmServerPane, ContainerAnimations.SWIPE_LEFT.getAnimationProducer());
            } else {
                Logging.LOG.log(Level.WARNING, "Failed to resolve auth server: " + url, exception);
                lblCreationWarning.setText(resolveFetchExceptionMessage(exception));
            }
        }).start();

    }

    @FXML
    private void onAddPrev() {
        transitionHandler.setContent(addServerPane, ContainerAnimations.SWIPE_RIGHT.getAnimationProducer());
    }

    @FXML
    private void onAddFinish() {
        if (!config().getAuthlibInjectorServers().contains(serverBeingAdded)) {
            config().getAuthlibInjectorServers().add(serverBeingAdded);
        }
        fireEvent(new DialogCloseEvent());
    }

}
