package com.github.am4dr.gradle.java_modularize.tooling;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class TempDirSupport {

    public static final Path DEFAULT_TEMP_ROOT_DIR = Paths.get("build", "tmp", "test");
    public final Path tempDirRoot;
    public final boolean deleteOnExit;

    public TempDirSupport(boolean deleteOnExit) throws IOException {
        this(DEFAULT_TEMP_ROOT_DIR, deleteOnExit);
    }

    public TempDirSupport(Path root, boolean deleteOnExit) throws IOException {
        this.tempDirRoot = Files.createDirectories(root);
        this.deleteOnExit = deleteOnExit;
    }

    public Path create(Class<?> klass) throws IOException {
        return create(klass.getSimpleName());
    }

    public Path create(String prefix) throws IOException {
        final Path tempDirectory = Files.createTempDirectory(tempDirRoot, prefix);
        if (deleteOnExit) {
            cleanTempDir(tempDirectory);
        }
        return tempDirectory;
    }

    void cleanTempDir(Path dir) throws IOException {
        Files.walkFileTree(dir, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                file.toFile().deleteOnExit();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                dir.toFile().deleteOnExit();
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
