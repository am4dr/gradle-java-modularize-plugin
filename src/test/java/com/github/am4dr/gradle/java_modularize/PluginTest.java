package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.util.GradleBuildSupport;
import com.github.am4dr.gradle.java_modularize.util.SampleTargetJars;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

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
        build = new GradleBuildSupport(this.getClass());
        build.append(
                "plugins {",
                "   id '" + GradleJavaModularizePlugin.PLUGIN_ID + "'",
                "}",
                "repositories { mavenLocal() }"
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

    @Test
    void emptyExtensionBlockTest() throws IOException {
        build.append("",
                "modularize {",
                "}"
        ).build();
    }
    @Test
    void emptyModulesBlockTest() throws IOException {
        build.append("",
                "modularize {",
                "   modules {",
                "       sampleModule {}",
                "   }",
                "}"
        ).build();
    }

    @Test
    void modulesBlockTest() throws IOException {
        build.append("",
                "modularize {",
                "   modules {",
                "       sampleModule {",
                "           descriptor = '" + SampleTargetJars.UNNAMED.id + "'",
                "       }",
                "   }",
                "}"
        ).build();
    }

    @Test
    void extensionModuleMethodTest() throws IOException {
        build.append("",
                "modularize {",
                "   module 'sampleModule', '" + SampleTargetJars.UNNAMED.id + "'",
                "}"
        ).build();
    }

    @Test
    void configurationTest() throws IOException {
        final BuildResult result = build.append("",
                "modularize {",
                "   module 'sampleModule1', '" + SampleTargetJars.UNNAMED.id + "'",
                "   module 'sampleModule2', '" + SampleTargetJars.UNNAMED.id + "'",
                "}",
                "task show {",
                "   doLast { project.configurations.forEach(System.out.&println) }",
                "}"
        ).runner(r -> r.withArguments("show", "-q")).build();
        final String[] lines = result.getOutput().split("\n");
        assertTrue(Arrays.stream(lines).anyMatch(it -> it.contains("sampleModule1")));
        assertTrue(Arrays.stream(lines).anyMatch(it -> it.contains("sampleModule2")));
    }
}
