package ru.spark.slauncher.auth.authlibinjector;

import ru.spark.slauncher.util.Logging;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;

public class SimpleAuthlibInjectorArtifactProvider implements AuthlibInjectorArtifactProvider {

    private Path location;

    public SimpleAuthlibInjectorArtifactProvider(Path location) {
        this.location = location;
    }

    @Override
    public AuthlibInjectorArtifactInfo getArtifactInfo() throws IOException {
        return AuthlibInjectorArtifactInfo.from(location);
    }

    @Override
    public Optional<AuthlibInjectorArtifactInfo> getArtifactInfoImmediately() {
        try {
            return Optional.of(getArtifactInfo());
        } catch (IOException e) {
            Logging.LOG.log(Level.WARNING, "Bad authlib-injector artifact", e);
            return Optional.empty();
        }
    }
}
