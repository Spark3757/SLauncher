package ru.spark.slauncher.ui.account;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorAccount;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorServer;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilAccount;
import ru.spark.slauncher.game.TexturesLoader;
import ru.spark.slauncher.setting.Accounts;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.construct.AdvancedListItem;
import ru.spark.slauncher.util.i18n.I18n;

import static ru.spark.slauncher.ui.FXUtils.newImage;

public class AccountAdvancedListItem extends AdvancedListItem {
    private final Tooltip tooltip;

    private ObjectProperty<Account> account = new SimpleObjectProperty<Account>() {

        @Override
        protected void invalidated() {
            Account account = get();
            if (account == null) {
                titleProperty().unbind();
                setTitle(I18n.i18n("account.missing"));
                setSubtitle(I18n.i18n("account.missing.add"));
                imageProperty().unbind();
                setImage(newImage("/assets/img/craft_table.png"));
                tooltip.setText("");
            } else {
                titleProperty().bind(Bindings.createStringBinding(account::getCharacter, account));
                setSubtitle(accountSubtitle(account));
                imageProperty().bind(TexturesLoader.fxAvatarBinding(account, 32));
                tooltip.setText(account.getCharacter() + " " + accountTooltip(account));
            }
        }
    };

    public AccountAdvancedListItem() {
        setRightGraphic(SVG.viewList(Theme.blackFillBinding(), -1, -1));
        tooltip = new Tooltip();
        FXUtils.installFastTooltip(this, tooltip);

        setOnScroll(event -> {
            Account current = account.get();
            if (current == null) return;
            ObservableList<Account> accounts = Accounts.getAccounts();
            int currentIndex = accounts.indexOf(account.get());
            if (event.getDeltaY() > 0) { // up
                currentIndex--;
            } else { // down
                currentIndex++;
            }
            Accounts.setSelectedAccount(accounts.get((currentIndex + accounts.size()) % accounts.size()));
        });
    }

    public ObjectProperty<Account> accountProperty() {
        return account;
    }

    private static String accountSubtitle(Account account) {
        String loginTypeName = Accounts.getLocalizedLoginTypeName(Accounts.getAccountFactory(account));
        if (account instanceof AuthlibInjectorAccount) {
            return ((AuthlibInjectorAccount) account).getServer().getName();
        } else {
            return loginTypeName;
        }
    }

    private static String accountTooltip(Account account) {
        if (account instanceof AuthlibInjectorAccount) {
            AuthlibInjectorServer server = ((AuthlibInjectorAccount) account).getServer();
            return account.getUsername() + ", " + I18n.i18n("account.injector.server") + ": " + server.getName();
        } else if (account instanceof YggdrasilAccount) {
            return account.getUsername();
        } else {
            return "";
        }
    }
}
