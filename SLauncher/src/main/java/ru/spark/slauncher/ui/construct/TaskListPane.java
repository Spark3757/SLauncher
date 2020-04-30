package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.download.fabric.FabricInstallTask;
import ru.spark.slauncher.download.forge.ForgeInstallTask;
import ru.spark.slauncher.download.game.GameAssetDownloadTask;
import ru.spark.slauncher.download.game.GameInstallTask;
import ru.spark.slauncher.download.liteloader.LiteLoaderInstallTask;
import ru.spark.slauncher.download.optifine.OptiFineInstallTask;
import ru.spark.slauncher.game.SLModpackExportTask;
import ru.spark.slauncher.game.SLModpackInstallTask;
import ru.spark.slauncher.mod.MinecraftInstanceTask;
import ru.spark.slauncher.mod.ModpackInstallTask;
import ru.spark.slauncher.mod.ModpackUpdateTask;
import ru.spark.slauncher.mod.curse.CurseCompletionTask;
import ru.spark.slauncher.mod.curse.CurseInstallTask;
import ru.spark.slauncher.mod.multimc.MultiMCModpackInstallTask;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.task.TaskListener;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TaskListPane extends StackPane {
    private TaskExecutor executor;
    private final AdvancedListBox listBox = new AdvancedListBox();
    private final Map<Task<?>, ProgressListNode> nodes = new HashMap<>();
    private final List<StageNode> stageNodes = new ArrayList<>();
    private final ObjectProperty<Insets> progressNodePadding = new SimpleObjectProperty<>(Insets.EMPTY);

    public TaskListPane() {
        listBox.setSpacing(0);

        getChildren().setAll(listBox);
    }

    public void setExecutor(TaskExecutor executor) {
        List<String> stages = Lang.removingDuplicates(executor.getStages());
        this.executor = executor;
        executor.addTaskListener(new TaskListener() {
            @Override
            public void onStart() {
                Platform.runLater(() -> {
                    stageNodes.clear();
                    listBox.clear();
                    stageNodes.addAll(stages.stream().map(StageNode::new).collect(Collectors.toList()));
                    stageNodes.forEach(listBox::add);

                    if (stages.isEmpty()) progressNodePadding.setValue(new Insets(0, 0, 8, 0));
                    else progressNodePadding.setValue(new Insets(0, 0, 8, 26));
                });
            }

            @Override
            public void onReady(Task<?> task) {
                if (task instanceof Task.StageTask) {
                    Platform.runLater(() -> {
                        stageNodes.stream().filter(x -> x.stage.equals(task.getStage())).findAny().ifPresent(StageNode::begin);
                    });
                }
            }

            @Override
            public void onRunning(Task<?> task) {
                if (!task.getSignificance().shouldShow() || task.getName() == null)
                    return;

                if (task instanceof GameAssetDownloadTask) {
                    task.setName(I18n.i18n("assets.download_all"));
                } else if (task instanceof GameInstallTask) {
                    task.setName(I18n.i18n("install.installer.install", I18n.i18n("install.installer.game")));
                } else if (task instanceof ForgeInstallTask) {
                    task.setName(I18n.i18n("install.installer.install", I18n.i18n("install.installer.forge")));
                } else if (task instanceof LiteLoaderInstallTask) {
                    task.setName(I18n.i18n("install.installer.install", I18n.i18n("install.installer.liteloader")));
                } else if (task instanceof OptiFineInstallTask) {
                    task.setName(I18n.i18n("install.installer.install", I18n.i18n("install.installer.optifine")));
                } else if (task instanceof FabricInstallTask) {
                    task.setName(I18n.i18n("install.installer.install", I18n.i18n("install.installer.fabric")));
                } else if (task instanceof CurseCompletionTask) {
                    task.setName(I18n.i18n("modpack.type.curse.completion"));
                } else if (task instanceof ModpackInstallTask) {
                    task.setName(I18n.i18n("modpack.installing"));
                } else if (task instanceof ModpackUpdateTask) {
                    task.setName(I18n.i18n("modpack.update"));
                } else if (task instanceof CurseInstallTask) {
                    task.setName(I18n.i18n("modpack.install", I18n.i18n("modpack.type.curse")));
                } else if (task instanceof MultiMCModpackInstallTask) {
                    task.setName(I18n.i18n("modpack.install", I18n.i18n("modpack.type.multimc")));
                } else if (task instanceof SLModpackInstallTask) {
                    task.setName(I18n.i18n("modpack.install", I18n.i18n("modpack.type.slauncher")));
                } else if (task instanceof SLModpackExportTask) {
                    task.setName(I18n.i18n("modpack.export"));
                } else if (task instanceof MinecraftInstanceTask) {
                    task.setName(I18n.i18n("modpack.scan"));
                }

                Platform.runLater(() -> {
                    ProgressListNode node = new ProgressListNode(task);
                    nodes.put(task, node);
                    StageNode stageNode = stageNodes.stream().filter(x -> x.stage.equals(task.getStage())).findAny().orElse(null);
                    listBox.add(listBox.indexOf(stageNode) + 1, node);
                });
            }

            @Override
            public void onFinished(Task<?> task) {
                if (task instanceof Task.StageTask) {
                    Platform.runLater(() -> {
                        stageNodes.stream().filter(x -> x.stage.equals(task.getStage())).findAny().ifPresent(StageNode::succeed);
                    });
                }

                Platform.runLater(() -> {
                    ProgressListNode node = nodes.remove(task);
                    if (node == null)
                        return;
                    node.unbind();
                    listBox.remove(node);
                });
            }

            @Override
            public void onFailed(Task<?> task, Throwable throwable) {
                if (task instanceof Task.StageTask) {
                    Platform.runLater(() -> {
                        stageNodes.stream().filter(x -> x.stage.equals(task.getStage())).findAny().ifPresent(StageNode::fail);
                    });
                }
                ProgressListNode node = nodes.remove(task);
                if (node == null)
                    return;
                Platform.runLater(() -> {
                    node.setThrowable(throwable);
                });
            }

            @Override
            public void onPropertiesUpdate(Map<String, Map<String, Object>> stageProperties) {
                stageProperties.forEach((stage, properties) -> {
                    int count = Lang.tryCast(properties.get("count"), Integer.class).orElse(0),
                            total = Lang.tryCast(properties.get("total"), Integer.class).orElse(0);
                    if (total > 0)
                        Platform.runLater(() ->
                                stageNodes.stream().filter(x -> x.stage.equals(stage)).findAny().ifPresent(stageNode -> stageNode.updateCounter(count, total)));
                });
            }
        });
    }

    private static class StageNode extends BorderPane {
        private final String stage;
        private final Label title = new Label();
        private final String message;
        private boolean started = false;

        public StageNode(String stage) {
            this.stage = stage;

            String stageKey = StringUtils.substringBefore(stage, ':');
            String stageValue = StringUtils.substringAfter(stage, ':');

            // @formatter:off
            switch (stageKey) {
                case "slauncher.modpack":
                    message = I18n.i18n("install.modpack");
                    break;
                case "slauncher.modpack.download":
                    message = I18n.i18n("launch.state.modpack");
                    break;
                case "slauncher.install.assets":
                    message = I18n.i18n("assets.download");
                    break;
                case "slauncher.install.game":
                    message = I18n.i18n("install.installer.install", I18n.i18n("install.installer.game") + " " + stageValue);
                    break;
                case "slauncher.install.forge":
                    message = I18n.i18n("install.installer.install", I18n.i18n("install.installer.forge") + " " + stageValue);
                    break;
                case "slauncher.install.liteloader":
                    message = I18n.i18n("install.installer.install", I18n.i18n("install.installer.liteloader") + " " + stageValue);
                    break;
                case "slauncher.install.optifine":
                    message = I18n.i18n("install.installer.install", I18n.i18n("install.installer.optifine") + " " + stageValue);
                    break;
                case "slauncher.install.fabric":
                    message = I18n.i18n("install.installer.install", I18n.i18n("install.installer.fabric") + " " + stageValue);
                    break;
                default:
                    message = I18n.i18n(stageKey);
                    break;
            }
            // @formatter:on

            title.setText(message);
            BorderPane.setAlignment(title, Pos.CENTER_LEFT);
            BorderPane.setMargin(title, new Insets(0, 0, 0, 8));
            setPadding(new Insets(0, 0, 8, 4));
            setCenter(title);
            setLeft(FXUtils.limitingSize(SVG.dotsHorizontal(Theme.blackFillBinding(), 14, 14), 14, 14));
        }

        public void begin() {
            if (started) return;
            started = true;
            setLeft(FXUtils.limitingSize(SVG.arrowRight(Theme.blackFillBinding(), 14, 14), 14, 14));
        }

        public void fail() {
            setLeft(FXUtils.limitingSize(SVG.close(Theme.blackFillBinding(), 14, 14), 14, 14));
        }

        public void succeed() {
            setLeft(FXUtils.limitingSize(SVG.check(Theme.blackFillBinding(), 14, 14), 14, 14));
        }

        public void updateCounter(int count, int total) {
            if (total > 0)
                title.setText(String.format("%s - %d/%d", message, count, total));
            else
                title.setText(message);
        }
    }

    private class ProgressListNode extends BorderPane {
        private final JFXProgressBar bar = new JFXProgressBar();
        private final Label title = new Label();
        private final Label state = new Label();
        private final DoubleBinding binding = Bindings.createDoubleBinding(() ->
                        getWidth() - getPadding().getLeft() - getPadding().getRight(),
                paddingProperty(), widthProperty());

        public ProgressListNode(Task<?> task) {
            bar.progressProperty().bind(task.progressProperty());
            title.setText(task.getName());
            state.textProperty().bind(task.messageProperty());

            setLeft(title);
            setRight(state);
            setBottom(bar);

            bar.minWidthProperty().bind(binding);
            bar.prefWidthProperty().bind(binding);
            bar.maxWidthProperty().bind(binding);

            paddingProperty().bind(progressNodePadding);
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
