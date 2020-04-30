package ru.spark.slauncher.upgrade;

import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Pair;
import ru.spark.slauncher.util.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.StandardOpenOption.*;

/**
 * Helper class for adding/removing executable header from SLauncher file.
 *
 * @author spark1337
 */
final class ExecutableHeaderHelper {
    private static Map<String, String> suffix2header = Lang.mapOf(
            Pair.pair("exe", "assets/SLauncher.exe"));

    private ExecutableHeaderHelper() {
    }

    private static Optional<String> getSuffix(Path file) {
        String filename = file.getFileName().toString();
        int idxDot = filename.lastIndexOf('.');
        if (idxDot < 0) {
            return Optional.empty();
        } else {
            return Optional.of(filename.substring(idxDot + 1));
        }
    }

    private static Optional<byte[]> readHeader(ZipFile zip, String suffix) throws IOException {
        String location = suffix2header.get(suffix);
        if (location != null) {
            ZipEntry entry = zip.getEntry(location);
            if (entry != null && !entry.isDirectory()) {
                try (InputStream in = zip.getInputStream(entry)) {
                    return Optional.of(IOUtils.readFullyAsByteArray(in));
                }
            }
        }
        return Optional.empty();
    }

    private static int detectHeaderLength(ZipFile zip, FileChannel channel) throws IOException {
        ByteBuffer buf = channel.map(MapMode.READ_ONLY, 0, channel.size());
        suffixLoop:
        for (String suffix : suffix2header.keySet()) {
            Optional<byte[]> header = readHeader(zip, suffix);
            if (header.isPresent()) {
                buf.rewind();
                for (byte b : header.get()) {
                    if (!buf.hasRemaining() || b != buf.get()) {
                        continue suffixLoop;
                    }
                }
                return header.get().length;
            }
        }
        return 0;
    }

    /**
     * Copies the executable and removes its header.
     */
    public static void copyWithoutHeader(Path from, Path to) throws IOException {
        try (
                FileChannel in = FileChannel.open(from, READ);
                FileChannel out = FileChannel.open(to, CREATE, WRITE, TRUNCATE_EXISTING);
                ZipFile zip = new ZipFile(from.toFile())
        ) {
            in.transferTo(detectHeaderLength(zip, in), Long.MAX_VALUE, out);
        }
    }

    /**
     * Copies the executable and appends the header according to the suffix.
     */
    public static void copyWithHeader(Path from, Path to) throws IOException {
        try (
                FileChannel in = FileChannel.open(from, READ);
                FileChannel out = FileChannel.open(to, CREATE, WRITE, TRUNCATE_EXISTING);
                ZipFile zip = new ZipFile(from.toFile())
        ) {
            Optional<String> suffix = getSuffix(to);
            if (suffix.isPresent()) {
                Optional<byte[]> header = readHeader(zip, suffix.get());
                if (header.isPresent()) {
                    out.write(ByteBuffer.wrap(header.get()));
                }
            }

            in.transferTo(detectHeaderLength(zip, in), Long.MAX_VALUE, out);
        }
    }
}
