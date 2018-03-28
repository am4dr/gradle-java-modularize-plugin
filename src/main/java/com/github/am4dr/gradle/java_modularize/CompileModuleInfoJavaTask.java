package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.tooling.ToolProviderSupport;
import com.github.am4dr.gradle.java_modularize.tooling.Tooling;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CompileModuleInfoJavaTask extends DefaultTask {

    private RegularFileProperty infoFile = newInputFile();
    private RegularFileProperty targetJar = newInputFile();
    private ConfigurableFileCollection dependencies = getProject().files();
    private DirectoryProperty outputDir = newOutputDirectory();

    @TaskAction
    public void compile() throws IOException {
        final File tempDir = getTemporaryDir();
        final File infoAsFile = infoFile.get().getAsFile();
        final ToolProviderSupport.Result result = Tooling.compileModuleInfoJava(Tooling.extractModuleName(infoAsFile), infoAsFile, targetJar.get().getAsFile(), tempDir, dependencies.getFiles());
        if (result.exitCode != 0) {
            throw new TaskExecutionException(this, new IllegalStateException("exit code is not 0: " + result.err));
        }
        final Path infoFile = Files.find(tempDir.toPath(), Integer.MAX_VALUE, (path, attr) -> path.getFileName().toString().equals("module-info.class"))
                .findAny().orElseThrow(() -> new TaskExecutionException(this, new IllegalStateException("module-info.class has not generated")));
        Files.copy(infoFile, getModuleInfoClassFile().get().getAsFile().toPath());
    }

    @InputFile
    public RegularFileProperty getInfoFile() {
        return infoFile;
    }

    @InputFile
    public RegularFileProperty getTargetJar() {
        return targetJar;
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
    public Provider<RegularFile> getModuleInfoClassFile() {
        return outputDir.file("module-info.class");
    }
}
