package ru.spark.slauncher.ui.versions;

import javafx.scene.control.Tooltip;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.construct.AdvancedListItem;
import ru.spark.slauncher.util.i18n.I18n;

import static ru.spark.slauncher.ui.FXUtils.newImage;

public class GameAdvancedListItem extends AdvancedListItem {
    private final Tooltip tooltip;

    public GameAdvancedListItem() {
        tooltip = new Tooltip();

        FXUtils.onChangeAndOperate(Profiles.selectedVersionProperty(), version -> {
            if (version != null && Profiles.getSelectedProfile() != null &&
                    Profiles.getSelectedProfile().getRepository().hasVersion(version)) {
                FXUtils.installFastTooltip(this, tooltip);
                setTitle(version);
                setSubtitle(null);
                setImage(Profiles.getSelectedProfile().getRepository().getVersionIconImage(version));
                tooltip.setText(version);
            } else {
                Tooltip.uninstall(this,tooltip);
                setTitle(I18n.i18n("version.empty"));
                setSubtitle(I18n.i18n("version.empty.add"));
                setImage(newImage("/assets/img/grass.png"));
                tooltip.setText("");
            }
        });

        setRightGraphic(SVG.gear(Theme.blackFillBinding(), -1, -1));
    }
}
