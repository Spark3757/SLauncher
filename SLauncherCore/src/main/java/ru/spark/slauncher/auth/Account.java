package ru.spark.slauncher.auth;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import ru.spark.slauncher.auth.yggdrasil.Texture;
import ru.spark.slauncher.auth.yggdrasil.TextureType;
import ru.spark.slauncher.util.ToStringBuilder;
import ru.spark.slauncher.util.javafx.ObservableHelper;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author spark1337
 */
public abstract class Account implements Observable {

    /**
     * @return the name of the account who owns the character
     */
    public abstract String getUsername();

    /**
     * @return the character name
     */
    public abstract String getCharacter();

    /**
     * @return the character UUID
     */
    public abstract UUID getUUID();

    /**
     * Login with stored credentials.
     *
     * @throws CredentialExpiredException when the stored credentials has expired, in which case a password login will be performed
     */
    public abstract AuthInfo logIn() throws AuthenticationException;

    /**
     * Login with specified password.
     */
    public abstract AuthInfo logInWithPassword(String password) throws AuthenticationException;

    /**
     * Play offline.
     *
     * @return the specific offline player's info.
     */
    public abstract Optional<AuthInfo> playOffline();

    public abstract Map<Object, Object> toStorage();

    public void clearCache() {
    }

    private ObservableHelper helper = new ObservableHelper(this);

    @Override
    public void addListener(InvalidationListener listener) {
        helper.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        helper.removeListener(listener);
    }

    /**
     * Called when the account has changed.
     * This method can be called from any thread.
     */
    protected void invalidate() {
        Platform.runLater(helper::invalidate);
    }

    public ObjectBinding<Optional<Map<TextureType, Texture>>> getTextures() {
        return Bindings.createObjectBinding(Optional::empty);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("username", getUsername())
                .append("character", getCharacter())
                .append("uuid", getUUID())
                .toString();
    }
}
