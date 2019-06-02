package ru.spark.slauncher.ui.account;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorServer;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.ListPage;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.util.javafx.MappedObservableList;

import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class AuthlibInjectorServersPage extends ListPage<AuthlibInjectorServerItem> implements DecoratorPage {
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title", i18n("account.injector.manage.title"));

    private final ObservableList<AuthlibInjectorServerItem> serverItems;

    public AuthlibInjectorServersPage() {
        serverItems = MappedObservableList.create(config().getAuthlibInjectorServers(), this::createServerItem);
        Bindings.bindContent(itemsProperty(), serverItems);
    }

    private AuthlibInjectorServerItem createServerItem(AuthlibInjectorServer server) {
        return new AuthlibInjectorServerItem(server,
                item -> config().getAuthlibInjectorServers().remove(item.getServer()));
    }

    @Override
    public void add() {
        Controllers.dialog(new AddAuthlibInjectorServerPane());
    }

    public String getTitle() {
        return title.get();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }
}