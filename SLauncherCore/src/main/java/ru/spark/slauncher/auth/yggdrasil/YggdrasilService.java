package ru.spark.slauncher.auth.yggdrasil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.ServerDisconnectException;
import ru.spark.slauncher.auth.ServerResponseMalformedException;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.Pair;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;
import ru.spark.slauncher.util.gson.ValidationTypeAdapterFactory;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.io.HttpMultipartRequest;
import ru.spark.slauncher.util.io.IOUtils;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.javafx.ObservableOptionalCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableList;

public class YggdrasilService {

    private static final ThreadPoolExecutor POOL = Lang.threadPool("YggdrasilProfileProperties", true, 2, 10, TimeUnit.SECONDS);

    public static final YggdrasilService MOJANG = new YggdrasilService(new MojangYggdrasilProvider());

    private final YggdrasilProvider provider;
    private final ObservableOptionalCache<UUID, CompleteGameProfile, AuthenticationException> profileRepository;

    public YggdrasilService(YggdrasilProvider provider) {
        this.provider = provider;
        this.profileRepository = new ObservableOptionalCache<>(
                uuid -> {
                    Logging.LOG.info("Fetching properties of " + uuid + " from " + provider);
                    return getCompleteGameProfile(uuid);
                },
                (uuid, e) -> Logging.LOG.log(Level.WARNING, "Failed to fetch properties of " + uuid + " from " + provider, e),
                POOL);
    }

    public ObservableOptionalCache<UUID, CompleteGameProfile, AuthenticationException> getProfileRepository() {
        return profileRepository;
    }

    public YggdrasilSession authenticate(String username, String password, String clientToken) throws AuthenticationException {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        Objects.requireNonNull(clientToken);

        Map<String, Object> request = new HashMap<>();
        request.put("agent", Lang.mapOf(
                Pair.pair("name", "Minecraft"),
                Pair.pair("version", 1)
        ));
        request.put("username", username);
        request.put("password", password);
        request.put("clientToken", clientToken);
        request.put("requestUser", true);

        return handleAuthenticationResponse(request(provider.getAuthenticationURL(), request), clientToken);
    }

    private static Map<String, Object> createRequestWithCredentials(String accessToken, String clientToken) {
        Map<String, Object> request = new HashMap<>();
        request.put("accessToken", accessToken);
        request.put("clientToken", clientToken);
        return request;
    }

    public YggdrasilSession refresh(String accessToken, String clientToken, GameProfile characterToSelect) throws AuthenticationException {
        Objects.requireNonNull(accessToken);
        Objects.requireNonNull(clientToken);

        Map<String, Object> request = createRequestWithCredentials(accessToken, clientToken);
        request.put("requestUser", true);

        if (characterToSelect != null) {
            request.put("selectedProfile", Lang.mapOf(
                    Pair.pair("id", characterToSelect.getId()),
                    Pair.pair("name", characterToSelect.getName())));
        }

        YggdrasilSession response = handleAuthenticationResponse(request(provider.getRefreshmentURL(), request), clientToken);

        if (characterToSelect != null) {
            if (response.getSelectedProfile() == null ||
                    !response.getSelectedProfile().getId().equals(characterToSelect.getId())) {
                throw new ServerResponseMalformedException("Failed to select character");
            }
        }

        return response;
    }

    public boolean validate(String accessToken) throws AuthenticationException {
        return validate(accessToken, null);
    }

    public boolean validate(String accessToken, String clientToken) throws AuthenticationException {
        Objects.requireNonNull(accessToken);

        try {
            requireEmpty(request(provider.getValidationURL(), createRequestWithCredentials(accessToken, clientToken)));
            return true;
        } catch (RemoteAuthenticationException e) {
            if ("ForbiddenOperationException".equals(e.getRemoteName())) {
                return false;
            }
            throw e;
        }
    }

    public void invalidate(String accessToken) throws AuthenticationException {
        invalidate(accessToken, null);
    }

    public void invalidate(String accessToken, String clientToken) throws AuthenticationException {
        Objects.requireNonNull(accessToken);

        requireEmpty(request(provider.getInvalidationURL(), createRequestWithCredentials(accessToken, clientToken)));
    }

    public void uploadSkin(UUID uuid, String accessToken, String model, Path file) throws AuthenticationException, UnsupportedOperationException {
        try {
            HttpURLConnection con = NetworkUtils.createHttpConnection(provider.getSkinUploadURL(uuid));
            con.setRequestMethod("PUT");
            con.setRequestProperty("Authorization", "Bearer " + accessToken);
            con.setDoOutput(true);
            try (HttpMultipartRequest request = new HttpMultipartRequest(con)) {
                request.param("model", model);
                try (InputStream fis = Files.newInputStream(file)) {
                    request.file("file", FileUtils.getName(file), "image/" + FileUtils.getExtension(file), fis);
                }
            }
            String response = IOUtils.readFullyAsString(con.getInputStream());
            if (response.startsWith("{")) {
                handleErrorMessage(fromJson(response, ErrorResponse.class));
            }
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }
    }


    /**
     * Get complete game profile.
     * <p>
     * Game profile provided from authentication is not complete (no skin data in properties).
     *
     * @param uuid the uuid that the character corresponding to.
     * @return the complete game profile(filled with more properties)
     */
    public Optional<CompleteGameProfile> getCompleteGameProfile(UUID uuid) throws AuthenticationException {
        Objects.requireNonNull(uuid);

        return Optional.ofNullable(fromJson(request(provider.getProfilePropertiesURL(uuid), null), CompleteGameProfile.class));
    }

    public static Optional<Map<TextureType, Texture>> getTextures(CompleteGameProfile profile) throws ServerResponseMalformedException {
        Objects.requireNonNull(profile);

        String encodedTextures = profile.getProperties().get("textures");

        if (encodedTextures != null) {
            byte[] decodedBinary;
            try {
                decodedBinary = Base64.getDecoder().decode(encodedTextures);
            } catch (IllegalArgumentException e) {
                throw new ServerResponseMalformedException(e);
            }
            TextureResponse texturePayload = fromJson(new String(decodedBinary, UTF_8), TextureResponse.class);
            return Optional.ofNullable(texturePayload.textures);
        } else {
            return Optional.empty();
        }
    }

    private static YggdrasilSession handleAuthenticationResponse(String responseText, String clientToken) throws AuthenticationException {
        AuthenticationResponse response = fromJson(responseText, AuthenticationResponse.class);
        handleErrorMessage(response);

        if (!clientToken.equals(response.clientToken))
            throw new AuthenticationException("Client token changed from " + clientToken + " to " + response.clientToken);

        return new YggdrasilSession(
                response.clientToken,
                response.accessToken,
                response.selectedProfile,
                response.availableProfiles == null ? null : unmodifiableList(response.availableProfiles),
                response.user);
    }

    private static void requireEmpty(String response) throws AuthenticationException {
        if (StringUtils.isBlank(response))
            return;

        try {
            handleErrorMessage(fromJson(response, ErrorResponse.class));
        } catch (JsonParseException e) {
            throw new ServerResponseMalformedException(e);
        }
    }

    private static void handleErrorMessage(ErrorResponse response) throws AuthenticationException {
        if (!StringUtils.isBlank(response.error)) {
            throw new RemoteAuthenticationException(response.error, response.errorMessage, response.cause);
        }
    }

    private static String request(URL url, Object payload) throws AuthenticationException {
        try {
            if (payload == null)
                return NetworkUtils.doGet(url);
            else
                return NetworkUtils.doPost(url, payload instanceof String ? (String) payload : GSON.toJson(payload), "application/json");
        } catch (IOException e) {
            throw new ServerDisconnectException(e);
        }
    }

    private static <T> T fromJson(String text, Class<T> typeOfT) throws ServerResponseMalformedException {
        try {
            return GSON.fromJson(text, typeOfT);
        } catch (JsonParseException e) {
            throw new ServerResponseMalformedException(e);
        }
    }

    private static class TextureResponse {
        public Map<TextureType, Texture> textures;
    }

    private static class AuthenticationResponse extends ErrorResponse {
        public String accessToken;
        public String clientToken;
        public GameProfile selectedProfile;
        public List<GameProfile> availableProfiles;
        public User user;
    }

    private static class ErrorResponse {
        public String error;
        public String errorMessage;
        public String cause;
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UUID.class, UUIDTypeAdapter.INSTANCE)
            .registerTypeAdapterFactory(ValidationTypeAdapterFactory.INSTANCE)
            .create();

}
