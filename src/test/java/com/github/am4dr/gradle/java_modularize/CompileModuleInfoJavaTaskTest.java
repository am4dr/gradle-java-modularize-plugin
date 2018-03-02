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
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompileModuleInfoJavaTaskTest {

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
                "import " + CompileModuleInfoJavaTask.class.getName(),
                "task compile(type: " + CompileModuleInfoJavaTask.class.getSimpleName() + ") {",
                "}"
        ).build();
    }

    @Test
    void compileWithoutInputsMustFail() throws IOException {
        build.append(
                "import " + CompileModuleInfoJavaTask.class.getName(),
                "task compile(type: " + CompileModuleInfoJavaTask.class.getSimpleName() + ") {",
                "}"
        ).runner(r -> r.withArguments("compile"));
        assertThrows(UnexpectedBuildFailure.class, build::build);
    }

    @Test
    void compileTest() throws IOException {
        final String targetJar = SampleTargetJars.UNNAMED.file.toString().replace("\\", "/");
        final Path outputDirPath = build.tempBuildDir.resolve("output");
        final String outputDir = outputDirPath.toString().replace("\\", "/");
        final Path infoFilePath = Paths.get("module-info.java");
        build.createFile(infoFilePath).append(infoFilePath, "",
                "module sample.unnamed {",
                "   exports sample;",
                "}");
        build.append(
                "import " + CompileModuleInfoJavaTask.class.getName(),
                "task compile(type: " + CompileModuleInfoJavaTask.class.getSimpleName() + ") {",
                "   infoFile = project.file('" + infoFilePath.toString() + "')",
                "   targetJar = project.file('" + targetJar + "')",
                "   outputDir = project.file('" + outputDir + "')",
                "}"
        ).runner(r -> r.withArguments("compile")).build();
        assertTrue(Files.isRegularFile(outputDirPath.resolve("module-info.class")));
    }
}