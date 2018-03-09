package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class GenerateModuleInfoTask extends DefaultTask {

    private RegularFileProperty targetJar = newInputFile();
    private Property<Boolean> isOpenModule = getProject().getObjects().property(Boolean.class);
    private ConfigurableFileCollection dependencies = getProject().files();
    private DirectoryProperty outputDir = newOutputDirectory();

    public GenerateModuleInfoTask() {
        isOpenModule.set(false);
    }

    @TaskAction
    public void run() throws IOException {
        final File tempDir = getTemporaryDir();
        final ToolProviderSupport.Result result = generateModuleInfoJava(isOpenModule.getOrElse(false), targetJar.get().getAsFile(), tempDir, dependencies.getFiles());
        if (result.exitCode != 0) {
            throw new TaskExecutionException(this, new IllegalStateException("exit code is not 0: " + result.err));
        }
        final Path infoFile = Files.find(tempDir.toPath(), Integer.MAX_VALUE, (path, attr) -> path.getFileName().toString().equals("module-info.java"))
                .findAny().orElseThrow(() -> new TaskExecutionException(this, new IllegalStateException("module-info.java has not generated")));
        Files.copy(infoFile, getModuleInfoJavaFile().get().getAsFile().toPath());
    }

    // TODO handle dependency module paths
    public static ToolProviderSupport.Result generateModuleInfoJava(boolean isOpenModule, File targetJar, File outputDir, Set<File> dependencies) {
        final String moduleTypeOption = isOpenModule ? "--generate-open-module" : "--generate-module-info";
        return ToolProviderSupport.run("jdeps", moduleTypeOption, outputDir.getAbsolutePath(), targetJar.getAbsolutePath());
    }

    @InputFile
    public RegularFileProperty getTargetJar() {
        return targetJar;
    }

    @Input
    public Property<Boolean> getIsOpenModule() {
        return isOpenModule;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencies() {
        return dependencies;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    @OutputFile
    public Provider<RegularFile> getModuleInfoJavaFile() {
        return outputDir.file("module-info.java");
    }
}
