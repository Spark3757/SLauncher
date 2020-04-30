package ru.spark.slauncher.ui.account;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.ListPage;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.javafx.ExtendedProperties;
import ru.spark.slauncher.util.javafx.MappedObservableList;

public class AccountList extends ListPage<AccountListItem> implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.fromTitle(I18n.i18n("account.manage")));
    private final ListProperty<Account> accounts = new SimpleListProperty<>(this, "accounts", FXCollections.observableArrayList());
    private final ObjectProperty<Account> selectedAccount;

    public AccountList() {
        setItems(MappedObservableList.create(accounts, AccountListItem::new));
        selectedAccount = ExtendedProperties.createSelectedItemPropertyFor(getItems(), Account.class);
    }

    public ObjectProperty<Account> selectedAccountProperty() {
        return selectedAccount;
    }

    public ListProperty<Account> accountsProperty() {
        return accounts;
    }

    @Override
    public void add() {
        Controllers.dialog(new AddAccountPane());
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }
}
