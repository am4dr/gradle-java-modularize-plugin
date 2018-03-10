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
        build = new GradleBuildSupport(this.getClass(), false);
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
                "           descriptors = ['" + SampleTargetJars.UNNAMED.id + "']",
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

    @Test
    void tasksIntegrationTest() throws IOException {
        final BuildResult result = build.append("import java.util.jar.JarFile",
                "modularize {",
                "   module 'sampleModule', '" + SampleTargetJars.UNNAMED.id + "'",
                "}",
                "task show {",
                "   dependsOn tasks.modularize",
                "   doLast {",
                "       def file = project.configurations.getByName('sampleModule').artifacts.files.files[0]",
                "       println file",
                "       JarFile jarfile = new JarFile(file)",
                "       jarfile.stream().forEach(System.out.&println)",
                "   }",
                "}"
        ).runner(r -> r.withArguments("show")).build();
        assertTrue(Arrays.stream(result.getOutput().split("\n")).anyMatch(it -> it.startsWith("module-info.class")));
    }

    @Test
    void specifyMultipleDescriptorsToModuleTest() throws IOException {
        final BuildResult result = build.append("import java.util.jar.JarFile",
                "modularize {",
                "   module 'sampleModule', '" + SampleTargetJars.UNNAMED.id + "'",
                "   module 'sampleModule', '" + SampleTargetJars.AUTONAMED.id + "'",
                "}",
                "task show {",
                "   dependsOn tasks.modularize",
                "   doLast {",
                "       def files = project.configurations.getByName('sampleModule').artifacts",
                "       files.each { file ->",
                "           println file",
                "       }",
                "   }",
                "}"
        ).runner(r -> r.withArguments("show")).build();
        assertTrue(Arrays.stream(result.getOutput().split("\n")).anyMatch(it -> it.contains("test-target-sample:jar:jar:autonamed")));
        assertTrue(Arrays.stream(result.getOutput().split("\n")).anyMatch(it -> it.contains("test-target-sample:jar:jar:unnamed")));
    }
}
