package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.util.DependentJar;
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

class CompileModuleInfoJavaTaskStaticTest {

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
    void extractModuleNameTest() throws IOException {
        final Path infoFile = tempDir.resolve("module-info.java");
        Files.createFile(infoFile);
        Files.write(infoFile, "module sample.module { }".getBytes());
        final String name = CompileModuleInfoJavaTask.extractModuleName(infoFile.toFile());
        assertEquals("sample.module", name);
    }

    @Test
    void extractModuleNameTest2() throws IOException {
        final Path infoFile = tempDir.resolve("module-info.java");
        Files.createFile(infoFile);
        Files.write(infoFile, String.join("\n","",
                "module sample.unnamed {",
                "   exports sample;",
                "}").getBytes());
        final String name = CompileModuleInfoJavaTask.extractModuleName(infoFile.toFile());
        assertEquals("sample.unnamed", name);
    }

    @Test
    void compileTest() throws IOException {
        final Path outDir = tempDir.resolve("out");
        final Path infoFile = tempDir.resolve("module-info.java");
        Files.createFile(infoFile);
        Files.write(infoFile, "module sample.unnamed { exports sample; }".getBytes());
        final ToolProviderSupport.Result result = CompileModuleInfoJavaTask.compile("sample.unnamed", infoFile.toFile(), SampleTargetJars.UNNAMED.file, outDir.toFile());
        assertEquals(0, result.exitCode);
        assertTrue(Files.isRegularFile(outDir.resolve("module-info.class")));
    }

    @Test
    void compileDependentModuleInfoTest() throws IOException {
        final Path outDir = tempDir.resolve("out");
        final Path infoFile = tempDir.resolve("module-info.java");
        Files.createFile(infoFile);
        Files.write(infoFile, "module sample.dependent { exports sample.dependent; }".getBytes());
        final ToolProviderSupport.Result result = CompileModuleInfoJavaTask.compile("sample.dependent", infoFile.toFile(), DependentJar.DEPENDENT.file, outDir.toFile());
        assertEquals(0, result.exitCode, String.join("\n\n", result.out, result.err));
        assertTrue(Files.isRegularFile(outDir.resolve("module-info.class")));
    }
}