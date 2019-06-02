package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.task.TaskListener;
import ru.spark.slauncher.ui.FXUtils;

import java.util.Optional;
import java.util.function.Consumer;

public class TaskExecutorDialogPane extends StackPane {
    private TaskExecutor executor;
    private Consumer<Region> onCancel;

    @FXML
    private JFXProgressBar progressBar;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblSubtitle;
    @FXML
    private Label lblProgress;
    @FXML
    private JFXButton btnCancel;
    @FXML
    private TaskListPane taskListPane;

    public TaskExecutorDialogPane(Consumer<Region> cancel) {
        FXUtils.loadFXML(this, "/assets/fxml/task-dialog.fxml");

        setCancel(cancel);

        btnCancel.setOnMouseClicked(e -> {
            Optional.ofNullable(executor).ifPresent(TaskExecutor::cancel);
            onCancel.accept(this);
        });

        lblProgress.textProperty().bind(Bindings.createStringBinding(
                () -> taskListPane.finishedTasksProperty().get() + "/" + taskListPane.totTasksProperty().get(),
                taskListPane.finishedTasksProperty(), taskListPane.totTasksProperty()
        ));
    }

    public void setExecutor(TaskExecutor executor) {
        setExecutor(executor, true);
    }

    public void setExecutor(TaskExecutor executor, boolean autoClose) {
        this.executor = executor;

        if (executor != null) {
            taskListPane.setExecutor(executor);

            if (autoClose)
                executor.addTaskListener(new TaskListener() {
                    @Override
                    public void onStop(boolean success, TaskExecutor executor) {
                        Platform.runLater(() -> fireEvent(new DialogCloseEvent()));
                    }
                });
        }
    }

    public StringProperty titleProperty() {
        return lblTitle.textProperty();
    }

    public String getTitle() {
        return lblTitle.getText();
    }

    public void setTitle(String currentState) {
        lblTitle.setText(currentState);
    }

    public StringProperty subtitleProperty() {
        return lblSubtitle.textProperty();
    }

    public String getSubtitle() {
        return lblSubtitle.getText();
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    public void setProgress(double progress) {
        if (progress == Double.MAX_VALUE)
            progressBar.setVisible(false);
        else
            progressBar.setProgress(progress);
    }

    public void setCancel(Consumer<Region> onCancel) {
        this.onCancel = onCancel;

        FXUtils.runInFX(() -> btnCancel.setDisable(onCancel == null));
    }
}
