package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.testing.target.DependentJars;
import com.github.am4dr.gradle.java_modularize.testing.target.StandaloneJars;
import com.github.am4dr.gradle.java_modularize.util.GradleBuildSupport;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaModularizeExtensionTest {

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
                "import " + ModuleDescriptor.class.getName(),
                "modularize {",
                "   modules {",
                "       sampleModule {",
                "           descriptors = [ModuleDescriptor.of('" + StandaloneJars.UNNAMED.id + "')]",
                "       }",
                "   }",
                "}"
        ).build();
    }

    @Test
    void extensionModuleMethodTest() throws IOException {
        build.append("",
                "modularize {",
                "   module 'sampleModule', '" + StandaloneJars.UNNAMED.id + "'",
                "}"
        ).build();
    }

    @Test
    void configurationTest() throws IOException {
        final BuildResult result = build.append("",
                "modularize {",
                "   module 'sampleModule1', '" + StandaloneJars.UNNAMED.id + "'",
                "   module 'sampleModule2', '" + StandaloneJars.UNNAMED.id + "'",
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
    void recursiveFlagTest() throws IOException {
        build.append("",
                "modularize {",
                "   module 'sampleModule1', '" + StandaloneJars.UNNAMED.id + "', false",
                "   module 'sampleModule2', '" + StandaloneJars.UNNAMED.id + "', true",
                "}",
                "task show {",
                "   doLast { project.configurations.forEach(System.out.&println) }",
                "}"
        ).runner(r -> r.withArguments("show", "-q")).build();
    }

    @Test
    void moduleConfigByClosureTest() throws IOException {
        build.append("",
                "import " + ModuleDescriptor.class.getName(),
                "modularize {",
                "   module('sampleModule1') {",
                "       descriptors += ModuleDescriptor.of('" + StandaloneJars.UNNAMED.id + "')",
                "   }",
                "}",
                "task show {",
                "   doLast { project.configurations.forEach(System.out.&println) }",
                "}"
        ).runner(r -> r.withArguments("show", "-q")).build();
    }

    @Test
    void acceptConfigurationAsAModuleDescriptorTest() throws IOException {
        build.append("",
                "import " + ModuleDescriptor.class.getName(),
                "configurations {",
                "   config",
                "}",
                "dependencies {",
                "   config '" + DependentJars.DEPENDENT.id + "'",
                "}",
                "modularize {",
                "   module('sampleModule1', configurations.config)",
                "}",
                "task show {",
                "   doLast { project.configurations.forEach(System.out.&println) }",
                "}"
        ).runner(r -> r.withArguments("show", "-q")).build();
    }
}
