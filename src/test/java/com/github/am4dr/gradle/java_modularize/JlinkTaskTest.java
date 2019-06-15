package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.testing.target.StandaloneJars;
import com.github.am4dr.gradle.java_modularize.util.GradleBuildSupport;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class JlinkTaskTest {

    private GradleBuildSupport build;

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
                "}"
        );
    }

    @AfterEach
    void afterEach() throws IOException {
        build.clean();
    }

    @Test
    void simpleJlinkTaskTest() throws IOException {
        final BuildResult result = build.append("import " + JlinkTask.class.getName(),
                "import java.nio.file.*",
                "task jlink(type: " + JlinkTask.class.getSimpleName() + ") {",
                "   modulePaths.from(project.files('" + StandaloneJars.NAMED.file.toString().replace("\\", "\\\\") + "'))",
                "   modules.add('sample.named')",
                "   launcherClass = 'sample.named/sample.named.Sample'",
                "   launchScriptName = 'named'",
                "}",
                "task assertion(dependsOn: ':jlink') {",
                "   doFirst {",
                "       assert Files.exists(project.buildDir.toPath().resolve('jlink'))",
                "       assert Files.exists(project.buildDir.toPath().resolve('jlink/named'))",
                "   }",
                "}"
        ).runner(r -> r.withArguments("assertion")).build();
    }
}