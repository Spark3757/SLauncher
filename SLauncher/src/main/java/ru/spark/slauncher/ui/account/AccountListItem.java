package ru.spark.slauncher.ui.account;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.CredentialExpiredException;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorAccount;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorServer;
import ru.spark.slauncher.auth.offline.OfflineAccount;
import ru.spark.slauncher.auth.yggdrasil.CompleteGameProfile;
import ru.spark.slauncher.auth.yggdrasil.TextureType;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilAccount;
import ru.spark.slauncher.game.TexturesLoader;
import ru.spark.slauncher.setting.Accounts;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.DialogController;
import ru.spark.slauncher.ui.construct.PromptDialogPane;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.i18n.I18n;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import static java.util.Collections.emptySet;
import static javafx.beans.binding.Bindings.createBooleanBinding;
import static ru.spark.slauncher.util.Logging.LOG;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class AccountListItem extends RadioButton {

    private final Account account;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty subtitle = new SimpleStringProperty();
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    public AccountListItem(Account account) {
        this.account = account;
        getStyleClass().clear();
        setUserData(account);

        String loginTypeName = Accounts.getLocalizedLoginTypeName(Accounts.getAccountFactory(account));
        if (account instanceof AuthlibInjectorAccount) {
            AuthlibInjectorServer server = ((AuthlibInjectorAccount) account).getServer();
            subtitle.bind(Bindings.concat(
                    loginTypeName, ", ", i18n("account.injector.server"), ": ",
                    Bindings.createStringBinding(server::getName, server)));
        } else {
            subtitle.set(loginTypeName);
        }

        StringBinding characterName = Bindings.createStringBinding(account::getCharacter, account);
        if (account instanceof OfflineAccount) {
            title.bind(characterName);
        } else {
            title.bind(
                    account.getUsername().isEmpty() ? characterName :
                            Bindings.concat(account.getUsername(), " - ", characterName));
        }

        image.bind(TexturesLoader.fxAvatarBinding(account, 32));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AccountListItemSkin(this);
    }

    private Task<?> refreshAsync() {
        return Task.runAsync(() -> {
            account.clearCache();
            try {
                account.logIn();
            } catch (CredentialExpiredException e) {
                try {
                    DialogController.logIn(account);
                } catch (CancellationException e1) {
                    // ignore cancellation
                } catch (Exception e1) {
                    LOG.log(Level.WARNING, "Failed to refresh " + account + " with password", e1);
                    throw e1;
                }
            } catch (AuthenticationException e) {
                LOG.log(Level.WARNING, "Failed to refresh " + account + " with token", e);
                throw e;
            }
        });
    }

    public void refresh() {
        refreshAsync().whenComplete(e -> {}).start();
    }

    public ObservableBooleanValue canUploadSkin() {
        if (account instanceof YggdrasilAccount) {
            if (account instanceof AuthlibInjectorAccount) {
                AuthlibInjectorAccount aiAccount = (AuthlibInjectorAccount) account;
                ObjectBinding<Optional<CompleteGameProfile>> profile = aiAccount.getYggdrasilService().getProfileRepository().binding(aiAccount.getUUID());
                return createBooleanBinding(() -> {
                    Set<TextureType> uploadableTextures = profile.get()
                            .map(AuthlibInjectorAccount::getUploadableTextures)
                            .orElse(emptySet());
                    return uploadableTextures.contains(TextureType.SKIN);
                }, profile);
            } else {
                return createBooleanBinding(() -> true);
            }
        } else {
            return createBooleanBinding(() -> false);
        }
    }

    public void uploadSkin() {
        if (!(account instanceof YggdrasilAccount)) {
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n("account.skin.upload"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(i18n("account.skin.file"), "*.png"));
        File selectedFile = chooser.showOpenDialog(Controllers.getStage());
        if (selectedFile == null) {
            return;
        }

        Controllers.prompt(new PromptDialogPane.Builder(i18n("account.skin.upload"), (questions, resolve, reject) -> {
            PromptDialogPane.Builder.CandidatesQuestion q = (PromptDialogPane.Builder.CandidatesQuestion) questions.get(0);
            String model = q.getValue() == 0 ? "" : "slim";
            refreshAsync()
                    .thenRunAsync(() -> ((YggdrasilAccount) account).uploadSkin(model, selectedFile.toPath()))
                    .thenComposeAsync(this::refreshAsync)
                    .thenRunAsync(Schedulers.javafx(), resolve::run)
                    .whenComplete(Schedulers.javafx(), e -> {
                        if (e != null) {
                            reject.accept(AddAccountPane.accountException(e));
                        }
                    }).start();
        }).addQuestion(new PromptDialogPane.Builder.CandidatesQuestion(i18n("account.skin.model"),
                i18n("account.skin.model.default"), i18n("account.skin.model.slim"))));
    }

    public void remove() {
        Accounts.getAccounts().remove(account);
    }

    public Account getAccount() {
        return account;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getSubtitle() {
        return subtitle.get();
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.set(subtitle);
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public Image getImage() {
        return image.get();
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }
}