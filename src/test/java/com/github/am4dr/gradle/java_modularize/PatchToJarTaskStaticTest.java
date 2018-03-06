package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.util.SampleTargetJars;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatchToJarTaskStaticTest {

    static final Path tmpTestDir = Paths.get("build", "tmp", "test");
    Path tempDir;

    @BeforeAll
    static void beforeAll() throws IOException {
        Files.createDirectories(tmpTestDir);
    }

    @BeforeEach
    void beforeEach() throws IOException {
        tempDir = Files.createTempDirectory(tmpTestDir, this.getClass().getSimpleName());
        tempDir.toFile().deleteOnExit();
    }

    @AfterEach
    void cleanTempDir() throws IOException {
        Files.walkFileTree(tempDir, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
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

    @Test
    void patchingTest() throws IOException {
        final Path outDir = tempDir.resolve("out");
        final Path infoFile = tempDir.resolve("module-info.class");
        Files.createFile(infoFile);
        final ToolProviderSupport.Result result = PatchToJarTask.patch(SampleTargetJars.UNNAMED.file, infoFile.toFile(), tempDir.toFile(), outDir.toFile());

        assertEquals(0, result.exitCode);
        final Path patched = outDir.resolve(SampleTargetJars.UNNAMED.file.getName());
        assertTrue(Files.isRegularFile(patched));
    }
}