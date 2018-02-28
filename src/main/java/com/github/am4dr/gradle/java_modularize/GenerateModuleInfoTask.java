package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.util.Set;

public class GenerateModuleInfoTask extends DefaultTask {

    private File targetJar;
    private Property<Boolean> isOpenModule = getProject().getObjects().property(Boolean.class);
    private FileCollection dependencies;
    private DirectoryProperty outputDir = newOutputDirectory();

    public GenerateModuleInfoTask() {
        isOpenModule.set(false);
    }

    @TaskAction
    public void run() {
        final ToolProviderSupport.Result result = generateModuleInfoJava(isOpenModule.getOrElse(false), targetJar, outputDir.get().getAsFile(), dependencies.getFiles());
        if (result.exitCode != 0) {
            throw new TaskExecutionException(this, new IllegalStateException("exit code is not 0"));
        }
    }

    // TODO handle dependency module paths
    public static ToolProviderSupport.Result generateModuleInfoJava(boolean isOpenModule, File targetJar, File outputDir, Set<File> dependencies) {
        final String moduleTypeOption = isOpenModule ? "--generate-open-module" : "--generate-module-info";
        return ToolProviderSupport.run("jdeps", moduleTypeOption, outputDir.getAbsolutePath(), targetJar.getAbsolutePath());
    }

    @Input
    public File getTargetJar() {
        return targetJar;
    }

    public void setTargetJar(File targetJar) {
        this.targetJar = targetJar;
    }

    @Input
    public Property<Boolean> getIsOpenModule() {
        return isOpenModule;
    }

    public void setIsOpenModule(Property<Boolean> isOpenModule) {
        this.isOpenModule = isOpenModule;
    }

    @Input
    public FileCollection getDependencies() {
        return dependencies;
    }

    public void setDependencies(FileCollection dependencies) {
        this.dependencies = dependencies;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(DirectoryProperty outputDir) {
        this.outputDir = outputDir;
    }
}
