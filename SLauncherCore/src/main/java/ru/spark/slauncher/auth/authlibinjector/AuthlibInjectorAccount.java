package ru.spark.slauncher.auth.authlibinjector;

import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.CharacterSelector;
import ru.spark.slauncher.auth.ServerDisconnectException;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilAccount;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilSession;
import ru.spark.slauncher.game.Arguments;
import ru.spark.slauncher.util.ToStringBuilder;
import ru.spark.slauncher.util.function.ExceptionalSupplier;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AuthlibInjectorAccount extends YggdrasilAccount {
    private final AuthlibInjectorServer server;
    private AuthlibInjectorArtifactProvider downloader;

    public AuthlibInjectorAccount(AuthlibInjectorServer server, AuthlibInjectorArtifactProvider downloader, String username, String password, CharacterSelector selector) throws AuthenticationException {
        super(server.getYggdrasilService(), username, password, selector);
        this.server = server;
        this.downloader = downloader;
    }

    public AuthlibInjectorAccount(AuthlibInjectorServer server, AuthlibInjectorArtifactProvider downloader, String username, YggdrasilSession session) {
        super(server.getYggdrasilService(), username, session);
        this.server = server;
        this.downloader = downloader;
    }

    @Override
    public synchronized AuthInfo logIn() throws AuthenticationException {
        return inject(super::logIn);
    }

    @Override
    public synchronized AuthInfo logInWithPassword(String password) throws AuthenticationException {
        return inject(() -> super.logInWithPassword(password));
    }

    @Override
    public Optional<AuthInfo> playOffline() {
        Optional<AuthInfo> auth = super.playOffline();
        Optional<AuthlibInjectorArtifactInfo> artifact = downloader.getArtifactInfoImmediately();
        Optional<String> prefetchedMeta = server.getMetadataResponse();

        if (auth.isPresent() && artifact.isPresent() && prefetchedMeta.isPresent()) {
            return Optional.of(auth.get().withArguments(generateArguments(artifact.get(), server, prefetchedMeta.get())));
        } else {
            return Optional.empty();
        }
    }

    private AuthInfo inject(ExceptionalSupplier<AuthInfo, AuthenticationException> loginAction) throws AuthenticationException {
        CompletableFuture<String> prefetchedMetaTask = CompletableFuture.supplyAsync(() -> {
            try {
                return server.fetchMetadataResponse();
            } catch (IOException e) {
                throw new CompletionException(new ServerDisconnectException(e));
            }
        });

        CompletableFuture<AuthlibInjectorArtifactInfo> artifactTask = CompletableFuture.supplyAsync(() -> {
            try {
                return downloader.getArtifactInfo();
            } catch (IOException e) {
                throw new CompletionException(new AuthlibInjectorDownloadException(e));
            }
        });

        AuthInfo auth = loginAction.get();
        String prefetchedMeta;
        AuthlibInjectorArtifactInfo artifact;

        try {
            prefetchedMeta = prefetchedMetaTask.get();
            artifact = artifactTask.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof AuthenticationException) {
                throw (AuthenticationException) e.getCause();
            } else {
                throw new AuthenticationException(e.getCause());
            }
        }

        return auth.withArguments(generateArguments(artifact, server, prefetchedMeta));
    }

    private static Arguments generateArguments(AuthlibInjectorArtifactInfo artifact, AuthlibInjectorServer server, String prefetchedMeta) {
        return new Arguments().addJVMArguments(
                "-javaagent:" + artifact.getLocation().toString() + "=" + server.getUrl(),
                "-Dauthlibinjector.side=client",
                "-Dauthlibinjector.yggdrasil.prefetched=" + Base64.getEncoder().encodeToString(prefetchedMeta.getBytes(UTF_8)));
    }

    @Override
    public Map<Object, Object> toStorage() {
        Map<Object, Object> map = super.toStorage();
        map.put("serverBaseURL", server.getUrl());
        return map;
    }

    @Override
    public void clearCache() {
        super.clearCache();
        server.invalidateMetadataCache();
    }

    public AuthlibInjectorServer getServer() {
        return server;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), server.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AuthlibInjectorAccount))
            return false;
        AuthlibInjectorAccount another = (AuthlibInjectorAccount) obj;
        return super.equals(another) && server.equals(another.server);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("username", getUsername())
                .append("server", getServer())
                .toString();
    }
}
