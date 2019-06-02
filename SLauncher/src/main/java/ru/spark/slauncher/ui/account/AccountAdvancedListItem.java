package ru.spark.slauncher.ui.account;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.auth.ely.ElyAccount;
import ru.spark.slauncher.auth.offline.OfflineAccount;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilAccount;
import ru.spark.slauncher.game.TexturesLoader;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.construct.AdvancedListItem;

import static ru.spark.slauncher.ui.FXUtils.newImage;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class AccountAdvancedListItem extends AdvancedListItem {
    private ObjectProperty<Account> account = new SimpleObjectProperty<Account>() {

        @Override
        protected void invalidated() {
            Account account = get();
            if (account == null) {
                titleProperty().unbind();
                setTitle(i18n("account.missing"));
                setSubtitle(i18n("account.missing.add"));
                imageProperty().unbind();
                setImage(newImage("/assets/img/craft_table.png"));
            } else {
                titleProperty().bind(Bindings.createStringBinding(account::getCharacter, account));
                setSubtitle(accountSubtitle(account));
                imageProperty().bind(TexturesLoader.fxAvatarBinding(account, 32));
            }
        }
    };

    public AccountAdvancedListItem() {
        setRightGraphic(SVG.viewList(Theme.blackFillBinding(), -1, -1));
    }

    private static String accountSubtitle(Account account) {
        if (account instanceof OfflineAccount)
            return i18n("account.methods.offline");
        else if (account instanceof YggdrasilAccount)
            return account.getUsername();
        else if (account instanceof ElyAccount)
            return account.getUsername();
        else
            return "";
    }

    public ObjectProperty<Account> accountProperty() {
        return account;
    }
}
