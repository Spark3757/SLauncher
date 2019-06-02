package ru.spark.slauncher.upgrade;

import org.tukaani.xz.XZInputStream;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

class SLauncherDownloadTask extends FileDownloadTask {

    private RemoteVersion.Type archiveFormat;

    public SLauncherDownloadTask(RemoteVersion version, Path target) {
        super(NetworkUtils.toURL(version.getUrl()), target.toFile(), version.getIntegrityCheck());
        archiveFormat = version.getType();
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        try {
            Path target = getFile().toPath();

            switch (archiveFormat) {
                case JAR:
                    break;

                case PACK_XZ:
                    byte[] raw = Files.readAllBytes(target);
                    try (InputStream in = new XZInputStream(new ByteArrayInputStream(raw));
                         JarOutputStream out = new JarOutputStream(Files.newOutputStream(target))) {
                        Pack200.newUnpacker().unpack(in, out);
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown format: " + archiveFormat);
            }
        } catch (Throwable e) {
            getFile().delete();
            throw e;
        }
    }

}
