package ru.spark.slauncher.ui.wizard;

import javafx.scene.Node;

public final class Wizard {

    public static Node createWizard(WizardProvider provider) {
        return createWizard("", provider);
    }

    public static Node createWizard(String namespace, WizardProvider provider) {
        return new DefaultWizardDisplayer(namespace, provider);
    }
}
