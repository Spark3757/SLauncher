package ru.spark.slauncher.game;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.util.*;
import ru.spark.slauncher.util.gson.Validation;

import java.util.*;
import java.util.logging.Level;

/**
 * @author Spark1337
 */
@Immutable
public class Version implements Comparable<Version>, Validation {

    private final String id;
    private final String minecraftArguments;
    private final Arguments arguments;
    private final String mainClass;
    private final String inheritsFrom;
    private final String jar;
    private final AssetIndexInfo assetIndex;
    private final String assets;
    private final List<Library> libraries;
    private final List<CompatibilityRule> compatibilityRules;
    private final Map<DownloadType, DownloadInfo> downloads;
    private final Map<DownloadType, LoggingInfo> logging;
    private final ReleaseType type;
    private final Date time;
    private final Date releaseTime;
    private final int minimumLauncherVersion;
    private final boolean hidden;

    private transient final boolean resolved;

    public Version(boolean resolved, String id, String minecraftArguments, Arguments arguments, String mainClass, String inheritsFrom, String jar, AssetIndexInfo assetIndex, String assets, List<Library> libraries, List<CompatibilityRule> compatibilityRules, Map<DownloadType, DownloadInfo> downloads, Map<DownloadType, LoggingInfo> logging, ReleaseType type, Date time, Date releaseTime, int minimumLauncherVersion, boolean hidden) {
        this.resolved = resolved;
        this.id = id;
        this.minecraftArguments = minecraftArguments;
        this.arguments = arguments;
        this.mainClass = mainClass;
        this.inheritsFrom = inheritsFrom;
        this.jar = jar;
        this.assetIndex = assetIndex;
        this.assets = assets;
        this.libraries = libraries == null ? new LinkedList<>() : new LinkedList<>(libraries);
        this.compatibilityRules = compatibilityRules == null ? null : new LinkedList<>(compatibilityRules);
        this.downloads = downloads == null ? null : new HashMap<>(downloads);
        this.logging = logging == null ? null : new HashMap<>(logging);
        this.type = type;
        this.time = time == null ? new Date() : (Date) time.clone();
        this.releaseTime = releaseTime == null ? new Date() : (Date) releaseTime.clone();
        this.minimumLauncherVersion = minimumLauncherVersion;
        this.hidden = hidden;
    }

    public Optional<String> getMinecraftArguments() {
        return Optional.ofNullable(minecraftArguments);
    }

    public Version setMinecraftArguments(String minecraftArguments) {
        return new Version(resolved, id, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden);
    }

    public Optional<Arguments> getArguments() {
        return Optional.ofNullable(arguments);
    }

    public Version setArguments(Arguments arguments) {
        return new Version(resolved, id, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden);
    }

    public String getMainClass() {
        return mainClass;
    }

    public Version setMainClass(String mainClass) {
        return new Version(resolved, id, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden);
    }

    public Date getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    public Version setId(String id) {
        return new Version(resolved, id, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden);
    }

    public ReleaseType getType() {
        return type;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public String getJar() {
        return jar;
    }

    public Version setJar(String jar) {
        return new Version(resolved, id, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden);
    }

    public String getInheritsFrom() {
        return inheritsFrom;
    }

    public Version setInheritsFrom(String inheritsFrom) {
        return new Version(resolved, id, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden);
    }

    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }

    public boolean isHidden() {
        return hidden;
    }

    public Map<DownloadType, LoggingInfo> getLogging() {
        return logging == null ? Collections.emptyMap() : Collections.unmodifiableMap(logging);
    }

    public Version setLogging(Map<DownloadType, LoggingInfo> logging) {
        return new Version(resolved, id, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden);
    }

    public List<Library> getLibraries() {
        return libraries == null ? Collections.emptyList() : Collections.unmodifiableList(libraries);
    }

    public Version setLibraries(List<Library> libraries) {
        return new Version(resolved, id, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden);
    }

    public List<CompatibilityRule> getCompatibilityRules() {
        return compatibilityRules == null ? Collections.emptyList() : Collections.unmodifiableList(compatibilityRules);
    }

    public DownloadInfo getDownloadInfo() {
        DownloadInfo client = downloads == null ? null : downloads.get(DownloadType.CLIENT);
        String jarName = jar == null ? id : jar;
        if (client == null)
            return new DownloadInfo(String.format("%s%s/%s.jar", Constants.DEFAULT_VERSION_DOWNLOAD_URL, jarName, jarName));
        else
            return client;
    }

    public AssetIndexInfo getAssetIndex() {
        String assetsId = assets == null ? "legacy" : assets;
        return assetIndex == null ? new AssetIndexInfo(assetsId, Constants.DEFAULT_INDEX_URL + assetsId + ".json") : assetIndex;
    }

    public boolean appliesToCurrentEnvironment() {
        return CompatibilityRule.appliesToCurrentEnvironment(compatibilityRules);
    }

    /**
     * Resolve given version
     */
    public Version resolve(VersionProvider provider) throws VersionNotFoundException {
        return resolve(provider, new HashSet<>()).setResolved();
    }

    protected Version resolve(VersionProvider provider, Set<String> resolvedSoFar) throws VersionNotFoundException {
        if (inheritsFrom == null) {
            return this.jar == null ? this.setJar(id) : this;
        }

        // To maximize the compatibility.
        if (!resolvedSoFar.add(id)) {
            Logging.LOG.log(Level.WARNING, "Found circular dependency versions: " + resolvedSoFar);
            return this;
        }

        // It is supposed to auto install an version in getVersion.
        Version parent = provider.getVersion(inheritsFrom).resolve(provider, resolvedSoFar);
        return new Version(
                true,
                id,
                minecraftArguments == null ? parent.minecraftArguments : minecraftArguments,
                Arguments.merge(parent.arguments, arguments),
                mainClass == null ? parent.mainClass : mainClass,
                null, // inheritsFrom
                jar == null ? parent.jar : jar,
                assetIndex == null ? parent.assetIndex : assetIndex,
                assets == null ? parent.assets : assets,
                Lang.merge(this.libraries, parent.libraries),
                Lang.merge(parent.compatibilityRules, this.compatibilityRules),
                downloads == null ? parent.downloads : downloads,
                logging == null ? parent.logging : logging,
                type,
                time,
                releaseTime,
                Math.max(minimumLauncherVersion, parent.minimumLauncherVersion),
                hidden);
    }

    private Version setResolved() {
        return new Version(true, id, minecraftArguments, arguments, mainClass, inheritsFrom, jar, assetIndex, assets, libraries, compatibilityRules, downloads, logging, type, time, releaseTime, minimumLauncherVersion, hidden);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Version && Objects.equals(id, ((Version) obj).id);
    }

    @Override
    public int compareTo(Version o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).toString();
    }

    @Override
    public void validate() throws JsonParseException {
        if (StringUtils.isBlank(id))
            throw new JsonParseException("Version ID cannot be blank");
        if (downloads != null)
            for (Map.Entry<DownloadType, DownloadInfo> entry : downloads.entrySet()) {
                if (!(entry.getKey() instanceof DownloadType))
                    throw new JsonParseException("Version downloads key must be DownloadType");
                if (!(entry.getValue() instanceof DownloadInfo))
                    throw new JsonParseException("Version downloads value must be DownloadInfo");
            }
        if (logging != null)
            for (Map.Entry<DownloadType, LoggingInfo> entry : logging.entrySet()) {
                if (!(entry.getKey() instanceof DownloadType))
                    throw new JsonParseException("Version logging key must be DownloadType");
                if (!(entry.getValue() instanceof LoggingInfo))
                    throw new JsonParseException("Version logging value must be LoggingInfo");
            }
    }

}
