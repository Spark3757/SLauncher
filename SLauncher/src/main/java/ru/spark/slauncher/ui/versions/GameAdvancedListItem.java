package ru.spark.slauncher.ui.versions;

import javafx.scene.image.Image;
import ru.spark.slauncher.setting.Profiles;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.construct.AdvancedListItem;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class GameAdvancedListItem extends AdvancedListItem {

    public GameAdvancedListItem() {
        FXUtils.onChangeAndOperate(Profiles.selectedVersionProperty(), version -> {
            if (version != null && Profiles.getSelectedProfile() != null &&
                    Profiles.getSelectedProfile().getRepository().hasVersion(version)) {
                setTitle(version);
                setSubtitle(null);
                setImage(Profiles.getSelectedProfile().getRepository().getVersionIconImage(version));
            } else {
                setTitle(i18n("version.empty"));
                setSubtitle(i18n("version.empty.add"));
                setImage(new Image("/assets/img/grass.png"));
            }
        });

        setRightGraphic(SVG.gear(Theme.blackFillBinding(), -1, -1));
    }
}
