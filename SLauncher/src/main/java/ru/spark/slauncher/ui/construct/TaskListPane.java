package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.download.forge.ForgeInstallTask;
import ru.spark.slauncher.download.game.GameAssetDownloadTask;
import ru.spark.slauncher.download.liteloader.LiteLoaderInstallTask;
import ru.spark.slauncher.download.optifine.OptiFineInstallTask;
import ru.spark.slauncher.game.SLauncherModpackExportTask;
import ru.spark.slauncher.game.SLauncherModpackInstallTask;
import ru.spark.slauncher.mod.*;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.task.TaskListener;

import java.util.HashMap;
import java.util.Map;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class TaskListPane extends StackPane {
    private final AdvancedListBox listBox = new AdvancedListBox();
    private final Map<Task, ProgressListNode> nodes = new HashMap<>();
    private final ReadOnlyIntegerWrapper finishedTasks = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper totTasks = new ReadOnlyIntegerWrapper();

    public TaskListPane() {
        listBox.setSpacing(0);

        getChildren().setAll(listBox);
    }

    public ReadOnlyIntegerProperty finishedTasksProperty() {
        return finishedTasks.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty totTasksProperty() {
        return totTasks.getReadOnlyProperty();
    }

    public void setExecutor(TaskExecutor executor) {
        executor.addTaskListener(new TaskListener() {
            @Override
            public void onStart() {
                Platform.runLater(() -> {
                    listBox.clear();
                    finishedTasks.set(0);
                    totTasks.set(0);
                });
            }

            @Override
            public void onReady(Task task) {
                Platform.runLater(() -> totTasks.set(totTasks.getValue() + 1));
            }

            @Override
            public void onRunning(Task task) {
                if (!task.getSignificance().shouldShow())
                    return;

                if (task instanceof GameAssetDownloadTask) {
                    task.setName(i18n("assets.download_all"));
                } else if (task instanceof ForgeInstallTask) {
                    task.setName(i18n("install.installer.install", i18n("install.installer.forge")));
                } else if (task instanceof LiteLoaderInstallTask) {
                    task.setName(i18n("install.installer.install", i18n("install.installer.liteloader")));
                } else if (task instanceof OptiFineInstallTask) {
                    task.setName(i18n("install.installer.install", i18n("install.installer.optifine")));
                } else if (task instanceof CurseCompletionTask) {
                    task.setName(i18n("modpack.type.curse.completion"));
                } else if (task instanceof ModpackInstallTask) {
                    task.setName(i18n("modpack.installing"));
                } else if (task instanceof ModpackUpdateTask) {
                    task.setName(i18n("modpack.update"));
                } else if (task instanceof CurseInstallTask) {
                    task.setName(i18n("modpack.install", i18n("modpack.type.curse")));
                } else if (task instanceof MultiMCModpackInstallTask) {
                    task.setName(i18n("modpack.install", i18n("modpack.type.multimc")));
                } else if (task instanceof SLauncherModpackInstallTask) {
                    task.setName(i18n("modpack.install", i18n("modpack.type.slauncher")));
                } else if (task instanceof SLauncherModpackExportTask) {
                    task.setName(i18n("modpack.export"));
                } else if (task instanceof MinecraftInstanceTask) {
                    task.setName(i18n("modpack.scan"));
                }

                ProgressListNode node = new ProgressListNode(task);
                nodes.put(task, node);
                Platform.runLater(() -> listBox.add(node));
            }

            @Override
            public void onFinished(Task task) {
                ProgressListNode node = nodes.remove(task);
                if (node == null)
                    return;
                node.unbind();
                Platform.runLater(() -> {
                    listBox.remove(node);
                    finishedTasks.set(finishedTasks.getValue() + 1);
                });
            }

            @Override
            public void onFailed(Task task, Throwable throwable) {
                ProgressListNode node = nodes.remove(task);
                if (node == null)
                    return;
                Platform.runLater(() -> {
                    node.setThrowable(throwable);
                    finishedTasks.set(finishedTasks.getValue() + 1);
                });
            }
        });
    }

    private static class ProgressListNode extends BorderPane {
        private final JFXProgressBar bar = new JFXProgressBar();
        private final Label title = new Label();
        private final Label state = new Label();

        public ProgressListNode(Task task) {
            bar.progressProperty().bind(task.progressProperty());
            title.setText(task.getName());
            state.textProperty().bind(task.messageProperty());

            setLeft(title);
            setRight(state);
            setBottom(bar);

            bar.minWidthProperty().bind(widthProperty());
            bar.prefWidthProperty().bind(widthProperty());
            bar.maxWidthProperty().bind(widthProperty());
        }

        public void unbind() {
            bar.progressProperty().unbind();
            state.textProperty().unbind();
        }

        public void setThrowable(Throwable throwable) {
            unbind();
            state.setText(throwable.getLocalizedMessage());
            bar.setProgress(0);
        }
    }
}
