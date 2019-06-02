package ru.spark.slauncher.util.platform;

import ru.spark.slauncher.util.Lang;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

/**
 * Represents the operating system.
 *
 * @author Spark1337
 */
public enum OperatingSystem {
    /**
     * Microsoft Windows.
     */
    WINDOWS("windows"),
    /**
     * Linux and Unix like OS, including Solaris.
     */
    LINUX("linux"),
    /**
     * Mac OS X.
     */
    OSX("osx"),
    /**
     * Unknown operating system.
     */
    UNKNOWN("universal");

    /**
     * The current operating system.
     */
    public static final OperatingSystem CURRENT_OS;
    /**
     * The total memory/MB this computer have.
     */
    public static final int TOTAL_MEMORY;
    /**
     * The suggested memory size/MB for Minecraft to allocate.
     */
    public static final int SUGGESTED_MEMORY;
    public static final String PATH_SEPARATOR = File.pathSeparator;
    public static final String FILE_SEPARATOR = File.separator;
    public static final String LINE_SEPARATOR = System.lineSeparator();
    /**
     * The system default encoding.
     */
    public static final String ENCODING = System.getProperty("sun.jnu.encoding", Charset.defaultCharset().name());
    /**
     * The version of current operating system.
     */
    public static final String SYSTEM_VERSION = System.getProperty("os.version");
    /**
     * The architecture of current operating system.
     */
    public static final String SYSTEM_ARCHITECTURE;

    static {
        String name = System.getProperty("os.name").toLowerCase(Locale.US);
        if (name.contains("win"))
            CURRENT_OS = WINDOWS;
        else if (name.contains("mac"))
            CURRENT_OS = OSX;
        else if (name.contains("solaris") || name.contains("linux") || name.contains("unix") || name.contains("sunos"))
            CURRENT_OS = LINUX;
        else
            CURRENT_OS = UNKNOWN;

        TOTAL_MEMORY = getTotalPhysicalMemorySize()
                .map(bytes -> (int) (bytes / 1024 / 1024))
                .orElse(1024);

        SUGGESTED_MEMORY = (int) (Math.round(1.0 * TOTAL_MEMORY / 4.0 / 128.0) * 128);

        String arch = System.getProperty("sun.arch.data.model");
        if (arch == null)
            arch = System.getProperty("os.arch");
        SYSTEM_ARCHITECTURE = arch;
    }

    private final String checkedName;

    OperatingSystem(String checkedName) {
        this.checkedName = checkedName;
    }

    private static Optional<Long> getTotalPhysicalMemorySize() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            Object attribute = mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "TotalPhysicalMemorySize");
            if (attribute instanceof Long) {
                return Optional.of((Long) attribute);
            }
        } catch (JMException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public static void forceGC() {
        System.gc();
        System.runFinalization();
        System.gc();
    }

    public static Path getWorkingDirectory(String folder) {
        String home = System.getProperty("user.home", ".");
        switch (OperatingSystem.CURRENT_OS) {
            case LINUX:
                return Paths.get(home, "." + folder);
            case WINDOWS:
                String appdata = System.getenv("APPDATA");
                return Paths.get(Lang.nonNull(appdata, home), "." + folder);
            case OSX:
                return Paths.get(home, "Library", "Application Support", folder);
            default:
                return Paths.get(home, folder);
        }
    }

    public String getCheckedName() {
        return checkedName;
    }
}
