package ru.spark.slauncher.ui.account;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorServer;
import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.ListPage;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.javafx.MappedObservableList;

public class AuthlibInjectorServersPage extends ListPage<AuthlibInjectorServerItem> implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.fromTitle(I18n.i18n("account.injector.manage.title")));

    private final ObservableList<AuthlibInjectorServerItem> serverItems;

    public AuthlibInjectorServersPage() {
        serverItems = MappedObservableList.create(ConfigHolder.config().getAuthlibInjectorServers(), this::createServerItem);
        Bindings.bindContent(itemsProperty(), serverItems);
    }

    private AuthlibInjectorServerItem createServerItem(AuthlibInjectorServer server) {
        return new AuthlibInjectorServerItem(server,
                item -> ConfigHolder.config().getAuthlibInjectorServers().remove(item.getServer()));
    }

    @Override
    public void add() {
        Controllers.dialog(new AddAuthlibInjectorServerPane());
    }

    @Override
    public ReadOnlyObjectWrapper<State> stateProperty() {
        return state;
    }
}
