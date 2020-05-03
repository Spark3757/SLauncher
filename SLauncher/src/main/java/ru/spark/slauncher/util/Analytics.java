package ru.spark.slauncher.util;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.UserBuilder;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.upgrade.RemoteVersion;
import ru.spark.slauncher.util.platform.OperatingSystem;

import java.security.MessageDigest;


public class Analytics {

    private static String id = "null";

    public static void init() {
        id = getComputerIdentifier();
        Sentry.init("http://c07e192100814ed8b65352fe31756447@49.12.96.164:9000/2");
        Sentry.getContext().setUser(new UserBuilder().setId(id).build());
    }

    public static void recordLauncherInstall() {
        Sentry.getContext().setUser(new UserBuilder().setId(id).build());
        Sentry.getContext().addTag("java_version", System.getProperty("java.version"));
        Sentry.getContext().addTag("os", System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION);
        Sentry.getContext().addTag("locale", ConfigHolder.config().getLocalization().getLocale().toLanguageTag());
        Sentry.getContext().addTag("version", Metadata.VERSION);

        Sentry.getStoredClient().sendEvent(
                new EventBuilder().
                        withMessage("launcher_install").
                        withRelease(Metadata.VERSION).
                        withServerName(System.getenv("COMPUTERNAME")).
                        withLevel(Event.Level.INFO));
    }

    public static void recordLauncherStart() {
        Sentry.clearContext();
        Sentry.getContext().setUser(new UserBuilder().setId(id).build());
        Sentry.getContext().addTag("java_version", System.getProperty("java.version"));
        Sentry.getContext().addTag("os", System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION);
        Sentry.getContext().addTag("locale", ConfigHolder.config().getLocalization().getLocale().toLanguageTag());
        Sentry.getContext().addTag("version", Metadata.VERSION);


        Sentry.getStoredClient().sendEvent(
                new EventBuilder().
                        withMessage("launcher_start").
                        withRelease(Metadata.VERSION).
                        withServerName(System.getenv("COMPUTERNAME")).
                        withLevel(Event.Level.INFO));
    }

    public static void recordMinecraftVersionLaunch(Version version) {
        Sentry.clearContext();
        Sentry.getContext().setUser(new UserBuilder().setId(id).build());
        Sentry.getContext().addTag("java_version", System.getProperty("java.version"));
        Sentry.getContext().addTag("os", System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION);
        Sentry.getContext().addTag("locale", ConfigHolder.config().getLocalization().getLocale().toLanguageTag());
        Sentry.getContext().addTag("version", Metadata.VERSION);
        Sentry.getContext().addTag("mc_version", version.getId());



        Sentry.getStoredClient().sendEvent(
                new EventBuilder().
                        withRelease(Metadata.VERSION).
                        withLevel(Event.Level.INFO).
                        withServerName(System.getenv("COMPUTERNAME")).
                        withMessage("version_launch").
                        build());
    }

    public static void recordLauncherUpgrade(RemoteVersion remoteVersion) {
        Sentry.clearContext();
        Sentry.getContext().setUser(new UserBuilder().setId(id).build());
        Sentry.getContext().addTag("java_version", System.getProperty("java.version"));
        Sentry.getContext().addTag("os", System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION);
        Sentry.getContext().addTag("locale", ConfigHolder.config().getLocalization().getLocale().toLanguageTag());
        Sentry.getContext().addTag("version", Metadata.VERSION);
        Sentry.getContext().addTag("remote_version", remoteVersion.getVersion());


        Sentry.getStoredClient().sendEvent(
                new EventBuilder().
                        withRelease(Metadata.VERSION).
                        withLevel(Event.Level.INFO).
                        withServerName(System.getenv("COMPUTERNAME")).
                        withMessage("launcher_upgrade").
                        build());
    }

    public static void recordLauncherCrash(Thread thread, String crashMessage) {

        Sentry.clearContext();
        Sentry.getContext().setUser(new UserBuilder().setId(id).build());
        Sentry.getContext().addTag("java_version", System.getProperty("java.version"));
        Sentry.getContext().addTag("os", System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION);
        Sentry.getContext().addTag("locale", ConfigHolder.config().getLocalization().getLocale().toLanguageTag());
        Sentry.getContext().addTag("version", Metadata.VERSION);

        Sentry.getContext().addExtra("thread", thread.toString());
        Sentry.getContext().addExtra("stackTrace", crashMessage);
        Sentry.getContext().addExtra("java_vm_version", System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), ");
        Sentry.getContext().addExtra("jvm_max_memory", Runtime.getRuntime().maxMemory() + "");
        Sentry.getContext().addExtra("jvm_total_memory", Runtime.getRuntime().totalMemory() + "");
        Sentry.getContext().addExtra("jvm_free_memory", Runtime.getRuntime().freeMemory() + "");

        Sentry.getStoredClient().sendEvent(
                new EventBuilder().
                        withRelease(Metadata.VERSION).
                        withLevel(Event.Level.ERROR).
                        withServerName(System.getenv("COMPUTERNAME")).
                        withMessage("launcher_crashed").
                        build());
    }

    public static String getComputerIdentifier() {
        try{
            String toEncrypt =  System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuffer hexString = new StringBuffer();

            byte byteData[] = md.digest();

            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return String.format("%08x", "unknown".hashCode());
        }
    }

}