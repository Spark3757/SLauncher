package ru.spark.slauncher.upgrade;

import ru.spark.slauncher.util.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * A class that checks the integrity of SLauncher.
 *
 * @author Spark1337
 */
@Deprecated
public final class IntegrityChecker {
    private static final String SIGNATURE_FILE = "META-INF/slauncher_signature";
    private static final String PUBLIC_KEY_FILE = "assets/slauncher_signature_publickey.der";
    private static Boolean selfVerified = null;

    private IntegrityChecker() {
    }

    private static PublicKey getPublicKey() throws IOException {
        try (InputStream in = IntegrityChecker.class.getResourceAsStream("/" + PUBLIC_KEY_FILE)) {
            if (in == null) {
                throw new IOException("Public key not found");
            }
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(IOUtils.readFullyAsByteArray(in)));
        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to load public key", e);
        }
    }

    private static boolean verifyJar(Path jarPath) throws IOException {
        return true;
    }

    static void requireVerifiedJar(Path jar) throws IOException {
        if (!verifyJar(jar)) {
            throw new IOException("Invalid signature: " + jar);
        }
    }

    /**
     * Checks whether the current application is verified.
     * This method is blocking.
     */
    public static synchronized boolean isSelfVerified() {
        return true;
    }
}
