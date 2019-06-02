package ru.spark.slauncher.task;

import ru.spark.slauncher.util.function.ExceptionalSupplier;

import java.util.Collection;
import java.util.Collections;

/**
 * A task that combines two tasks and make sure [pred] runs before succ.
 *
 * @author Spark1337
 */
final class CoupleTask extends Task {

    private final boolean relyingOnDependents;
    private final Task pred;
    private final ExceptionalSupplier<Task, ?> supplier;
    private Task succ;

    /**
     * A task that combines two tasks and make sure pred runs before succ.
     *
     * @param pred                the task that runs before supplier.
     * @param supplier            a callback that returns the task runs after pred, succ will be executed asynchronously. You can do something that relies on the result of pred.
     * @param relyingOnDependents true if this task chain will be broken when task pred fails.
     */
    CoupleTask(Task pred, ExceptionalSupplier<Task, ?> supplier, boolean relyingOnDependents) {
        this.pred = pred;
        this.supplier = supplier;
        this.relyingOnDependents = relyingOnDependents;

        setSignificance(TaskSignificance.MODERATE);
        setName(supplier.toString());
    }

    @Override
    public void execute() throws Exception {
        setName(supplier.toString());
        succ = supplier.get();
    }

    @Override
    public Collection<Task> getDependents() {
        return pred == null ? Collections.emptySet() : Collections.singleton(pred);
    }

    @Override
    public Collection<Task> getDependencies() {
        return succ == null ? Collections.emptySet() : Collections.singleton(succ);
    }

    @Override
    public boolean isRelyingOnDependents() {
        return relyingOnDependents;
    }
}
