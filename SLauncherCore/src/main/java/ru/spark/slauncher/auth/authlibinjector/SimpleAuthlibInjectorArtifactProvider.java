package ru.spark.slauncher.auth.authlibinjector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;

import static ru.spark.slauncher.util.Logging.LOG;

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
            LOG.log(Level.WARNING, "Bad authlib-injector artifact", e);
            return Optional.empty();
        }
    }
}
