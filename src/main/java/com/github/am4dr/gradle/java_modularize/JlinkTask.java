package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JlinkTask extends DefaultTask {

    private final ObjectFactory objects = getProject().getObjects();
    private ListProperty<String> args = objects.listProperty(String.class);
    private ConfigurableFileCollection modulePaths = getProject().files();
    private ListProperty<String> modules = objects.listProperty(String.class);
    private Property<String> launchScriptName = objects.property(String.class);
    private Property<String> launcherClass = objects.property(String.class);
    private Property<Integer> compress = objects.property(Integer.class);
    private Property<String> outputDirName = objects.property(String.class);
    private DirectoryProperty outputParentDir = newOutputDirectory();

    public JlinkTask() {
        args.set(List.of());
        outputParentDir.set(getProject().getLayout().getBuildDirectory().dir("jlink"));
        outputDirName.set(launchScriptName);
        compress.set(0);
    }

    @TaskAction
    public void jlink() throws IOException {
        final File tempDir = new File(getTemporaryDir(), outputDirName.get());
        if (args.get().isEmpty()) {
            args.set(createArgs(modulePaths.getFiles(), modules.get(), launchScriptName.get(), launcherClass.get(), compress.get(), tempDir));
        }
        final ToolProviderSupport.Result result = jlink(args.get());
        if (result.exitCode != 0) {
            throw new TaskExecutionException(this, new IllegalStateException("exit code is not 0: " + result.out + "\n\n" + result.err));
        }
        Files.move(tempDir.toPath(), getOutputDir().get().getAsFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static ToolProviderSupport.Result jlink(List<String> args) {
        return ToolProviderSupport.run("jlink", args);
    }

    private static ArrayList<String> createArgs(Set<File> modulePaths, List<String> modules, String name, String launcherClassName, int compress, File outputDir) {
        final ArrayList<String> args = new ArrayList<>(List.of(
                "--module-path", modulePaths.stream().map(File::toString).collect(Collectors.joining(File.pathSeparator)),
                String.format("--compress=%d", compress),
                "--launcher", String.format("%s=%s", name, launcherClassName),
                "--output", outputDir.toString()));
        modules.forEach(m -> args.addAll(List.of("--add-modules", m)));
        return args;
    }

    @Input
    public ListProperty<String> getArgs() {
        return args;
    }

    @InputFiles
    public ConfigurableFileCollection getModulePaths() {
        return modulePaths;
    }

    @Input
    public ListProperty<String> getModules() {
        return modules;
    }

    @Input
    public Property<String> getLaunchScriptName() {
        return launchScriptName;
    }

    @Input
    public Property<String> getLauncherClass() {
        return launcherClass;
    }

    @Input
    public Property<Integer> getCompress() {
        return compress;
    }

    @Input
    public Property<String> getOutputDirName() {
        return outputDirName;
    }

    @OutputDirectory
    public DirectoryProperty getOutputParentDir() {
        return outputParentDir;
    }

    @OutputDirectory
    public Provider<Directory> getOutputDir() {
        return outputParentDir.dir(outputDirName);
    }

}
