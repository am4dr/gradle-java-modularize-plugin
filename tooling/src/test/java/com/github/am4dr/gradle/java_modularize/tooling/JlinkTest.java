package com.github.am4dr.gradle.java_modularize.tooling;

import com.github.am4dr.gradle.java_modularize.testing.target.StandaloneJars;
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

public class JlinkTest {

    final TempDirSupport tempDirSupport;
    Path tempDir;

    public JlinkTest() throws IOException {
        this.tempDirSupport = new TempDirSupport(false);
    }

    @BeforeEach
    void setup() throws IOException {
        tempDir = tempDirSupport.create(this.getClass());
    }

    @Test
    void callJlinkTest() {
        final ToolProviderSupport.Result result = Tooling.jlink(List.of("--help"));
    }

    @Test
    void simpleJlinkTest() throws IOException {
        final File destDir = tempDir.resolve("jlink").toFile();
        final List<File> modulePaths = List.of(new File(System.getProperty("java.home") + "/jmods"), StandaloneJars.NAMED.file);
        final ArrayList<String> args = Tooling.createArgs(modulePaths, List.of("sample.named"), "named", "sample.named/sample.named.Sample", destDir, List.of());
        final ToolProviderSupport.Result result = Tooling.jlink(args);
        assertEquals(0, result.exitCode, result.out + result.err);
        assertTrue(Files.exists(destDir.toPath()));
        assertTrue(Files.exists(destDir.toPath().resolve("bin/named")));
        assertTrue(Files.exists(destDir.toPath().resolve("bin/named.bat")));
    }
}
