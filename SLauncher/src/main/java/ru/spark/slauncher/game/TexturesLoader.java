package ru.spark.slauncher.game;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.auth.ServerResponseMalformedException;
import ru.spark.slauncher.auth.ely.ElyService;
import ru.spark.slauncher.auth.yggdrasil.*;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.ResourceNotFoundError;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.javafx.BindingMapping;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

/**
 * @author Spark1337
 */
public final class TexturesLoader {

    private static final ThreadPoolExecutor POOL = Lang.threadPool("TexturesDownload", true, 2, 10, TimeUnit.SECONDS);
    private static final Path TEXTURES_DIR = Metadata.MINECRAFT_DIRECTORY.resolve("assets").resolve("skins");
    // ==== Skins ====
    private final static Map<TextureModel, LoadedTexture> DEFAULT_SKINS = new EnumMap<>(TextureModel.class);

    static {
        loadDefaultSkin("/assets/img/steve.png", TextureModel.STEVE);
        loadDefaultSkin("/assets/img/alex.png", TextureModel.ALEX);
    }

    private TexturesLoader() {
    }

    private static Path getTexturePath(Texture texture) {
        String url = texture.getUrl();
        int slash = url.lastIndexOf('/');
        int dot = url.lastIndexOf('.');
        if (dot < slash) {
            dot = url.length();
        }
        String hash = url.substring(slash + 1, dot);
        String prefix = hash.length() > 2 ? hash.substring(0, 2) : "xx";
        return TEXTURES_DIR.resolve(prefix).resolve(hash);
    }

    private static Path getTexturePath(ru.spark.slauncher.auth.ely.Texture texture) {
        String url = texture.getUrl();
        int slash = url.lastIndexOf('/');
        int dot = url.lastIndexOf('.');
        if (dot < slash) {
            dot = url.length();
        }
        String hash = url.substring(slash + 1, dot);
        String prefix = hash.length() > 2 ? hash.substring(0, 2) : "xx";
        return TEXTURES_DIR.resolve(prefix).resolve(hash);
    }

    public static LoadedTexture loadTexture(Texture texture) throws IOException {
        if (StringUtils.isBlank(texture.getUrl())) {
            throw new IOException("Texture url is empty");
        }

        Path file = getTexturePath(texture);
        if (!Files.isRegularFile(file)) {
            // download it
            try {
                new FileDownloadTask(new URL(texture.getUrl()), file.toFile()).run();
                Logging.LOG.info("Texture downloaded: " + texture.getUrl());
            } catch (Exception e) {
                if (Files.isRegularFile(file)) {
                    // concurrency conflict?
                    Logging.LOG.log(Level.WARNING, "Failed to download texture " + texture.getUrl() + ", but the file is available", e);
                } else {
                    throw new IOException("Failed to download texture " + texture.getUrl());
                }
            }
        }

        BufferedImage img;
        try (InputStream in = Files.newInputStream(file)) {
            img = ImageIO.read(in);
        }
        Map<String, String> metadata = texture.getMetadata();
        if (metadata == null) {
            metadata = emptyMap();
        }
        return new LoadedTexture(img, metadata);
    }
    // ====

    public static LoadedTexture loadTexture(ru.spark.slauncher.auth.ely.Texture texture) throws IOException {
        if (StringUtils.isBlank(texture.getUrl())) {
            throw new IOException("Texture url is empty");
        }

        Path file = getTexturePath(texture);
        if (!Files.isRegularFile(file)) {
            // download it
            try {
                new FileDownloadTask(new URL(texture.getUrl()), file.toFile()).run();
                Logging.LOG.info("Texture downloaded: " + texture.getUrl());
            } catch (Exception e) {
                if (Files.isRegularFile(file)) {
                    // concurrency conflict?
                    Logging.LOG.log(Level.WARNING, "Failed to download texture " + texture.getUrl() + ", but the file is available", e);
                } else {
                    throw new IOException("Failed to download texture " + texture.getUrl());
                }
            }
        }

        BufferedImage img;
        try (InputStream in = Files.newInputStream(file)) {
            img = ImageIO.read(in);
        }
        Map<String, String> metadata = texture.getMetadata();
        if (metadata == null) {
            metadata = emptyMap();
        }
        return new LoadedTexture(img, metadata);
    }

    private static void loadDefaultSkin(String path, TextureModel model) {
        try (InputStream in = ResourceNotFoundError.getResourceAsStream(path)) {
            DEFAULT_SKINS.put(model, new LoadedTexture(ImageIO.read(in), singletonMap("model", model.modelName)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static LoadedTexture getDefaultSkin(TextureModel model) {
        return DEFAULT_SKINS.get(model);
    }

    public static ObjectBinding<LoadedTexture> skinBinding(YggdrasilService service, UUID uuid) {
        LoadedTexture uuidFallback = getDefaultSkin(TextureModel.detectUUID(uuid));
        return BindingMapping.of(service.getProfileRepository().binding(uuid))
                .map(profile -> profile
                        .flatMap(it -> {
                            try {
                                return YggdrasilService.getTextures(it);
                            } catch (ServerResponseMalformedException e) {
                                Logging.LOG.log(Level.WARNING, "Failed to parse texture payload", e);
                                return Optional.empty();
                            }
                        })
                        .flatMap(it -> Optional.ofNullable(it.get(TextureType.SKIN)))
                        .filter(it -> StringUtils.isNotBlank(it.getUrl())))
                .asyncMap(it -> {
                    if (it.isPresent()) {
                        Texture texture = it.get();
                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                return loadTexture(texture);
                            } catch (IOException e) {
                                Logging.LOG.log(Level.WARNING, "Failed to load texture " + texture.getUrl() + ", using fallback texture", e);
                                return uuidFallback;
                            }
                        }, POOL);
                    } else {
                        return CompletableFuture.completedFuture(uuidFallback);
                    }
                }, uuidFallback);
    }

    public static ObjectBinding<LoadedTexture> skinBinding(ElyService service, UUID uuid) {
        LoadedTexture uuidFallback = getDefaultSkin(TextureModel.detectUUID(uuid));
        return BindingMapping.of(service.getProfileRepository().binding(uuid))
                .map(profile -> profile
                        .flatMap(it -> {
                            try {
                                return ElyService.getTextures(it);
                            } catch (ServerResponseMalformedException e) {
                                Logging.LOG.log(Level.WARNING, "Failed to parse texture payload", e);
                                return Optional.empty();
                            }
                        })
                        .flatMap(it -> Optional.ofNullable(it.get(TextureType.SKIN)))
                        .filter(it -> StringUtils.isNotBlank(it.getUrl())))
                .asyncMap(it -> {
                    if (it.isPresent()) {
                        ru.spark.slauncher.auth.ely.Texture texture = it.get();
                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                return loadTexture(texture);
                            } catch (IOException e) {
                                Logging.LOG.log(Level.WARNING, "Failed to load texture " + texture.getUrl() + ", using fallback texture", e);
                                return uuidFallback;
                            }
                        }, POOL);
                    } else {
                        return CompletableFuture.completedFuture(uuidFallback);
                    }
                }, uuidFallback);
    }

    // ==== Avatar ====
    public static BufferedImage toAvatar(BufferedImage skin, int size) {
        BufferedImage avatar = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = avatar.createGraphics();

        int scale = skin.getWidth() / 64;
        int faceOffset = (int) Math.round(size / 18.0);
        g.drawImage(skin,
                faceOffset, faceOffset, size - faceOffset, size - faceOffset,
                8 * scale, 8 * scale, 16 * scale, 16 * scale,
                null);
        g.drawImage(skin,
                0, 0, size, size,
                40 * scale, 8 * scale, 48 * scale, 16 * scale, null);

        g.dispose();
        return avatar;
    }
    // ====

    public static ObjectBinding<Image> fxAvatarBinding(YggdrasilService service, UUID uuid, int size) {
        return BindingMapping.of(skinBinding(service, uuid))
                .map(it -> toAvatar(it.image, size))
                .map(img -> SwingFXUtils.toFXImage(img, null));
    }

    public static ObjectBinding<Image> fxAvatarBinding(ElyService service, UUID uuid, int size) {
        return BindingMapping.of(skinBinding(service, uuid))
                .map(it -> toAvatar(it.image, size))
                .map(img -> SwingFXUtils.toFXImage(img, null));
    }

    public static ObjectBinding<Image> fxAvatarBinding(Account account, int size) {
        if (account instanceof YggdrasilAccount) {
            return fxAvatarBinding(((YggdrasilAccount) account).getYggdrasilService(), account.getUUID(), size);
        } else {
            return Bindings.createObjectBinding(
                    () -> SwingFXUtils.toFXImage(toAvatar(getDefaultSkin(TextureModel.detectUUID(account.getUUID())).image, size), null));
        }
    }

    // ==== Texture Loading ====
    public static class LoadedTexture {
        private final BufferedImage image;
        private final Map<String, String> metadata;

        public LoadedTexture(BufferedImage image, Map<String, String> metadata) {
            this.image = requireNonNull(image);
            this.metadata = requireNonNull(metadata);
        }

        public BufferedImage getImage() {
            return image;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }
    }
    // ====
}
