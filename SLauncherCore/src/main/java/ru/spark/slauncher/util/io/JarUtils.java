package ru.spark.slauncher.util.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class JarUtils {

    public static Optional<Path> thisJar() {
        CodeSource codeSource = JarUtils.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            return Optional.empty();
        }

        URL url = codeSource.getLocation();
        if (url == null) {
            return Optional.empty();
        }

        Path path;
        try {
            path = Paths.get(url.toURI());
        } catch (FileSystemNotFoundException | IllegalArgumentException | URISyntaxException e) {
            return Optional.empty();
        }

        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }

        return Optional.of(path);
    }

    public static Optional<Manifest> getManifest(Path jar) {
        try (JarFile file = new JarFile(jar.toFile())) {
            return Optional.ofNullable(file.getManifest());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Optional<String> getImplementationVersion(Path jar) {
        return Optional.of(jar).flatMap(JarUtils::getManifest)
                .flatMap(manifest -> Optional.ofNullable(manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION)));
    }
}
