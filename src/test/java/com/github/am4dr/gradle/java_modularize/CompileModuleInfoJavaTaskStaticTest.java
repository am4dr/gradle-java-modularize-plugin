package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.tooling.ToolProviderSupport;
import com.github.am4dr.gradle.java_modularize.tooling.Tooling;
import com.github.am4dr.gradle.java_modularize.util.DependentJar;
import com.github.am4dr.gradle.java_modularize.util.SampleTargetJars;
import com.github.am4dr.gradle.java_modularize.util.TempDirSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompileModuleInfoJavaTaskStaticTest {

    final TempDirSupport tempDirSupport;
    Path tempDir;

    public CompileModuleInfoJavaTaskStaticTest() throws IOException {
        this.tempDirSupport = new TempDirSupport(false);
    }

    @BeforeEach
    void setup() throws IOException {
        tempDir = tempDirSupport.create(this.getClass());
    }

    @Test
    void extractModuleNameTest() throws IOException {
        final Path infoFile = tempDir.resolve("module-info.java");
        Files.createFile(infoFile);
        Files.write(infoFile, "module sample.module { }".getBytes());
        final String name = Tooling.extractModuleName(infoFile.toFile());
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
        final String name = Tooling.extractModuleName(infoFile.toFile());
        assertEquals("sample.unnamed", name);
    }

    @Test
    void compileTest() throws IOException {
        final Path outDir = tempDir.resolve("out");
        final Path infoFile = tempDir.resolve("module-info.java");
        Files.createFile(infoFile);
        Files.write(infoFile, "module sample.unnamed { exports sample; }".getBytes());
        final ToolProviderSupport.Result result = Tooling.compileModuleInfoJava("sample.unnamed", infoFile.toFile(), SampleTargetJars.UNNAMED.file, outDir.toFile(), Set.of());
        assertEquals(0, result.exitCode);
        assertTrue(Files.isRegularFile(outDir.resolve("module-info.class")));
    }

    @Test
    void compileDependentModuleInfoTest() throws IOException {
        final Path outDir = tempDir.resolve("out");
        final Path infoFile = tempDir.resolve("module-info.java");
        Files.createFile(infoFile);
        Files.write(infoFile, "module sample.dependent { exports sample.dependent; }".getBytes());
        final ToolProviderSupport.Result result = Tooling.compileModuleInfoJava("sample.dependent", infoFile.toFile(), DependentJar.DEPENDENT.file, outDir.toFile(), Set.of(SampleTargetJars.UNNAMED.file));
        assertEquals(0, result.exitCode, String.join("\n\n", result.out, result.err));
        assertTrue(Files.isRegularFile(outDir.resolve("module-info.class")));
    }
}