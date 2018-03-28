package com.github.am4dr.gradle.java_modularize.tooling;

import com.github.am4dr.gradle.java_modularize.testing.target.DependentJars;
import com.github.am4dr.gradle.java_modularize.testing.target.StandaloneJars;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompileModuleInfoJavaTest {

    final TempDirSupport tempDirSupport;
    Path tempDir;

    public CompileModuleInfoJavaTest() throws IOException {
        this.tempDirSupport = new TempDirSupport(false);
    }

    @BeforeEach
    void setup() throws IOException {
        tempDir = tempDirSupport.create(this.getClass());
    }

    @Test
    void compileTest() throws IOException {
        final Path outDir = tempDir.resolve("out");
        final Path infoFile = tempDir.resolve("module-info.java");
        Files.createFile(infoFile);
        Files.write(infoFile, "module sample.unnamed { exports sample; }".getBytes());
        final ToolProviderSupport.Result result = Tooling.compileModuleInfoJava("sample.unnamed", infoFile.toFile(), StandaloneJars.UNNAMED.file, outDir.toFile(), Set.of());
        assertEquals(0, result.exitCode);
        assertTrue(Files.isRegularFile(outDir.resolve("module-info.class")));
    }

    @Test
    void compileDependentModuleInfoTest() throws IOException {
        final Path outDir = tempDir.resolve("out");
        final Path infoFile = tempDir.resolve("module-info.java");
        Files.createFile(infoFile);
        Files.write(infoFile, "module sample.dependent { exports sample.dependent; }".getBytes());
        final ToolProviderSupport.Result result = Tooling.compileModuleInfoJava("sample.dependent", infoFile.toFile(), DependentJars.DEPENDENT.file, outDir.toFile(), Set.of(StandaloneJars.UNNAMED.file));
        assertEquals(0, result.exitCode, String.join("\n\n", result.out, result.err));
        assertTrue(Files.isRegularFile(outDir.resolve("module-info.class")));
    }
}
