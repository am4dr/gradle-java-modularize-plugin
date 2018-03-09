package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PatchToJarTask extends DefaultTask {

    private RegularFileProperty targetJar = newInputFile();
    private RegularFileProperty infoFile = newInputFile();
    private DirectoryProperty outputDir = newOutputDirectory();

    @TaskAction
    void run() throws IOException {
        final File tempDir = getTemporaryDir();
        patch(targetJar.get().getAsFile(), infoFile.get().getAsFile(), tempDir, outputDir.get().getAsFile());
    }

    public static ToolProviderSupport.Result patch(File targetJar, File infoFile, File tempDir, File outputDir) throws IOException {
        Files.createDirectories(tempDir.toPath());
        Files.createDirectories(outputDir.toPath());
        final Path copied = Files.copy(targetJar.toPath(), tempDir.toPath().resolve(targetJar.getName()), StandardCopyOption.REPLACE_EXISTING);
        final ToolProviderSupport.Result patchResult = ToolProviderSupport.run("jar", "uf", copied.toRealPath().toString(), infoFile.getAbsolutePath());
        if (patchResult.exitCode == 0) {
            Files.copy(copied, outputDir.toPath().resolve(targetJar.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
        Files.deleteIfExists(copied);
        return patchResult;
    }

    @InputFile
    public RegularFileProperty getTargetJar() {
        return targetJar;
    }

    @InputFiles
    public RegularFileProperty getInfoFile() {
        return infoFile;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    @OutputFile
    public Provider<RegularFile> getPatchedJar() {
        return outputDir.file(targetJar.map(file -> file.getAsFile().getName()));
    }
}
