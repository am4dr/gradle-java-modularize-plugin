package com.github.am4dr.gradle.java_modularize.testing.target;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandaloneJarsTest {

    @Test
    void test() {
        final URL resource = this.getClass().getClassLoader().getResource(StandaloneJars.UNNAMED.file.getName());
        assertNotNull(resource);
    }

    @Test
    void namedModuleContainsModuleInfo() throws IOException {
        final boolean hasModuleInfo = new JarFile(StandaloneJars.NAMED.file).stream()
                .map(JarEntry::getName)
                .anyMatch("module-info.class"::equals);
        assertTrue(hasModuleInfo);
    }
}