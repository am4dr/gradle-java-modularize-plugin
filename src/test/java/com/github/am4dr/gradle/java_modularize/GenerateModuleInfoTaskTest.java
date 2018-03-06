package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.util.GradleBuildSupport;
import com.github.am4dr.gradle.java_modularize.util.SampleTargetJars;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateModuleInfoTaskTest {

    private GradleBuildSupport build;

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
    }

    @Test
    void taskTypeTest() throws IOException {
        build.append(
                "import " + GenerateModuleInfoTask.class.getName(),
                "task generate(type: " + GenerateModuleInfoTask.class.getSimpleName() + ") {",
                "}"
        ).build();
    }

    @Test
    void taskRunWithEmptyInputsShouldFail() throws IOException {
        build.append(
                "import " + GenerateModuleInfoTask.class.getName(),
                "task generate(type: " + GenerateModuleInfoTask.class.getSimpleName() + ") {",
                "}"
        ).runner(r -> r.withArguments("generate"));
        assertThrows(UnexpectedBuildFailure.class, build::build);
    }

    @Test
    void taskRunWithInputsTest() throws IOException {
        final String targetJar = SampleTargetJars.UNNAMED.file.toString().replaceAll("\\\\", "/");
        final String outputDir = build.tempBuildDir.resolve("output").toString().replaceAll("\\\\", "/");
        build.append(
                "import " + GenerateModuleInfoTask.class.getName(),
                "task generate(type: " + GenerateModuleInfoTask.class.getSimpleName() + ") {",
                "   targetJar = project.file('" + targetJar + "')",
                "   outputDir = project.file('" + outputDir + "')",
                "}"
        ).runner(r -> r.withArguments("generate")).build();

        final Path outputFile = build.tempBuildDir.resolve("output/test.target.sample/module-info.java");
        assertTrue(Files.isRegularFile(outputFile));
    }
}
