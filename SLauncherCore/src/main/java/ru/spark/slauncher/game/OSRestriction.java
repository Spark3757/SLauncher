package ru.spark.slauncher.game;

import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.platform.OperatingSystem;

import java.util.regex.Pattern;

/**
 * @author Spark1337
 */
public final class OSRestriction {

    private final OperatingSystem name;
    private final String version;
    private final String arch;

    public OSRestriction() {
        this(OperatingSystem.UNKNOWN);
    }

    public OSRestriction(OperatingSystem name) {
        this(name, null);
    }

    public OSRestriction(OperatingSystem name, String version) {
        this(name, version, null);
    }

    public OSRestriction(OperatingSystem name, String version, String arch) {
        this.name = name;
        this.version = version;
        this.arch = arch;
    }

    public OperatingSystem getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getArch() {
        return arch;
    }

    public boolean allow() {
        if (name != OperatingSystem.UNKNOWN && name != OperatingSystem.CURRENT_OS)
            return false;

        if (version != null)
            if (Lang.test(() -> !Pattern.compile(version).matcher(OperatingSystem.SYSTEM_VERSION).matches()))
                return false;

        if (arch != null)
            return !Lang.test(() -> !Pattern.compile(arch).matcher(OperatingSystem.SYSTEM_ARCHITECTURE).matches());

        return true;
    }

}
