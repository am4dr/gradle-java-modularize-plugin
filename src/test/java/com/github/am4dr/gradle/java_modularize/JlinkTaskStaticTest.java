package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.util.SampleTargetJars;
import com.github.am4dr.gradle.java_modularize.util.TempDirSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JlinkTaskStaticTest {

    final TempDirSupport tempDirSupport;
    Path tempDir;

    JlinkTaskStaticTest() throws IOException {
        this.tempDirSupport = new TempDirSupport(false);
    }

    @BeforeEach
    void setup() throws IOException {
        tempDir = tempDirSupport.create(JlinkTaskStaticTest.class);
    }

    @Test
    void callJlinkTest() {
        final ToolProviderSupport.Result result = JlinkTask.jlink(List.of("--help"));
    }

    @Test
    void simpleJlinkTest() throws IOException {
        final File destDir = tempDir.resolve("jlink").toFile();
        final List<File> modulePaths = List.of(new File(System.getProperty("java.home") + "/jmods"), SampleTargetJars.NAMED.file);
        final ArrayList<String> args = JlinkTask.createArgs(modulePaths, List.of("sample.named"), "named", "sample.named/sample.named.Sample", destDir, List.of());
        final ToolProviderSupport.Result result = JlinkTask.jlink(args);
        assertEquals(0, result.exitCode, result.out + result.err);
        assertTrue(Files.exists(destDir.toPath()));
        assertTrue(Files.exists(destDir.toPath().resolve("bin/named")));
        assertTrue(Files.exists(destDir.toPath().resolve("bin/named.bat")));
    }
}