package ru.spark.slauncher.util;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.upgrade.RemoteVersion;
import ru.spark.slauncher.util.platform.OperatingSystem;


public class Analytics {

    private static SentryClient sentry;

    public static void init() {
        Sentry.init("https://2650db753c7b408480b64c7ee0423416@sentry.io/1425014");
    }

    public static void recordLauncherStart() {
        Sentry.capture(new EventBuilder().
                withRelease(Metadata.VERSION).
                withLevel(Event.Level.INFO).
                withMessage("launcher_start").
                withTag("java_version", System.getProperty("java.version")).
                withTag("os", System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION).
                withTag("locale", ConfigHolder.config().getLocalization().getLocale().toLanguageTag()).
                build());
    }

    public static void recordMinecraftVersionLaunch(Version version) {
        Sentry.capture(new EventBuilder().
                withRelease(Metadata.VERSION).
                withLevel(Event.Level.INFO).
                withMessage("version_launch").
                withTag("mc_version", version.getId()).
                withTag("version", Metadata.VERSION).
                withTag("java_version", System.getProperty("java.version")).
                withTag("os", System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION).
                withTag("locale", ConfigHolder.config().getLocalization().getLocale().toLanguageTag()).
                build());
    }

    public static void recordLauncherUpgrade(RemoteVersion remoteVersion) {
        Sentry.capture(new EventBuilder().
                withRelease(Metadata.VERSION).
                withLevel(Event.Level.INFO).
                withMessage("launcher_upgrade").
                withTag("remote_version", remoteVersion.getVersion()).
                withTag("old_version", Metadata.VERSION).
                withTag("java_version", System.getProperty("java.version")).
                withTag("os", System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION).
                withTag("locale", ConfigHolder.config().getLocalization().getLocale().toLanguageTag()).
                build());
    }

    public static void recordLauncherCrash(Thread thread, String crashMessage) {
        Sentry.capture(new EventBuilder().
                withRelease(Metadata.VERSION).
                withLevel(Event.Level.ERROR).
                withMessage("launcher_crashed").
                withTag("version", Metadata.VERSION).
                withTag("thread", thread.toString()).
                withTag("stackTrace", crashMessage).
                withTag("os", System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION).
                withTag("java_version", System.getProperty("java.version")).
                withTag("java_vm_version", System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), ").
                withTag("jvm_max_memory", Runtime.getRuntime().maxMemory() + "").
                withTag("jvm_total_memory", Runtime.getRuntime().totalMemory() + "").
                withTag("jvm_free_memory", Runtime.getRuntime().freeMemory() + "").
                build());
    }


}
