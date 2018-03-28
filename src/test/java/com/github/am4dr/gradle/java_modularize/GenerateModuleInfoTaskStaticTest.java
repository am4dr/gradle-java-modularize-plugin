package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.tooling.ToolProviderSupport;
import com.github.am4dr.gradle.java_modularize.tooling.Tooling;
import com.github.am4dr.gradle.java_modularize.util.DependentJar;
import com.github.am4dr.gradle.java_modularize.util.SampleTargetJars;
import com.github.am4dr.gradle.java_modularize.util.TempDirSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateModuleInfoTaskStaticTest {

    final TempDirSupport tempDirSupport;
    Path tempDir;

    public GenerateModuleInfoTaskStaticTest() throws IOException {
        this.tempDirSupport = new TempDirSupport(false);
    }

    @BeforeEach
    void setup() throws IOException {
        tempDir = tempDirSupport.create(this.getClass());
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

        final Path moduleInfoJava = tempDir.resolve("test.dependent.target.sample/module-info.java");
        assertTrue(Files.exists(moduleInfoJava), String.join("\n\n", result.out, result.err));
    }

    @Test
    void multipleDependencyTargetTest() {
        final ToolProviderSupport.Result result = executeGenerateMethod(false, DependentJar.DEPENDENT.file, Set.of(SampleTargetJars.UNNAMED.file, SampleTargetJars.NAMED.file));
        assertEquals(0, result.exitCode, String.join("\n\n", result.out, result.err));

        final Path moduleInfoJava = tempDir.resolve("test.dependent.target.sample/module-info.java");
        assertTrue(Files.exists(moduleInfoJava), String.join("\n\n", result.out, result.err));
    }

    ToolProviderSupport.Result executeGenerateMethod(boolean isOpenModule, File targetJar) {
        return executeGenerateMethod(isOpenModule, targetJar, Set.of());
    }

    ToolProviderSupport.Result executeGenerateMethod(boolean isOpenModule, File targetJar, Set<File> dependencies) {
        return Tooling.generateModuleInfoJava(isOpenModule, targetJar, tempDir.toFile(), dependencies);
    }
}
