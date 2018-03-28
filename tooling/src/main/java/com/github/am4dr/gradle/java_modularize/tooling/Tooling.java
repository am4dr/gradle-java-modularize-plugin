package com.github.am4dr.gradle.java_modularize.tooling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public final class Tooling {

    public static ToolProviderSupport.Result generateModuleInfoJava(boolean isOpenModule, File targetJar, File outputDir, Set<File> dependencies) {
        final String moduleTypeOption = isOpenModule ? "--generate-open-module" : "--generate-module-info";
        final ArrayList<String> args = new ArrayList<>();
        if (!dependencies.isEmpty()) {
            args.addAll(List.of("--module-path", joinModulePaths(dependencies), "--add-modules", "ALL-MODULE-PATH"));
        }
        args.addAll(List.of(moduleTypeOption, outputDir.getAbsolutePath(), targetJar.getAbsolutePath()));
        return ToolProviderSupport.run("jdeps", args);
    }

    public static ToolProviderSupport.Result compileModuleInfoJava(String moduleName, File infoFile, File targetJar, File outputDir, Set<File> dependencies) {
        final ArrayList<String> args = new ArrayList<>();
        if (!dependencies.isEmpty()) {
            args.addAll(List.of("--module-path", joinModulePaths(dependencies)));
        }
        args.addAll(List.of(infoFile.getAbsolutePath(),
                "--patch-module", String.format("%s=%s", moduleName, targetJar.getAbsolutePath()),
                "-d", outputDir.getAbsolutePath()));
        return ToolProviderSupport.run("javac", args);
    }

    private static String joinModulePaths(Collection<File> dependencies) {
        return dependencies.stream().map(File::toString).collect(Collectors.joining(File.pathSeparator));
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

    public static ToolProviderSupport.Result injectModuleInfo(File targetJar, File infoFile, File tempDir, File outputDir) throws IOException {
        Files.createDirectories(tempDir.toPath());
        Files.createDirectories(outputDir.toPath());
        final Path copied = Files.copy(targetJar.toPath(), tempDir.toPath().resolve(targetJar.getName()), StandardCopyOption.REPLACE_EXISTING);
        final ToolProviderSupport.Result patchResult = ToolProviderSupport.run("jar", "uf", copied.toRealPath().toString(),
                "-C", infoFile.getParentFile().getAbsolutePath(), "module-info.class");
        if (patchResult.exitCode == 0) {
            Files.copy(copied, outputDir.toPath().resolve(targetJar.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
        Files.deleteIfExists(copied);
        return patchResult;
    }

    public static ToolProviderSupport.Result jlink(List<String> args) {
        return ToolProviderSupport.run("jlink", args);
    }

    public static ArrayList<String> createArgs(List<File> modulePaths, List<String> modules, String name, String launcherClassName, File outputDir, List<String> options) {
        final ArrayList<String> args = new ArrayList<>(List.of(
                "--module-path", joinModulePaths(modulePaths),
                "--launcher", String.format("%s=%s", name, launcherClassName),
                "--output", outputDir.toString()));
        args.addAll(options);
        modules.forEach(m -> args.addAll(List.of("--add-modules", m)));
        return args;
    }
}
