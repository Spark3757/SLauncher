package ru.spark.slauncher.ui.construct;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.util.Logging;

import java.util.Stack;

public class StackContainerPane extends StackPane {
    private final Stack<Node> stack = new Stack<>();

    public void push(Node node) {
        stack.push(node);
        getChildren().setAll(node);

        Logging.LOG.info(this + " " + stack);
    }

    public void pop(Node node) {
        boolean flag = stack.remove(node);
        if (stack.isEmpty())
            getChildren().setAll();
        else
            getChildren().setAll(stack.peek());

        Logging.LOG.info(this + " " + stack + ", removed: " + flag + ", object: " + node);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
