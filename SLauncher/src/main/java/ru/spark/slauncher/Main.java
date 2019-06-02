package ru.spark.slauncher;

import ru.spark.slauncher.upgrade.UpdateHandler;
import ru.spark.slauncher.util.Logging;

import javax.swing.*;
import java.io.File;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class Main {

    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        System.setProperty("http.agent", "SLauncher/" + Metadata.VERSION);
        System.setProperty("javafx.autoproxy.disable", "true");

        checkJavaFX();
        checkDirectoryPath();


        Logging.start(Metadata.SLauncher_DIRECTORY.resolve("logs"));


        if (UpdateHandler.processArguments(args)) {
            return;
        }

        Launcher.main(args);
    }

    private static void checkDirectoryPath() {
        String currentDirectory = new File("").getAbsolutePath();
        if (currentDirectory.contains("!")) {
            // No Chinese translation because both Swing and JavaFX cannot render Chinese character properly when exclamation mark exists in the path.
            showErrorAndExit("Exclamation mark(!) is not allowed in the path where SLauncher is in.\n"
                    + "The path is " + currentDirectory);
        }
    }

    private static void checkJavaFX() {
        try {
            Class.forName("javafx.application.Application");
        } catch (ClassNotFoundException e) {
            showErrorAndExit(i18n("fatal.missing_javafx"));
        }
    }


    /**
     * Indicates that a fatal error has occurred, and that the application cannot start.
     */
    static void showErrorAndExit(String message) {
        System.err.println(message);
        System.err.println("A fatal error has occurred, forcibly exiting.");
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    /**
     * Indicates that potential issues have been detected, and that the application may not function properly (but it can still run).
     */
    static void showWarningAndContinue(String message) {
        System.err.println(message);
        System.err.println("Potential issues have been detected.");
        JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

}
