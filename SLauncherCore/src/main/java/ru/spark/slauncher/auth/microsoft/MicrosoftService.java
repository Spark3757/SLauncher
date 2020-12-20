package ru.spark.slauncher.auth.microsoft;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.NoCharacterException;
import ru.spark.slauncher.auth.ServerDisconnectException;
import ru.spark.slauncher.auth.ServerResponseMalformedException;
import ru.spark.slauncher.auth.yggdrasil.RemoteAuthenticationException;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.gson.TolerableValidationException;
import ru.spark.slauncher.util.gson.Validation;
import ru.spark.slauncher.util.io.HttpRequest;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.io.ResponseCodeException;

import static java.util.Objects.requireNonNull;
import static ru.spark.slauncher.util.Lang.mapOf;
import static ru.spark.slauncher.util.Pair.pair;

public class MicrosoftService {
    private static final Pattern OAUTH_URL_PATTERN = Pattern.compile("^https://login\\.live\\.com/oauth20_desktop\\.srf\\?code=(.*?)&lc=(.*?)$");

    private final WebViewCallback callback;

    public MicrosoftService(WebViewCallback callback) {
        this.callback = callback;
    }

    public MicrosoftSession authenticate() throws AuthenticationException {
        requireNonNull(callback);

        try {
            // Microsoft OAuth Flow
            String code = callback.show(this, urlToBeTested -> OAUTH_URL_PATTERN.matcher(urlToBeTested).find(), "https://login.live.com/oauth20_authorize.srf" +
                    "?client_id=00000000402b5328" +
                    "&response_type=code" +
                    "&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL" +
                    "&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf")
                    .thenApply(url -> {
                        Matcher matcher = OAUTH_URL_PATTERN.matcher(url);
                        matcher.find();
                        return matcher.group(1);
                    })
                    .get();

            // Authorization Code -> Token
            String responseText = HttpRequest.POST("https://login.live.com/oauth20_token.srf").form(mapOf(
                    pair("client_id", "00000000402b5328"),
                    pair("code", code),
                    pair("grant_type", "authorization_code"),
                    pair("redirect_uri", "https://login.live.com/oauth20_desktop.srf"),
                    pair("scope", "service::user.auth.xboxlive.com::MBI_SSL"))).getString();
            LiveAuthorizationResponse response = JsonUtils.fromNonNullJson(responseText, LiveAuthorizationResponse.class);

            // Authenticate with XBox Live
            XBoxLiveAuthenticationResponse xboxResponse = HttpRequest.POST("https://user.auth.xboxlive.com/user/authenticate")
                    .json(mapOf(
                            pair("Properties", mapOf(
                                    pair("AuthMethod", "RPS"),
                                    pair("SiteName", "user.auth.xboxlive.com"),
                                    pair("RpsTicket", response.accessToken)
                            )),
                            pair("RelyingParty", "http://auth.xboxlive.com"),
                            pair("TokenType", "JWT")))
                    .getJson(XBoxLiveAuthenticationResponse.class);
            String uhs = (String) xboxResponse.displayClaims.xui.get(0).get("uhs");

            // Authenticate with XSTS
            XBoxLiveAuthenticationResponse xstsResponse = HttpRequest.POST("https://xsts.auth.xboxlive.com/xsts/authorize")
                    .json(mapOf(
                            pair("Properties", mapOf(
                                    pair("SandboxId", "RETAIL"),
                                    pair("UserTokens", Collections.singletonList(xboxResponse.token))
                            )),
                            pair("RelyingParty", "rp://api.minecraftservices.com/"),
                            pair("TokenType", "JWT")))
                    .getJson(XBoxLiveAuthenticationResponse.class);

            // Authenticate with Minecraft
            MinecraftLoginWithXBoxResponse minecraftResponse = HttpRequest.POST("https://api.minecraftservices.com/authentication/login_with_xbox")
                    .json(mapOf(pair("identityToken", "XBL3.0 x=" + uhs + ";" + xstsResponse.token)))
                    .getJson(MinecraftLoginWithXBoxResponse.class);

            // Checking Game Ownership
            MinecraftStoreResponse storeResponse = HttpRequest.GET("https://api.minecraftservices.com/entitlements/mcstore")
                    .authorization(String.format("%s %s", minecraftResponse.tokenType, minecraftResponse.accessToken))
                    .getJson(MinecraftStoreResponse.class);
            handleErrorResponse(storeResponse);
            if (storeResponse.items.isEmpty()) {
                throw new NoCharacterException();
            }

            return new MicrosoftSession(minecraftResponse.tokenType, minecraftResponse.accessToken, new MicrosoftSession.User(minecraftResponse.username), null);
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new ServerDisconnectException(e);
        } catch (JsonParseException e) {
            throw new ServerResponseMalformedException(e);
        }
    }

    public MicrosoftSession refresh(MicrosoftSession oldSession) throws AuthenticationException {
        try {
            // Get the profile
            MinecraftProfileResponse profileResponse = HttpRequest.GET(NetworkUtils.toURL("https://api.minecraftservices.com/minecraft/profile"))
                    .authorization(String.format("%s %s", oldSession.getTokenType(), oldSession.getAccessToken()))
                    .getJson(MinecraftProfileResponse.class);
            handleErrorResponse(profileResponse);
            return new MicrosoftSession(oldSession.getTokenType(), oldSession.getAccessToken(), oldSession.getUser(), new MicrosoftSession.GameProfile(profileResponse.id, profileResponse.name));
        } catch (IOException e) {
            throw new ServerDisconnectException(e);
        } catch (JsonParseException e) {
            throw new ServerResponseMalformedException(e);
        }
    }

    public boolean validate(String tokenType, String accessToken) throws AuthenticationException {
        requireNonNull(tokenType);
        requireNonNull(accessToken);

        try {
            HttpRequest.GET(NetworkUtils.toURL("https://api.minecraftservices.com/minecraft/profile"))
                    .authorization(String.format("%s %s", tokenType, accessToken))
                    .filter((url, responseCode) -> {
                        if (responseCode / 100 == 4) {
                            throw new ResponseCodeException(url, responseCode);
                        }
                    })
                    .getString();
            return true;
        } catch (ResponseCodeException e) {
            return false;
        } catch (IOException e) {
            throw new ServerDisconnectException(e);
        }
    }

    private static void handleErrorResponse(MinecraftErrorResponse response) throws AuthenticationException {
        if (response.error != null) {
            throw new RemoteAuthenticationException(response.error, response.errorMessage, response.developerMessage);
        }
    }

    private static class LiveAuthorizationResponse {
        @SerializedName("token_type")
        String tokenType;

        @SerializedName("expires_in")
        int expiresIn;

        @SerializedName("scope")
        String scope;

        @SerializedName("access_token")
        String accessToken;

        @SerializedName("refresh_token")
        String refreshToken;

        @SerializedName("user_id")
        String userId;

        @SerializedName("foci")
        String foci;
    }

    private static class XBoxLiveAuthenticationResponseDisplayClaims {
        List<Map<Object, Object>> xui;
    }

    private static class XBoxLiveAuthenticationResponse {
        @SerializedName("IssueInstant")
        String issueInstant;

        @SerializedName("NotAfter")
        String notAfter;

        @SerializedName("Token")
        String token;

        @SerializedName("DisplayClaims")
        XBoxLiveAuthenticationResponseDisplayClaims displayClaims;
    }

    private static class MinecraftLoginWithXBoxResponse {
        @SerializedName("username")
        String username;

        @SerializedName("roles")
        List<String> roles;

        @SerializedName("access_token")
        String accessToken;

        @SerializedName("token_type")
        String tokenType;

        @SerializedName("expires_in")
        int expiresIn;
    }

    private static class MinecraftStoreResponseItem {
        @SerializedName("name")
        String name;
        @SerializedName("signature")
        String signature;
    }

    private static class MinecraftStoreResponse extends MinecraftErrorResponse {
        @SerializedName("items")
        List<MinecraftStoreResponseItem> items;

        @SerializedName("signature")
        String signature;

        @SerializedName("keyId")
        String keyId;
    }

    private static class MinecraftProfileResponseSkin implements Validation {
        public String id;
        public String state;
        public String url;
        public String variant;
        public String alias;

        @Override
        public void validate() throws JsonParseException, TolerableValidationException {
            Validation.requireNonNull(id, "id cannot be null");
            Validation.requireNonNull(state, "state cannot be null");
            Validation.requireNonNull(url, "url cannot be null");
            Validation.requireNonNull(variant, "variant cannot be null");
            Validation.requireNonNull(alias, "alias cannot be null");
        }
    }

    private static class MinecraftProfileResponseCape {

    }

    private static class MinecraftProfileResponse extends MinecraftErrorResponse implements Validation {
        @SerializedName("id")
        UUID id;
        @SerializedName("name")
        String name;
        @SerializedName("skins")
        List<MinecraftProfileResponseSkin> skins;
        @SerializedName("capes")
        List<MinecraftProfileResponseCape> capes;

        @Override
        public void validate() throws JsonParseException, TolerableValidationException {
            Validation.requireNonNull(id, "id cannot be null");
            Validation.requireNonNull(name, "name cannot be null");
            Validation.requireNonNull(skins, "skins cannot be null");
            Validation.requireNonNull(capes, "capes cannot be null");
        }
    }

    private static class MinecraftErrorResponse {
        public String path;
        public String errorType;
        public String error;
        public String errorMessage;
        public String developerMessage;
    }

    public interface WebViewCallback {
        CompletableFuture<String> show(MicrosoftService service, Predicate<String> urlTester, String initialURL);
    }

}
