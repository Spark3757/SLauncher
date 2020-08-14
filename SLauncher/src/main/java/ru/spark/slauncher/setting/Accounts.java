package ru.spark.slauncher.setting;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.auth.AccountFactory;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.authlibinjector.*;
import ru.spark.slauncher.auth.offline.OfflineAccount;
import ru.spark.slauncher.auth.offline.OfflineAccountFactory;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilAccount;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilAccountFactory;
import ru.spark.slauncher.task.Schedulers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.ui.FXUtils.onInvalidating;
import static ru.spark.slauncher.util.Lang.mapOf;
import static ru.spark.slauncher.util.Logging.LOG;
import static ru.spark.slauncher.util.Pair.pair;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

/**
 * @author spark1337
 */
public final class Accounts {
    public static final OfflineAccountFactory FACTORY_OFFLINE = OfflineAccountFactory.INSTANCE;
    public static final YggdrasilAccountFactory FACTORY_MOJANG = YggdrasilAccountFactory.MOJANG;
    private static final AuthlibInjectorArtifactProvider AUTHLIB_INJECTOR_DOWNLOADER = createAuthlibInjectorArtifactProvider();
    public static final AuthlibInjectorAccountFactory FACTORY_AUTHLIB_INJECTOR = new AuthlibInjectorAccountFactory(AUTHLIB_INJECTOR_DOWNLOADER, Accounts::getOrCreateAuthlibInjectorServer);
    // ==== login type / account factory mapping ====
    private static final Map<String, AccountFactory<?>> type2factory = new HashMap<>();
    private static final Map<AccountFactory<?>, String> factory2type = new HashMap<>();
    private static ObservableList<Account> accounts = observableArrayList(account -> new Observable[]{account});
    private static ReadOnlyListWrapper<Account> accountsWrapper = new ReadOnlyListWrapper<>(Accounts.class, "accounts", accounts);
    /**
     * True if {@link #init()} hasn't been called.
     */
    private static boolean initialized = false;
    private static ObjectProperty<Account> selectedAccount = new SimpleObjectProperty<Account>(Accounts.class, "selectedAccount") {
        {
            accounts.addListener(onInvalidating(this::invalidated));
        }

        @Override
        protected void invalidated() {
// this methods first checks whether the current selection is valid
// if it's valid, the underlying storage will be updated
// otherwise, the first account will be selected as an alternative(or null if accounts is empty)
            Account selected = get();
            if (accounts.isEmpty()) {
                if (selected == null) {
// valid
                } else {
// the previously selected account is gone, we can only set it to null here
                    set(null);
                    return;
                }
            } else {
                if (accounts.contains(selected)) {
// valid
                } else {
// the previously selected account is gone
                    set(accounts.get(0));
                    return;
                }
            }
// selection is valid, store it
            if (!initialized)
                return;
            updateAccountStorages();
        }
    };
    // ====
// ==== Login type name i18n ===
    private static Map<AccountFactory<?>, String> unlocalizedLoginTypeNames = mapOf(
            pair(Accounts.FACTORY_OFFLINE, "account.methods.offline"),
            pair(Accounts.FACTORY_MOJANG, "account.methods.yggdrasil"),
            pair(Accounts.FACTORY_AUTHLIB_INJECTOR, "account.methods.authlib_injector"));

    static {
        type2factory.put("offline", FACTORY_OFFLINE);
        type2factory.put("yggdrasil", FACTORY_MOJANG);
        type2factory.put("authlibInjector", FACTORY_AUTHLIB_INJECTOR);
        type2factory.forEach((type, factory) -> factory2type.put(factory, type));
    }

    static {
        accounts.addListener(onInvalidating(Accounts::updateAccountStorages));
    }
    private Accounts() {
    }

    private static void triggerAuthlibInjectorUpdateCheck() {
        if (AUTHLIB_INJECTOR_DOWNLOADER instanceof AuthlibInjectorDownloader) {
            Schedulers.io().execute(() -> {
                try {
                    ((AuthlibInjectorDownloader) AUTHLIB_INJECTOR_DOWNLOADER).checkUpdate();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Failed to check update for authlib-injector", e);
                }
            });
        }
    }

    public static String getLoginType(AccountFactory<?> factory) {
        return Optional.ofNullable(factory2type.get(factory))
                .orElseThrow(() -> new IllegalArgumentException("Unrecognized account factory"));
    }

    public static AccountFactory<?> getAccountFactory(String loginType) {
        return Optional.ofNullable(type2factory.get(loginType))
                .orElseThrow(() -> new IllegalArgumentException("Unrecognized login type"));
    }

    // ====
    public static AccountFactory<?> getAccountFactory(Account account) {
        if (account instanceof OfflineAccount)
            return FACTORY_OFFLINE;
        else if (account instanceof AuthlibInjectorAccount)
            return FACTORY_AUTHLIB_INJECTOR;
        else if (account instanceof YggdrasilAccount)
            return FACTORY_MOJANG;
        else
            throw new IllegalArgumentException("Failed to determine account type: " + account);
    }

    private static Map<Object, Object> getAccountStorage(Account account) {
        Map<Object, Object> storage = account.toStorage();
        storage.put("type", getLoginType(getAccountFactory(account)));
        if (account == selectedAccount.get()) {
            storage.put("selected", true);
        }
        return storage;
    }

    private static void updateAccountStorages() {
// don't update the underlying storage before data loading is completed
// otherwise it might cause data loss
        if (!initialized)
            return;
// update storage
        config().getAccountStorages().setAll(accounts.stream().map(Accounts::getAccountStorage).collect(toList()));
    }

    /**
     * Called when it's ready to load accounts from {@link ConfigHolder#config()}.
     */
    static void init() {
        if (initialized)
            throw new IllegalStateException("Already initialized");
// load accounts
        config().getAccountStorages().forEach(storage -> {
            AccountFactory<?> factory = type2factory.get(storage.get("type"));
            if (factory == null) {
                LOG.warning("Unrecognized account type: " + storage);
                return;
            }
            Account account;
            try {
                account = factory.fromStorage(storage);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to load account: " + storage, e);
                return;
            }
            accounts.add(account);
            if (Boolean.TRUE.equals(storage.get("selected"))) {
                selectedAccount.set(account);
            }
        });
        initialized = true;
        config().getAuthlibInjectorServers().addListener(onInvalidating(Accounts::removeDanglingAuthlibInjectorAccounts));
        Account selected = selectedAccount.get();
        if (selected != null) {
            Schedulers.io().execute(() -> {
                try {
                    selected.logIn();
                } catch (AuthenticationException e) {
                    LOG.log(Level.WARNING, "Failed to log " + selected + " in", e);
                }
            });
        }
        if (!config().getAuthlibInjectorServers().isEmpty()) {
            triggerAuthlibInjectorUpdateCheck();
        }
        for (AuthlibInjectorServer server : config().getAuthlibInjectorServers()) {
            if (selected instanceof AuthlibInjectorAccount && ((AuthlibInjectorAccount) selected).getServer() == server)
                continue;
            Schedulers.io().execute(() -> {
                try {
                    server.fetchMetadataResponse();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Failed to fetch authlib-injector server metdata: " + server, e);
                }
            });
        }
    }

    public static ObservableList<Account> getAccounts() {
        return accounts;
    }

    public static ReadOnlyListProperty<Account> accountsProperty() {
        return accountsWrapper.getReadOnlyProperty();
    }

    public static Account getSelectedAccount() {
        return selectedAccount.get();
    }

    public static void setSelectedAccount(Account selectedAccount) {
        Accounts.selectedAccount.set(selectedAccount);
    }

    public static ObjectProperty<Account> selectedAccountProperty() {
        return selectedAccount;
    }

    // ==== authlib-injector ====
    private static AuthlibInjectorArtifactProvider createAuthlibInjectorArtifactProvider() {
        String authlibinjectorLocation = System.getProperty("slauncher.authlibinjector.location");
        if (authlibinjectorLocation == null) {
            return new AuthlibInjectorDownloader(
                    Metadata.SL_DIRECTORY.resolve("authlib-injector.jar"),
                    DownloadProviders::getDownloadProvider) {
                @Override
                public Optional<AuthlibInjectorArtifactInfo> getArtifactInfoImmediately() {
                    Optional<AuthlibInjectorArtifactInfo> local = super.getArtifactInfoImmediately();
                    if (local.isPresent()) {
                        return local;
                    }
// search authlib-injector.jar in current directory, it's used as a fallback
                    return parseArtifact(Paths.get("authlib-injector.jar"));
                }
            };
        } else {
            LOG.info("Using specified authlib-injector: " + authlibinjectorLocation);
            return new SimpleAuthlibInjectorArtifactProvider(Paths.get(authlibinjectorLocation));
        }
    }

    private static AuthlibInjectorServer getOrCreateAuthlibInjectorServer(String url) {
        return config().getAuthlibInjectorServers().stream()
                .filter(server -> url.equals(server.getUrl()))
                .findFirst()
                .orElseGet(() -> {
                    AuthlibInjectorServer server = new AuthlibInjectorServer(url);
                    config().getAuthlibInjectorServers().add(server);
                    return server;
                });
    }

    /**
     * After an {@link AuthlibInjectorServer} is removed, the associated accounts should also be removed.
     * This method performs a check and removes the dangling accounts.
     */
    private static void removeDanglingAuthlibInjectorAccounts() {
        accounts.stream()
                .filter(AuthlibInjectorAccount.class::isInstance)
                .map(AuthlibInjectorAccount.class::cast)
                .filter(it -> !config().getAuthlibInjectorServers().contains(it.getServer()))
                .collect(toList())
                .forEach(accounts::remove);
    }

    public static String getLocalizedLoginTypeName(AccountFactory<?> factory) {
        return i18n(Optional.ofNullable(unlocalizedLoginTypeNames.get(factory))
                .orElseThrow(() -> new IllegalArgumentException("Unrecognized account factory")));
    }
// ====
}