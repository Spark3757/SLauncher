package ru.spark.slauncher.setting;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.game.GameDirectoryType;
import ru.spark.slauncher.game.LaunchOptions;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.platform.JavaVersion;
import ru.spark.slauncher.util.platform.OperatingSystem;
import ru.spark.slauncher.util.platform.Platform;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author spark1337
 */
@JsonAdapter(VersionSetting.Serializer.class)
public final class VersionSetting implements Cloneable {

    private final BooleanProperty usesGlobalProperty = new SimpleBooleanProperty(this, "usesGlobal", false);
    private final StringProperty javaProperty = new SimpleStringProperty(this, "java", "");
    private final StringProperty defaultJavaPathProperty = new SimpleStringProperty(this, "defaultJavaPath", "");
    private final StringProperty javaDirProperty = new SimpleStringProperty(this, "javaDir", "");
    private final StringProperty wrapperProperty = new SimpleStringProperty(this, "wrapper", "");
    private final StringProperty permSizeProperty = new SimpleStringProperty(this, "permSize", "");
    private final IntegerProperty maxMemoryProperty = new SimpleIntegerProperty(this, "maxMemory", OperatingSystem.SUGGESTED_MEMORY);
    /**
     * The minimum memory that JVM can allocate for heap.
     */
    private final ObjectProperty<Integer> minMemoryProperty = new SimpleObjectProperty<>(this, "minMemory", null);

    // java
    private final StringProperty preLaunchCommandProperty = new SimpleStringProperty(this, "precalledCommand", "");
    private final StringProperty javaArgsProperty = new SimpleStringProperty(this, "javaArgs", "");
    private final StringProperty minecraftArgsProperty = new SimpleStringProperty(this, "minecraftArgs", "");
    private final BooleanProperty noJVMArgsProperty = new SimpleBooleanProperty(this, "noJVMArgs", false);
    private final BooleanProperty notCheckJVMProperty = new SimpleBooleanProperty(this, "notCheckJVM", false);
    private final BooleanProperty notCheckGameProperty = new SimpleBooleanProperty(this, "notCheckGame", false);
    private final BooleanProperty showLogsProperty = new SimpleBooleanProperty(this, "showLogs", false);
    private final StringProperty serverIpProperty = new SimpleStringProperty(this, "serverIp", "");
    private final BooleanProperty fullscreenProperty = new SimpleBooleanProperty(this, "fullscreen", false);
    private final IntegerProperty widthProperty = new SimpleIntegerProperty(this, "width", 854);
    private final IntegerProperty heightProperty = new SimpleIntegerProperty(this, "height", 480);
    /**
     * 0 - .minecraft<br/>
     * 1 - .minecraft/versions/&lt;version&gt;/<br/>
     */
    private final ObjectProperty<GameDirectoryType> gameDirTypeProperty = new SimpleObjectProperty<>(this, "gameDirType", GameDirectoryType.ROOT_FOLDER);
    /**
     * Your custom gameDir
     */
    private final StringProperty gameDirProperty = new SimpleStringProperty(this, "gameDir", "");
    /**
     * 0 - Close the launcher when the game starts.<br/>
     * 1 - Hide the launcher when the game starts.<br/>
     * 2 - Keep the launcher open.<br/>
     */
    private final ObjectProperty<LauncherVisibility> launcherVisibilityProperty = new SimpleObjectProperty<>(this, "launcherVisibility", LauncherVisibility.HIDE);
    public transient String id;
    private boolean global = false;

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public BooleanProperty usesGlobalProperty() {
        return usesGlobalProperty;
    }

    /**
     * SLauncher Version Settings have been divided into 2 parts.
     * 1. Global settings.
     * 2. Version settings.
     * If a version claims that it uses global settings, its version setting will be disabled.
     * <p>
     * Defaults false because if one version uses global first, custom version file will not be generated.
     */
    public boolean isUsesGlobal() {
        return usesGlobalProperty.get();
    }

    public void setUsesGlobal(boolean usesGlobal) {
        usesGlobalProperty.set(usesGlobal);
    }

    public StringProperty javaProperty() {
        return javaProperty;
    }

    /**
     * Java version or "Custom" if user customizes java directory, "Default" if the jvm that this app relies on.
     */
    public String getJava() {
        return javaProperty.get();
    }

    public void setJava(String java) {
        javaProperty.set(java);
    }

    public boolean isUsesCustomJavaDir() {
        return "Custom".equals(getJava());
    }

    public void setUsesCustomJavaDir() {
        setJava("Custom");
        setDefaultJavaPath(null);
    }

    /**
     * Path to Java executable, or null if user customizes java directory.
     * It's used to determine which JRE to use when multiple JREs match the selected Java version.
     */
    public String getDefaultJavaPath() {
        return defaultJavaPathProperty.get();
    }

    public void setDefaultJavaPath(String defaultJavaPath) {
        defaultJavaPathProperty.set(defaultJavaPath);
    }

    public StringProperty javaDirProperty() {
        return javaDirProperty;
    }

    /**
     * User customized java directory or null if user uses system Java.
     */
    public String getJavaDir() {
        return javaDirProperty.get();
    }

    public void setJavaDir(String javaDir) {
        javaDirProperty.set(javaDir);
    }

    public StringProperty wrapperProperty() {
        return wrapperProperty;
    }

    /**
     * The command to launch java, i.e. optirun.
     */
    public String getWrapper() {
        return wrapperProperty.get();
    }

    // options

    public void setWrapper(String wrapper) {
        wrapperProperty.set(wrapper);
    }

    public StringProperty permSizeProperty() {
        return permSizeProperty;
    }

    /**
     * The permanent generation size of JVM garbage collection.
     */
    public String getPermSize() {
        return permSizeProperty.get();
    }

    public void setPermSize(String permSize) {
        permSizeProperty.set(permSize);
    }

    public IntegerProperty maxMemoryProperty() {
        return maxMemoryProperty;
    }

    /**
     * The maximum memory/MB that JVM can allocate for heap.
     */
    public int getMaxMemory() {
        return maxMemoryProperty.get();
    }

    public void setMaxMemory(int maxMemory) {
        maxMemoryProperty.set(maxMemory);
    }

    public ObjectProperty<Integer> minMemoryProperty() {
        return minMemoryProperty;
    }

    public Integer getMinMemory() {
        return minMemoryProperty.get();
    }

    public void setMinMemory(Integer minMemory) {
        minMemoryProperty.set(minMemory);
    }

    public StringProperty preLaunchCommandProperty() {
        return preLaunchCommandProperty;
    }

    /**
     * The command that will be executed before launching the Minecraft.
     * Operating system relevant.
     */
    public String getPreLaunchCommand() {
        return preLaunchCommandProperty.get();
    }

    public void setPreLaunchCommand(String preLaunchCommand) {
        preLaunchCommandProperty.set(preLaunchCommand);
    }

    public StringProperty javaArgsProperty() {
        return javaArgsProperty;
    }

    /**
     * The user customized arguments passed to JVM.
     */
    public String getJavaArgs() {
        return javaArgsProperty.get();
    }

    public void setJavaArgs(String javaArgs) {
        javaArgsProperty.set(javaArgs);
    }

    public StringProperty minecraftArgsProperty() {
        return minecraftArgsProperty;
    }

    /**
     * The user customized arguments passed to Minecraft.
     */
    public String getMinecraftArgs() {
        return minecraftArgsProperty.get();
    }

    public void setMinecraftArgs(String minecraftArgs) {
        minecraftArgsProperty.set(minecraftArgs);
    }

    public BooleanProperty noJVMArgsProperty() {
        return noJVMArgsProperty;
    }

    /**
     * True if disallow SLauncher use default JVM arguments.
     */
    public boolean isNoJVMArgs() {
        return noJVMArgsProperty.get();
    }

    public void setNoJVMArgs(boolean noJVMArgs) {
        noJVMArgsProperty.set(noJVMArgs);
    }

    public BooleanProperty notCheckJVMProperty() {
        return notCheckJVMProperty;
    }

    /**
     * True if SLauncher does not check JVM validity.
     */
    public boolean isNotCheckJVM() {
        return notCheckJVMProperty.get();
    }

    // Minecraft settings.

    public void setNotCheckJVM(boolean notCheckJVM) {
        notCheckJVMProperty.set(notCheckJVM);
    }

    public BooleanProperty notCheckGameProperty() {
        return notCheckGameProperty;
    }

    /**
     * True if SLauncher does not check game's completeness.
     */
    public boolean isNotCheckGame() {
        return notCheckGameProperty.get();
    }

    public void setNotCheckGame(boolean notCheckGame) {
        notCheckGameProperty.set(notCheckGame);
    }

    public BooleanProperty showLogsProperty() {
        return showLogsProperty;
    }

    /**
     * True if show the logs after game launched.
     */
    public boolean isShowLogs() {
        return showLogsProperty.get();
    }

    public void setShowLogs(boolean showLogs) {
        showLogsProperty.set(showLogs);
    }

    public StringProperty serverIpProperty() {
        return serverIpProperty;
    }

    /**
     * The server ip that will be entered after Minecraft successfully loaded ly.
     * <p>
     * Format: ip:port or without port.
     */
    public String getServerIp() {
        return serverIpProperty.get();
    }

    public void setServerIp(String serverIp) {
        serverIpProperty.set(serverIp);
    }

    public BooleanProperty fullscreenProperty() {
        return fullscreenProperty;
    }

    /**
     * True if Minecraft started in fullscreen mode.
     */
    public boolean isFullscreen() {
        return fullscreenProperty.get();
    }

    public void setFullscreen(boolean fullscreen) {
        fullscreenProperty.set(fullscreen);
    }

    public IntegerProperty widthProperty() {
        return widthProperty;
    }

    /**
     * The width of Minecraft window, defaults 800.
     * <p>
     * The field saves int value.
     * String type prevents unexpected value from JsonParseException.
     * We can only reset this field instead of recreating the whole setting file.
     */
    public int getWidth() {
        return widthProperty.get();
    }

    public void setWidth(int width) {
        widthProperty.set(width);
    }

    public IntegerProperty heightProperty() {
        return heightProperty;
    }

    /**
     * The height of Minecraft window, defaults 480.
     * <p>
     * The field saves int value.
     * String type prevents unexpected value from JsonParseException.
     * We can only reset this field instead of recreating the whole setting file.
     */
    public int getHeight() {
        return heightProperty.get();
    }

    public void setHeight(int height) {
        heightProperty.set(height);
    }

    public ObjectProperty<GameDirectoryType> gameDirTypeProperty() {
        return gameDirTypeProperty;
    }

    public GameDirectoryType getGameDirType() {
        return gameDirTypeProperty.get();
    }

    public void setGameDirType(GameDirectoryType gameDirType) {
        gameDirTypeProperty.set(gameDirType);
    }

    public StringProperty gameDirProperty() {
        return gameDirProperty;
    }

    public String getGameDir() {
        return gameDirProperty.get();
    }

    // launcher settings

    public void setGameDir(String gameDir) {
        gameDirProperty.set(gameDir);
    }

    public ObjectProperty<LauncherVisibility> launcherVisibilityProperty() {
        return launcherVisibilityProperty;
    }

    public LauncherVisibility getLauncherVisibility() {
        return launcherVisibilityProperty.get();
    }

    public void setLauncherVisibility(LauncherVisibility launcherVisibility) {
        launcherVisibilityProperty.set(launcherVisibility);
    }

    public JavaVersion getJavaVersion() throws InterruptedException {
        return getJavaVersion(true);
    }

    public void setJavaVersion(JavaVersion java) {
        setJava(java.getVersion());
        setDefaultJavaPath(java.getBinary().toString());
    }

    public JavaVersion getJavaVersion(boolean checkJava) throws InterruptedException {
        // TODO: lazy initialization may result in UI suspension.
        if (StringUtils.isBlank(getJava()))
            setJava(StringUtils.isBlank(getJavaDir()) ? "Default" : "Custom");
        if ("Default".equals(getJava())) return JavaVersion.fromCurrentEnvironment();
        else if (isUsesCustomJavaDir()) {
            try {
                if (checkJava)
                    return JavaVersion.fromExecutable(Paths.get(getJavaDir()));
                else
                    return new JavaVersion(Paths.get(getJavaDir()), "", Platform.PLATFORM);
            } catch (IOException | InvalidPathException e) {
                return null; // Custom Java Directory not found,
            }
        } else if (StringUtils.isNotBlank(getJava())) {
            List<JavaVersion> matchedJava = JavaVersion.getJavas().stream()
                    .filter(java -> java.getVersion().equals(getJava()))
                    .collect(Collectors.toList());
            if (matchedJava.isEmpty()) {
                setJava("Default");
                return JavaVersion.fromCurrentEnvironment();
            } else {
                return matchedJava.stream()
                        .filter(java -> java.getBinary().toString().equals(getDefaultJavaPath()))
                        .findFirst()
                        .orElse(matchedJava.get(0));
            }
        } else throw new Error();
    }

    public void addPropertyChangedListener(InvalidationListener listener) {
        usesGlobalProperty.addListener(listener);
        javaProperty.addListener(listener);
        javaDirProperty.addListener(listener);
        wrapperProperty.addListener(listener);
        permSizeProperty.addListener(listener);
        maxMemoryProperty.addListener(listener);
        minMemoryProperty.addListener(listener);
        preLaunchCommandProperty.addListener(listener);
        javaArgsProperty.addListener(listener);
        minecraftArgsProperty.addListener(listener);
        noJVMArgsProperty.addListener(listener);
        notCheckGameProperty.addListener(listener);
        notCheckJVMProperty.addListener(listener);
        showLogsProperty.addListener(listener);
        serverIpProperty.addListener(listener);
        fullscreenProperty.addListener(listener);
        widthProperty.addListener(listener);
        heightProperty.addListener(listener);
        gameDirTypeProperty.addListener(listener);
        gameDirProperty.addListener(listener);
        launcherVisibilityProperty.addListener(listener);
        defaultJavaPathProperty.addListener(listener);
    }

    public LaunchOptions toLaunchOptions(File gameDir, boolean checkJava) throws InterruptedException {
        JavaVersion javaVersion = Optional.ofNullable(getJavaVersion(checkJava)).orElse(JavaVersion.fromCurrentEnvironment());
        LaunchOptions.Builder builder = new LaunchOptions.Builder()
                .setGameDir(gameDir)
                .setJava(javaVersion)
                .setVersionName(Metadata.TITLE)
                .setVersionType(Metadata.TITLE)
                .setProfileName(Metadata.TITLE)
                .setMinecraftArgs(getMinecraftArgs())
                .setJavaArgs(Paths.get(OperatingSystem.getWorkingDirectory("SLauncher").toAbsolutePath() + "/brander.jar").toFile().exists() ?
                        "-javaagent:" + OperatingSystem.getWorkingDirectory("SLauncher").toAbsolutePath() + "/brander.jar " + getJavaArgs() : getJavaArgs())
                .setMaxMemory(getMaxMemory())
                .setMinMemory(getMinMemory())
                .setMetaspace(Lang.toIntOrNull(getPermSize()))
                .setWidth(getWidth())
                .setHeight(getHeight())
                .setFullscreen(isFullscreen())
                .setServerIp(getServerIp())
                .setWrapper(getWrapper())
                .setPrecalledCommand(getPreLaunchCommand())
                .setNoGeneratedJVMArgs(isNoJVMArgs());
        if (ConfigHolder.config().hasProxy()) {
            builder.setProxyHost(ConfigHolder.config().getProxyHost());
            builder.setProxyPort(ConfigHolder.config().getProxyPort());
            if (ConfigHolder.config().hasProxyAuth()) {
                builder.setProxyUser(ConfigHolder.config().getProxyUser());
                builder.setProxyPass(ConfigHolder.config().getProxyPass());
            }
        }
        return builder.create();
    }

    @Override
    public VersionSetting clone() {
        VersionSetting versionSetting = new VersionSetting();
        versionSetting.setUsesGlobal(isUsesGlobal());
        versionSetting.setJava(getJava());
        versionSetting.setDefaultJavaPath(getDefaultJavaPath());
        versionSetting.setJavaDir(getJavaDir());
        versionSetting.setWrapper(getWrapper());
        versionSetting.setPermSize(getPermSize());
        versionSetting.setMaxMemory(getMaxMemory());
        versionSetting.setMinMemory(getMinMemory());
        versionSetting.setPreLaunchCommand(getPreLaunchCommand());
        versionSetting.setJavaArgs(getJavaArgs());
        versionSetting.setMinecraftArgs(getMinecraftArgs());
        versionSetting.setNoJVMArgs(isNoJVMArgs());
        versionSetting.setNotCheckGame(isNotCheckGame());
        versionSetting.setNotCheckJVM(isNotCheckJVM());
        versionSetting.setShowLogs(isShowLogs());
        versionSetting.setServerIp(getServerIp());
        versionSetting.setFullscreen(isFullscreen());
        versionSetting.setWidth(getWidth());
        versionSetting.setHeight(getHeight());
        versionSetting.setGameDirType(getGameDirType());
        versionSetting.setGameDir(getGameDir());
        versionSetting.setLauncherVisibility(getLauncherVisibility());
        return versionSetting;
    }

    public static class Serializer implements JsonSerializer<VersionSetting>, JsonDeserializer<VersionSetting> {
        @Override
        public JsonElement serialize(VersionSetting src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) return JsonNull.INSTANCE;
            JsonObject obj = new JsonObject();

            obj.addProperty("usesGlobal", src.isUsesGlobal());
            obj.addProperty("javaArgs", src.getJavaArgs());
            obj.addProperty("minecraftArgs", src.getMinecraftArgs());
            obj.addProperty("maxMemory", src.getMaxMemory() <= 0 ? OperatingSystem.SUGGESTED_MEMORY : src.getMaxMemory());
            obj.addProperty("minMemory", src.getMinMemory());
            obj.addProperty("permSize", src.getPermSize());
            obj.addProperty("width", src.getWidth());
            obj.addProperty("height", src.getHeight());
            obj.addProperty("javaDir", src.getJavaDir());
            obj.addProperty("precalledCommand", src.getPreLaunchCommand());
            obj.addProperty("serverIp", src.getServerIp());
            obj.addProperty("java", src.getJava());
            obj.addProperty("wrapper", src.getWrapper());
            obj.addProperty("fullscreen", src.isFullscreen());
            obj.addProperty("noJVMArgs", src.isNoJVMArgs());
            obj.addProperty("notCheckGame", src.isNotCheckGame());
            obj.addProperty("notCheckJVM", src.isNotCheckJVM());
            obj.addProperty("showLogs", src.isShowLogs());
            obj.addProperty("gameDir", src.getGameDir());
            obj.addProperty("launcherVisibility", src.getLauncherVisibility().ordinal());
            obj.addProperty("gameDirType", src.getGameDirType().ordinal());
            obj.addProperty("defaultJavaPath", src.getDefaultJavaPath());

            return obj;
        }

        @Override
        public VersionSetting deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == JsonNull.INSTANCE || !(json instanceof JsonObject))
                return null;
            JsonObject obj = (JsonObject) json;

            int maxMemoryN = parseJsonPrimitive(Optional.ofNullable(obj.get("maxMemory")).map(JsonElement::getAsJsonPrimitive).orElse(null), OperatingSystem.SUGGESTED_MEMORY);
            if (maxMemoryN <= 0) maxMemoryN = OperatingSystem.SUGGESTED_MEMORY;

            VersionSetting vs = new VersionSetting();

            vs.setUsesGlobal(Optional.ofNullable(obj.get("usesGlobal")).map(JsonElement::getAsBoolean).orElse(false));
            vs.setJavaArgs(Optional.ofNullable(obj.get("javaArgs")).map(JsonElement::getAsString).orElse(""));
            vs.setMinecraftArgs(Optional.ofNullable(obj.get("minecraftArgs")).map(JsonElement::getAsString).orElse(""));
            vs.setMaxMemory(maxMemoryN);
            vs.setMinMemory(Optional.ofNullable(obj.get("minMemory")).map(JsonElement::getAsInt).orElse(null));
            vs.setPermSize(Optional.ofNullable(obj.get("permSize")).map(JsonElement::getAsString).orElse(""));
            vs.setWidth(Optional.ofNullable(obj.get("width")).map(JsonElement::getAsJsonPrimitive).map(this::parseJsonPrimitive).orElse(0));
            vs.setHeight(Optional.ofNullable(obj.get("height")).map(JsonElement::getAsJsonPrimitive).map(this::parseJsonPrimitive).orElse(0));
            vs.setJavaDir(Optional.ofNullable(obj.get("javaDir")).map(JsonElement::getAsString).orElse(""));
            vs.setPreLaunchCommand(Optional.ofNullable(obj.get("precalledCommand")).map(JsonElement::getAsString).orElse(""));
            vs.setServerIp(Optional.ofNullable(obj.get("serverIp")).map(JsonElement::getAsString).orElse(""));
            vs.setJava(Optional.ofNullable(obj.get("java")).map(JsonElement::getAsString).orElse(""));
            vs.setWrapper(Optional.ofNullable(obj.get("wrapper")).map(JsonElement::getAsString).orElse(""));
            vs.setGameDir(Optional.ofNullable(obj.get("gameDir")).map(JsonElement::getAsString).orElse(""));
            vs.setFullscreen(Optional.ofNullable(obj.get("fullscreen")).map(JsonElement::getAsBoolean).orElse(false));
            vs.setNoJVMArgs(Optional.ofNullable(obj.get("noJVMArgs")).map(JsonElement::getAsBoolean).orElse(false));
            vs.setNotCheckGame(Optional.ofNullable(obj.get("notCheckGame")).map(JsonElement::getAsBoolean).orElse(false));
            vs.setNotCheckJVM(Optional.ofNullable(obj.get("notCheckJVM")).map(JsonElement::getAsBoolean).orElse(false));
            vs.setShowLogs(Optional.ofNullable(obj.get("showLogs")).map(JsonElement::getAsBoolean).orElse(false));
            vs.setLauncherVisibility(LauncherVisibility.values()[Optional.ofNullable(obj.get("launcherVisibility")).map(JsonElement::getAsInt).orElse(1)]);
            vs.setGameDirType(GameDirectoryType.values()[Optional.ofNullable(obj.get("gameDirType")).map(JsonElement::getAsInt).orElse(0)]);
            vs.setDefaultJavaPath(Optional.ofNullable(obj.get("defaultJavaPath")).map(JsonElement::getAsString).orElse(null));

            return vs;
        }

        private int parseJsonPrimitive(JsonPrimitive primitive) {
            return parseJsonPrimitive(primitive, 0);
        }

        private int parseJsonPrimitive(JsonPrimitive primitive, int defaultValue) {
            if (primitive == null)
                return defaultValue;
            else if (primitive.isNumber())
                return primitive.getAsInt();
            else
                return Lang.parseInt(primitive.getAsString(), defaultValue);
        }
    }
}
