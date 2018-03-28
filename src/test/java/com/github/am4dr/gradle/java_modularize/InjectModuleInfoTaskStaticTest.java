package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.tooling.ToolProviderSupport;
import com.github.am4dr.gradle.java_modularize.tooling.Tooling;
import com.github.am4dr.gradle.java_modularize.util.SampleTargetJars;
import com.github.am4dr.gradle.java_modularize.util.TempDirSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InjectModuleInfoTaskStaticTest {

    final TempDirSupport tempDirSupport;
    Path tempDir;

    public InjectModuleInfoTaskStaticTest() throws IOException {
        this.tempDirSupport = new TempDirSupport(false);
    }

    @BeforeEach
    void setup() throws IOException {
        tempDir = tempDirSupport.create(this.getClass());
    }

    @Test
    void injectionTest() throws IOException {
        final Path outDir = tempDir.resolve("out");
        final Path infoFile = tempDir.resolve("module-info.class");
        Files.createFile(infoFile);
        Files.write(infoFile, this.getClass().getResourceAsStream("module-info.class").readAllBytes());
        final ToolProviderSupport.Result result = Tooling.injectModuleInfo(SampleTargetJars.UNNAMED.file, infoFile.toFile(), tempDir.toFile(), outDir.toFile());

        assertEquals(0, result.exitCode, result.out + result.err);
        final Path injected = outDir.resolve(SampleTargetJars.UNNAMED.file.getName());
        assertTrue(Files.isRegularFile(injected));
    }
}