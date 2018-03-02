package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompileModuleInfoJavaTask extends DefaultTask {

    private RegularFileProperty infoFile = newInputFile();
    private RegularFileProperty targetJar = newInputFile();
    private DirectoryProperty outputDir = newOutputDirectory();

    @TaskAction
    public void compile() throws IOException {
        final File infoAsFile = infoFile.get().getAsFile();
        final ToolProviderSupport.Result result = compile(extractModuleName(infoAsFile), infoAsFile, targetJar.get().getAsFile(), outputDir.get().getAsFile());
        if (result.exitCode != 0) {
            throw new TaskExecutionException(this, new IllegalStateException("exit code is not 0: " + result.err));
        }
    }

    public static ToolProviderSupport.Result compile(String moduleName, File infoFile, File targetJar, File outputDir) {
        return ToolProviderSupport.run("javac", infoFile.getAbsolutePath(),
                "--patch-module", String.format("%s=%s", moduleName, targetJar.getAbsolutePath()),
                "-d", outputDir.getAbsolutePath());
    }

    public static String extractModuleName(File file) throws IOException {
        final List<String> tokens = Files.lines(file.toPath())
                .map(s -> s.replace("{", " { "))
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .filter(s -> !s.equals("")).collect(Collectors.toList());
        // [java - takeWhile() working differently with flatmap - Stack Overflow]( https://stackoverflow.com/questions/47888814/takewhile-working-differently-with-flatmap )
        final String moduleName = tokens.stream()
                .dropWhile(s -> !s.equals("module")).takeWhile(s -> !s.equals("{"))
                .reduce((l, r) -> r)
                .orElseThrow(() -> new IllegalStateException("could not get the module name from " + file));
        return moduleName;
    }

    @InputFile
    public RegularFileProperty getInfoFile() {
        return infoFile;
    }

    @InputFile
    public RegularFileProperty getTargetJar() {
        return targetJar;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }
}
