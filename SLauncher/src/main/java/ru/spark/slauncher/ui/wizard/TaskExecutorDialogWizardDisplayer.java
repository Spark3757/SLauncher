package ru.spark.slauncher.ui.wizard;

import javafx.beans.property.StringProperty;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.task.TaskListener;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.construct.DialogCloseEvent;
import ru.spark.slauncher.ui.construct.MessageDialogPane.MessageType;
import ru.spark.slauncher.ui.construct.TaskExecutorDialogPane;
import ru.spark.slauncher.util.StringUtils;

import java.util.Map;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public interface TaskExecutorDialogWizardDisplayer extends AbstractWizardDisplayer {

    @Override
    default void handleTask(Map<String, Object> settings, Task task) {
        TaskExecutorDialogPane pane = new TaskExecutorDialogPane(it -> {
            it.fireEvent(new DialogCloseEvent());
            onEnd();
        });

        pane.setTitle(i18n("message.doing"));
        pane.setProgress(Double.MAX_VALUE);
        if (settings.containsKey("title")) {
            Object title = settings.get("title");
            if (title instanceof StringProperty)
                pane.titleProperty().bind((StringProperty) title);
            else if (title instanceof String)
                pane.setTitle((String) title);
        }

        if (settings.containsKey("subtitle")) {
            Object subtitle = settings.get("subtitle");
            if (subtitle instanceof StringProperty)
                pane.subtitleProperty().bind((StringProperty) subtitle);
            else if (subtitle instanceof String)
                pane.setSubtitle((String) subtitle);
        }

        FXUtils.runInFX(() -> {
            TaskExecutor executor = task.executor(new TaskListener() {
                @Override
                public void onStop(boolean success, TaskExecutor executor) {
                    FXUtils.runInFX(() -> {
                        if (success) {
                            if (settings.containsKey("success_message") && settings.get("success_message") instanceof String)
                                Controllers.dialog((String) settings.get("success_message"), null, MessageType.FINE, () -> onEnd());
                            else if (!settings.containsKey("forbid_success_message"))
                                Controllers.dialog(i18n("message.success"), null, MessageType.FINE, () -> onEnd());
                        } else {
                            if (executor.getLastException() == null)
                                return;
                            String appendix = StringUtils.getStackTrace(executor.getLastException());
                            if (settings.get("failure_callback") instanceof WizardProvider.FailureCallback)
                                ((WizardProvider.FailureCallback) settings.get("failure_callback")).onFail(settings, executor.getLastException(), () -> onEnd());
                            else if (settings.get("failure_message") instanceof String)
                                Controllers.dialog(appendix, (String) settings.get("failure_message"), MessageType.ERROR, () -> onEnd());
                            else if (!settings.containsKey("forbid_failure_message"))
                                Controllers.dialog(appendix, i18n("wizard.failed"), MessageType.ERROR, () -> onEnd());
                        }

                    });
                }
            });
            pane.setExecutor(executor);
            Controllers.dialog(pane);
            executor.start();
        });
    }
}
