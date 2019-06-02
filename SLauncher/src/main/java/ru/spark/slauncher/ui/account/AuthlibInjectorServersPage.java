package ru.spark.slauncher.ui.account;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorServer;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.util.javafx.MappedObservableList;

import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class AuthlibInjectorServersPage extends StackPane implements DecoratorPage {
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title", i18n("account.injector.manage.title"));

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox listPane;
    @FXML
    private StackPane contentPane;

    private ObservableList<AuthlibInjectorServerItem> serverItems;

    public AuthlibInjectorServersPage() {
        FXUtils.loadFXML(this, "/assets/fxml/authlib-injector-servers.fxml");
        FXUtils.smoothScrolling(scrollPane);

        serverItems = MappedObservableList.create(config().getAuthlibInjectorServers(), this::createServerItem);
        Bindings.bindContent(listPane.getChildren(), serverItems);
    }

    private AuthlibInjectorServerItem createServerItem(AuthlibInjectorServer server) {
        return new AuthlibInjectorServerItem(server,
                item -> config().getAuthlibInjectorServers().remove(item.getServer()));
    }

    @FXML
    private void onAdd() {
        Controllers.dialog(new AddAuthlibInjectorServerPane());
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }
}
