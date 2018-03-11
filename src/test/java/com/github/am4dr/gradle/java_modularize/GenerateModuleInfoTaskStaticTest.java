package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.util.DependentJar;
import com.github.am4dr.gradle.java_modularize.util.SampleTargetJars;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateModuleInfoTaskStaticTest {

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
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test
    void jdepsToolProviderTest() {
        final ToolProviderSupport.Result jdeps = ToolProviderSupport.run("jdeps", "--version");
        assertEquals(0, jdeps.exitCode);
    }

    @Test
    void jdepsGenerateFromUnnamed() {
        final ToolProviderSupport.Result result = executeGenerateMethod(false, SampleTargetJars.UNNAMED.file);
        assertEquals(0, result.exitCode);
        final Path moduleInfoJava = tempDir.resolve("test.target.sample/module-info.java");
        assertTrue(Files.exists(moduleInfoJava));
    }

    @Test
    void jdepsGenerateFromAutonamed() {
        final ToolProviderSupport.Result result = executeGenerateMethod(false, SampleTargetJars.AUTONAMED.file);
        assertEquals(0, result.exitCode);
        final Path moduleInfoJava = tempDir.resolve("sample.autonamed/module-info.java");
        assertTrue(Files.exists(moduleInfoJava));
    }

    @Test
    void jdepsGenerateFromNamedMayBeFail() {
        final ToolProviderSupport.Result result = executeGenerateMethod(false, SampleTargetJars.NAMED.file);
        assertEquals(1, result.exitCode);
    }

    @Test
    void jdepsGenerateFromDependentJar() {
        final ToolProviderSupport.Result failure = executeGenerateMethod(false, DependentJar.DEPENDENT.file);
        assertEquals(1, failure.exitCode, String.join("\n\n", failure.out, failure.err));

        final ToolProviderSupport.Result result = executeGenerateMethod(false, DependentJar.DEPENDENT.file, Set.of(SampleTargetJars.UNNAMED.file));
        assertEquals(0, result.exitCode, String.join("\n\n", result.out, result.err));

        final Path moduleInfoJava = tempDir.resolve("test.target.dependent.sample/module-info.java");
        assertTrue(Files.exists(moduleInfoJava));
    }

    ToolProviderSupport.Result executeGenerateMethod(boolean isOpenModule, File targetJar) {
        return executeGenerateMethod(isOpenModule, targetJar, Set.of());
    }

    ToolProviderSupport.Result executeGenerateMethod(boolean isOpenModule, File targetJar, Set<File> dependencies) {
        return GenerateModuleInfoTask.generateModuleInfoJava(isOpenModule, targetJar, tempDir.toFile(), dependencies);
    }
}
