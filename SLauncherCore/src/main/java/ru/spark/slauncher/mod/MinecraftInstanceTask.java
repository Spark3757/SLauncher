package ru.spark.slauncher.mod;

import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.DigestUtils;
import ru.spark.slauncher.util.Hex;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

public final class MinecraftInstanceTask<T> extends Task<Void> {

    private final File zipFile;
    private final Charset encoding;
    private final String subDirectory;
    private final File jsonFile;
    private final T manifest;
    private final String type;

    public MinecraftInstanceTask(File zipFile, Charset encoding, String subDirectory, T manifest, String type, File jsonFile) {
        this.zipFile = zipFile;
        this.encoding = encoding;
        this.subDirectory = FileUtils.normalizePath(subDirectory);
        this.manifest = manifest;
        this.jsonFile = jsonFile;
        this.type = type;
    }

    @Override
    public void execute() throws Exception {
        List<ModpackConfiguration.FileInformation> overrides = new LinkedList<>();

        try (FileSystem fs = CompressingUtils.readonly(zipFile.toPath()).setEncoding(encoding).build()) {
            Path root = fs.getPath(subDirectory);

            if (Files.exists(root))
                Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String relativePath = root.relativize(file).normalize().toString().replace(File.separatorChar, '/');
                        overrides.add(new ModpackConfiguration.FileInformation(relativePath, Hex.encodeHex(DigestUtils.digest("SHA-1", file))));
                        return FileVisitResult.CONTINUE;
                    }
                });
        }

        FileUtils.writeText(jsonFile, JsonUtils.GSON.toJson(new ModpackConfiguration<>(manifest, type, overrides)));
    }
}
