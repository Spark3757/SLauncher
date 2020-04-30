package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.task.TaskListener;
import ru.spark.slauncher.ui.FXUtils;

import java.util.Optional;
import java.util.function.Consumer;

import static ru.spark.slauncher.ui.FXUtils.runInFX;

public class TaskExecutorDialogPane extends StackPane {
    private TaskExecutor executor;
    private Consumer<Region> onCancel;
    private final Consumer<FileDownloadTask.SpeedEvent> speedEventHandler;

    @FXML
    private Label lblTitle;
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

        speedEventHandler = speedEvent -> {
            String unit = "B/s";
            double speed = speedEvent.getSpeed();
            if (speed > 1024) {
                speed /= 1024;
                unit = "KB/s";
            }
            if (speed > 1024) {
                speed /= 1024;
                unit = "MB/s";
            }
            double finalSpeed = speed;
            String finalUnit = unit;
            Platform.runLater(() -> {
                lblProgress.setText(String.format("%.1f %s", finalSpeed, finalUnit));
            });
        };
        FileDownloadTask.speedEvent.channel(FileDownloadTask.SpeedEvent.class).registerWeak(speedEventHandler);
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

    public void setCancel(Consumer<Region> onCancel) {
        this.onCancel = onCancel;

        runInFX(() -> btnCancel.setDisable(onCancel == null));
    }
}
