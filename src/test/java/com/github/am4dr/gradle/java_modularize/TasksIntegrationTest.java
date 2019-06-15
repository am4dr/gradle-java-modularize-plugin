package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.testing.target.DependentJars;
import com.github.am4dr.gradle.java_modularize.testing.target.StandaloneJars;
import com.github.am4dr.gradle.java_modularize.util.GradleBuildSupport;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksIntegrationTest {

    private GradleBuildSupport build;
    private GradleRunner runner;

    @BeforeAll
    static void createTempBuildRootDir() throws IOException {
        GradleBuildSupport.setupBuildParentDirectory();
    }

    @BeforeEach
    void setupBuildDir() throws IOException {
        build = new GradleBuildSupport(this.getClass(), true);
        build.createFile(Path.of("settings.gradle"));
        build.append(
                "plugins {",
                "   id '" + GradleJavaModularizePlugin.PLUGIN_ID + "'",
                "}",
                "repositories { mavenLocal() }"
        );
        runner = build.runner;
    }

    @AfterEach
    void afterEach() throws IOException {
        build.clean();
    }

    @Test
    void tasksIntegrationTest() throws IOException {
        final BuildResult result = build.append("import java.util.jar.JarFile",
                "modularize {",
                "   module 'sampleModule', '" + StandaloneJars.UNNAMED.id + "'",
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
        final BuildResult result = build.append("",
                "modularize {",
                "   module 'sampleModule', '" + StandaloneJars.UNNAMED.id + "'",
                "   module 'sampleModule', '" + StandaloneJars.AUTONAMED.id + "'",
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

    @Test
    void targetContainsModuleInfoClassTest() throws IOException {
        final BuildResult result = build.append("import java.util.jar.JarFile",
                "modularize {",
                "   module 'sampleModule', '" + StandaloneJars.NAMED.id + "'",
                "}",
                "task show {",
                "   dependsOn tasks.modularize",
                "   doLast {",
                "       def arts = project.configurations.getByName('sampleModule').artifacts",
                "       arts.each { art ->",
                "           println art",
                "           JarFile jarfile = new JarFile(art.file)",
                "           assert jarfile.stream().anyMatch { it.name == 'module-info.class' }",
                "       }",
                "   }",
                "}"
        ).runner(r -> r.withArguments("show")).build();
        assertTrue(Arrays.stream(result.getOutput().split("\n")).anyMatch(it -> it.contains("test-target-sample:jar:jar:named")));
    }

    @Test
    void targetHasDependenciesTest() throws IOException {
        final BuildResult result = build.append("import java.util.jar.JarFile",
                "modularize {",
                "   module 'sampleModule', '" + DependentJars.DEPENDENT.id + "'",
                "}",
                "task show {",
                "   dependsOn tasks.modularize",
                "   doLast {",
                "       def arts = project.configurations.getByName('sampleModule').artifacts",
                "       arts.each { art ->",
                "           println art",
                "           JarFile jarfile = new JarFile(art.file)",
                "           assert jarfile.stream().anyMatch { it.name == 'module-info.class' }",
                "       }",
                "   }",
                "}"
        ).runner(r -> r.withArguments("show")).build();
        assertTrue(Arrays.stream(result.getOutput().split("\n")).anyMatch(it -> it.contains("test-dependent-target-sample:jar:jar:null")), result.getOutput());
    }

    @Test
    void recursiveDependencyResolutionTest() throws IOException {
        final BuildResult result = build.append("import java.util.jar.JarFile",
                "modularize {",
                "   module ('sampleModule') {",
                "       descriptors += descriptor('" + DependentJars.DEPENDENT.id + "')",
                "       recursive = true",
                "   }",
                "}",
                "task show {",
                "   dependsOn tasks.modularize",
                "   doLast {",
                "       def arts = project.configurations.getByName('sampleModule').artifacts",
                "       arts.each { art ->",
                "           println art",
                "           JarFile jarfile = new JarFile(art.file)",
                "           assert jarfile.stream().anyMatch { it.name == 'module-info.class' }",
                "       }",
                "       assert arts.any { it.name == 'test-dependent-target-sample' }",
                "       assert arts.any { it.name == 'test-target-sample' && it.classifier == 'unnamed' }",
                "   }",
                "}"
        ).runner(r -> r.withArguments("show")).build();
    }

    @Test
    void configurationDescriptorTest() throws IOException {
        final BuildResult result = build.append("import java.util.jar.JarFile",
                "configurations {",
                "   target",
                "}",
                "dependencies {",
                "   target '" + DependentJars.DEPENDENT.id + "'",
                "}",
                "modularize {",
                "   module('sampleModule', configurations.target, false)",
                "}",
                "task show {",
                "   dependsOn tasks.modularize",
                "   doLast {",
                "       def arts = project.configurations.getByName('sampleModule').artifacts",
                "       arts.each { art ->",
                "           println art",
                "           JarFile jarfile = new JarFile(art.file)",
                "           assert jarfile.stream().anyMatch { it.name == 'module-info.class' }",
                "       }",
                "       assert arts.any { it.name == 'test-dependent-target-sample' }",
                "       assert !arts.any { it.name == 'test-target-sample' && it.classifier == 'unnamed' }",
                "   }",
                "}"
        ).runner(r -> r.withArguments("show")).build();
    }
}
