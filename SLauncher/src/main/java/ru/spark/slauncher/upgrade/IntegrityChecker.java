package ru.spark.slauncher.upgrade;

import ru.spark.slauncher.util.DigestUtils;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.IOUtils;
import ru.spark.slauncher.util.io.JarUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A class that checks the integrity of SLauncher.
 *
 * @author spark1337
 */
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
        PublicKey publickey = getPublicKey();

        byte[] signature = null;
        Map<String, byte[]> fileFingerprints = new TreeMap<>();
        try (ZipFile zip = new ZipFile(jarPath.toFile())) {
            for (ZipEntry entry : zip.stream().toArray(ZipEntry[]::new)) {
                String filename = entry.getName();
                try (InputStream in = zip.getInputStream(entry)) {
                    if (in == null) {
                        throw new IOException("entry is null");
                    }

                    if (SIGNATURE_FILE.equals(filename)) {
                        signature = IOUtils.readFullyAsByteArray(in);
                    } else {
                        fileFingerprints.put(filename, DigestUtils.digest("SHA-512", in));
                    }
                }
            }
        }

        if (signature == null) {
            throw new IOException("Signature is missing");
        }

        try {
            Signature verifier = Signature.getInstance("SHA512withRSA");
            verifier.initVerify(publickey);
            for (Entry<String, byte[]> entry : fileFingerprints.entrySet()) {
                verifier.update(DigestUtils.digest("SHA-512", entry.getKey().getBytes(UTF_8)));
                verifier.update(entry.getValue());
            }
            return verifier.verify(signature);
        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to verify signature", e);
        }
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
        if (selfVerified != null) {
            return selfVerified;
        }
        try {
            verifySelf();
            Logging.LOG.info("Successfully verified current JAR");
            selfVerified = true;
        } catch (IOException e) {
            Logging.LOG.log(Level.WARNING, "Failed to verify myself, is the JAR corrupt?", e);
            selfVerified = false;
        }
        return selfVerified;
    }

    private static void verifySelf() throws IOException {
        Path self = JarUtils.thisJar().orElseThrow(() -> new IOException("Failed to find current SLauncher location"));
        requireVerifiedJar(self);
    }
}
