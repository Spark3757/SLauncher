package ru.spark.slauncher.ui.account;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.auth.NoSelectedCharacterException;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.construct.DialogCloseEvent;
import ru.spark.slauncher.util.Logging;

import java.util.function.Consumer;
import java.util.logging.Level;

public class AccountLoginPane extends StackPane {
    private final Account oldAccount;
    private final Consumer<AuthInfo> success;
    private final Runnable failed;

    @FXML
    private Label lblUsername;
    @FXML
    private JFXPasswordField txtPassword;
    @FXML
    private Label lblCreationWarning;
    @FXML
    private JFXProgressBar progressBar;

    public AccountLoginPane(Account oldAccount, Consumer<AuthInfo> success, Runnable failed) {
        this.oldAccount = oldAccount;
        this.success = success;
        this.failed = failed;

        FXUtils.loadFXML(this, "/assets/fxml/account-login.fxml");

        lblUsername.setText(oldAccount.getUsername());
        txtPassword.setOnAction(e -> onAccept());
    }

    @FXML
    private void onAccept() {
        String password = txtPassword.getText();
        progressBar.setVisible(true);
        lblCreationWarning.setText("");
        Task.supplyAsync(() -> oldAccount.logInWithPassword(password))
                .whenComplete(Schedulers.javafx(), authInfo -> {
                    success.accept(authInfo);
                    fireEvent(new DialogCloseEvent());
                    progressBar.setVisible(false);
                }, e -> {
                    Logging.LOG.log(Level.INFO, "Failed to login with password: " + oldAccount, e);
                    if (e instanceof NoSelectedCharacterException) {
                        fireEvent(new DialogCloseEvent());
                    } else {
                        lblCreationWarning.setText(AddAccountPane.accountException(e));
                    }
                    progressBar.setVisible(false);
                }).start();
    }

    @FXML
    private void onCancel() {
        failed.run();
        fireEvent(new DialogCloseEvent());
    }
}
