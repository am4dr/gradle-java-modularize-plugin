package com.github.am4dr.gradle.java_modularize.util;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.function.Executable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.APPEND;

public class GradleBuildSupport {

    public static final Path DEFAULT_TEMP_BUILD_ROOT_DIR = Paths.get("build", "tmp", "testGradleBuild").toAbsolutePath();
    public final Path tempBuildDir;
    public final Path tempBuildFile;
    public final GradleRunner runner;

    public GradleBuildSupport() throws IOException {
        this(DEFAULT_TEMP_BUILD_ROOT_DIR, GradleBuildSupport.class, true);
    }

    public GradleBuildSupport(Class<?> klass) throws IOException {
        this(DEFAULT_TEMP_BUILD_ROOT_DIR, klass, true);
    }

    public GradleBuildSupport(Class<?> klass, boolean deleteOnExit) throws IOException {
        this(DEFAULT_TEMP_BUILD_ROOT_DIR, klass, deleteOnExit);
    }

    public GradleBuildSupport(Path tempBuildRootDir, Class<?> klass, boolean deleteOnExit) throws IOException {
        tempBuildDir = Files.createTempDirectory(tempBuildRootDir, klass.getSimpleName());
        tempBuildFile = Files.createFile(tempBuildDir.resolve("build.gradle"));
        if (deleteOnExit) {
            tempBuildDir.toFile().deleteOnExit();
            tempBuildFile.toFile().deleteOnExit();
        }
        runner = GradleRunner.create().withProjectDir(tempBuildDir.toFile()).withPluginClasspath();
    }

    public static void setupBuildParentDirectory() throws IOException {
        Files.createDirectories(DEFAULT_TEMP_BUILD_ROOT_DIR);
    }

    public GradleBuildSupport createFile(Path path) throws IOException {
        final Path target = tempBuildDir.resolve(path);
        Files.createDirectories(target.getParent());
        Files.createFile(target);
        return this;
    }

    private BufferedWriter getUTF8Writer(Path path, OpenOption... opts) throws IOException {
        return Files.newBufferedWriter(path, StandardCharsets.UTF_8, opts);
    }

    public GradleBuildSupport append(Path path, String... body) throws IOException {
        final Path target = tempBuildDir.resolve(path);
        try (BufferedWriter writer = getUTF8Writer(target, APPEND)) {
            writer.write(String.join("\n", body)+"\n");
        }
        return this;
    }

    public GradleBuildSupport append(String... body) throws IOException {
        return append(tempBuildFile, body);
    }

    public BuildResult build() {
        return runner.build();
    }

    public BuildResult build(String... body) throws IOException {
        return append(body).runner.build();
    }

    public Executable execute() {
        return runner::build;
    }

    public GradleBuildSupport runner(Consumer<GradleRunner> consumer) {
        consumer.accept(runner);
        return this;
    }
}
