package com.github.am4dr.gradle.java_modularize.tooling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolingTest {

    final TempDirSupport tempDirSupport;
    Path tempDir;

    public ToolingTest() throws IOException {
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
}