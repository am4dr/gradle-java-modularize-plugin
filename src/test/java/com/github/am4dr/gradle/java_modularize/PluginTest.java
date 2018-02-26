package com.github.am4dr.gradle.java_modularize;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PluginTest {

    private GradleBuildSupport build;
    private GradleRunner runner;

    @BeforeAll
    static void createTempBuildRootDir() throws IOException {
        GradleBuildSupport.setupBuildParentDirectory();
    }

    @BeforeEach
    void setupBuildDir() throws IOException {
        build = new GradleBuildSupport();
        build.append(
                "plugins {",
                "   id '" + GradleJavaModularizePlugin.PLUGIN_ID + "'",
                "}"
        );
        runner = build.runner;
    }

    @Test
    void testRuntimeTest() {
        assertTrue(true);
    }

    @Test
    void pluginApplyByIdTest() {
        runner.build();
    }
}
