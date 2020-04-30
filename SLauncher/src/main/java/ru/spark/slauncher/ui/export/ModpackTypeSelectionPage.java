package ru.spark.slauncher.ui.export;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardPage;
import ru.spark.slauncher.util.i18n.I18n;

import java.util.Map;

public final class ModpackTypeSelectionPage extends StackPane implements WizardPage {
    private final WizardController controller;
    @FXML
    private JFXButton btnSL;
    @FXML
    private JFXButton btnMultiMC;
    @FXML
    private JFXButton btnServer;

    public ModpackTypeSelectionPage(WizardController controller) {
        this.controller = controller;
        FXUtils.loadFXML(this, "/assets/fxml/modpack/type.fxml");

        JFXButton[] buttons = new JFXButton[]{btnSL, btnMultiMC, btnServer};
        String[] types = new String[]{MODPACK_TYPE_SL, MODPACK_TYPE_MULTIMC, MODPACK_TYPE_SERVER};
        for (int i = 0; i < types.length; ++i) {
            String type = types[i];
            buttons[i].setOnMouseClicked(e -> {
                controller.getSettings().put(MODPACK_TYPE, type);
                controller.onNext();
            });
        }
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
    }

    @Override
    public String getTitle() {
        return I18n.i18n("modpack.wizard.step.3.title");
    }

    public static final String MODPACK_TYPE = "modpack.type";

    public static final String MODPACK_TYPE_SL = "slauncher";
    public static final String MODPACK_TYPE_MULTIMC = "multimc";
    public static final String MODPACK_TYPE_SERVER = "server";
}
