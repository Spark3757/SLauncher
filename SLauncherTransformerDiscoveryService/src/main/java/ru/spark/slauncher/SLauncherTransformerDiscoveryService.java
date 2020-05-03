package ru.spark.slauncher;

import cpw.mods.modlauncher.serviceapi.ITransformerDiscoveryService;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SLauncherTransformerDiscoveryService implements ITransformerDiscoveryService {
    private static final String CANDIDATES = System.getProperty("slauncher.transformer.candidates");

    @Override
    public List<Path> candidates(Path gameDirectory) {
        return Arrays.stream(CANDIDATES.split(File.pathSeparator))
                .flatMap(path -> {
                    try {
                        return Stream.of(Paths.get(path));
                    } catch (InvalidPathException e) {
                        return Stream.of();
                    }
                }).collect(Collectors.toList());
    }
}