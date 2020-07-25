package ru.spark.slauncher.ui.wizard;

import javafx.beans.property.StringProperty;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.task.TaskListener;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.construct.DialogCloseEvent;
import ru.spark.slauncher.ui.construct.MessageDialogPane;
import ru.spark.slauncher.ui.construct.TaskExecutorDialogPane;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;

import java.util.Map;
import java.util.Queue;

import static ru.spark.slauncher.ui.FXUtils.runInFX;

public abstract class TaskExecutorDialogWizardDisplayer extends AbstractWizardDisplayer {

    public TaskExecutorDialogWizardDisplayer(Queue<Object> cancelQueue) {
        super(cancelQueue);
    }

    @Override
    public void handleTask(Map<String, Object> settings, Task<?> task) {
        TaskExecutorDialogPane pane = new TaskExecutorDialogPane(it -> {
            it.fireEvent(new DialogCloseEvent());
            onEnd();
        });

        pane.setTitle(I18n.i18n("message.doing"));
        if (settings.containsKey("title")) {
            Object title = settings.get("title");
            if (title instanceof StringProperty)
                pane.titleProperty().bind((StringProperty) title);
            else if (title instanceof String)
                pane.setTitle((String) title);
        }

        runInFX(() -> {
            TaskExecutor executor = task.cancellableExecutor(new TaskListener() {
                @Override
                public void onStop(boolean success, TaskExecutor executor) {
                    runInFX(() -> {
                        if (success) {
                            if (settings.containsKey("success_message") && settings.get("success_message") instanceof String)
                                Controllers.dialog((String) settings.get("success_message"), null, MessageDialogPane.MessageType.FINE, () -> onEnd());
                            else if (!settings.containsKey("forbid_success_message"))
                                Controllers.dialog(I18n.i18n("message.success"), null, MessageDialogPane.MessageType.FINE, () -> onEnd());
                        } else {
                            if (executor.getException() == null)
                                return;
                            String appendix = StringUtils.getStackTrace(executor.getException());
                            if (settings.get("failure_callback") instanceof WizardProvider.FailureCallback)
                                ((WizardProvider.FailureCallback) settings.get("failure_callback")).onFail(settings, executor.getException(), () -> onEnd());
                            else if (settings.get("failure_message") instanceof String)
                                Controllers.dialog(appendix, (String) settings.get("failure_message"), MessageDialogPane.MessageType.ERROR, () -> onEnd());
                            else if (!settings.containsKey("forbid_failure_message"))
                                Controllers.dialog(appendix, I18n.i18n("wizard.failed"), MessageDialogPane.MessageType.ERROR, () -> onEnd());
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
