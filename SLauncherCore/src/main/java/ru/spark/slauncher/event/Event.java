package ru.spark.slauncher.event;

import ru.spark.slauncher.util.ToStringBuilder;

import java.util.Objects;

/**
 * @author Spark1337
 */
public class Event {

    /**
     * The object on which the Event initially occurred.
     */
    protected final transient Object source;
    private boolean canceled;
    private Result result = Result.DEFAULT;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws NullPointerException if source is null.
     */
    public Event(Object source) {
        Objects.requireNonNull(source);

        this.source = source;
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns a String representation of this Event.
     *
     * @return A a String representation of this Event.
     */
    public String toString() {
        return new ToStringBuilder(this).append("source", source).toString();
    }

    /**
     * true if this event is canceled.
     *
     * @throws UnsupportedOperationException if trying to cancel a non-cancelable event.
     */
    public final boolean isCanceled() {
        return canceled;
    }

    /**
     * @param canceled new value
     * @throws UnsupportedOperationException if trying to cancel a non-cancelable event.
     */
    public final void setCanceled(boolean canceled) {
        if (!isCancelable())
            throw new UnsupportedOperationException("Attempted to cancel a non-cancelable event: " + getClass());
        this.canceled = canceled;
    }

    /**
     * true if this Event this cancelable.
     */
    public boolean isCancelable() {
        return false;
    }

    public boolean hasResult() {
        return false;
    }

    /**
     * Retutns the value set as the result of this event
     */
    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        if (!hasResult())
            throw new UnsupportedOperationException("Attempted to set result on a no result event: " + this.getClass() + " of type.");
        this.result = result;
    }

    public enum Result {
        DENY,
        DEFAULT,
        ALLOW
    }
}
