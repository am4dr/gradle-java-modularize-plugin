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
import java.util.stream.Collectors;

public class JlinkTask extends DefaultTask {

    public final Provider<Directory> DEFAULT_OUTPUT_PARENT_DIRECTORY = getProject().getLayout().getBuildDirectory().dir("jlink");
    private final ObjectFactory objects = getProject().getObjects();
    private final ListProperty<String> args = objects.listProperty(String.class);
    private final ListProperty<String> options = objects.listProperty(String.class);
    private final ConfigurableFileCollection modulePaths = getProject().files();
    private final ListProperty<String> modules = objects.listProperty(String.class);
    private final Property<String> launchScriptName = objects.property(String.class);
    private final Property<String> launcherClass = objects.property(String.class);
    private final Property<Integer> compress = objects.property(Integer.class);
    private final Property<String> outputDirName = objects.property(String.class);
    private final DirectoryProperty outputParentDir = newOutputDirectory();
    private final Property<Boolean> useDefaultModules = objects.property(Boolean.class);

    public JlinkTask() {
        args.set(List.of());
        options.set(List.of());
        outputParentDir.set(DEFAULT_OUTPUT_PARENT_DIRECTORY);
        outputDirName.set(launchScriptName);
        compress.set(0);
        useDefaultModules.set(true);
    }

    @TaskAction
    public void jlink() throws IOException {
        final File tempDir = new File(getTemporaryDir(), outputDirName.get());
        final List<File> modulePaths = new ArrayList<>(this.modulePaths.getFiles());
        if (useDefaultModules.get()) {
            modulePaths.add(0, new File(System.getProperty("java.home")+"/jmods"));
        }
        modulePaths.addAll(this.modulePaths.getFiles());
        if (compress.get() != 0) {
            options.add(String.format("--compress=%d", compress.get()));
        }
        if (args.get().isEmpty()) {
            args.set(createArgs(modulePaths, modules.get(), launchScriptName.get(), launcherClass.get(), tempDir, options.get()));
        }
        final ToolProviderSupport.Result result = jlink(args.get());
        if (result.exitCode != 0) {
            throw new TaskExecutionException(this, new IllegalStateException("exit code is not 0: " + result.out + "\n\n" + result.err));
        }
//        getProject().sync(sync -> sync.from(tempDir).into(getOutputDir()));
        Files.move(tempDir.toPath(), getOutputDir().get().getAsFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static ToolProviderSupport.Result jlink(List<String> args) {
        return ToolProviderSupport.run("jlink", args);
    }

    public static ArrayList<String> createArgs(List<File> modulePaths, List<String> modules, String name, String launcherClassName, File outputDir, List<String> options) {
        final ArrayList<String> args = new ArrayList<>(List.of(
                "--module-path", modulePaths.stream().map(File::toString).collect(Collectors.joining(File.pathSeparator)),
                "--launcher", String.format("%s=%s", name, launcherClassName),
                "--output", outputDir.toString()));
        args.addAll(options);
        modules.forEach(m -> args.addAll(List.of("--add-modules", m)));
        return args;
    }

    @Input
    public ListProperty<String> getArgs() {
        return args;
    }

    @Input
    public ListProperty<String> getOptions() {
        return options;
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

    @Input
    public Property<Boolean> getUseDefaultModules() {
        return useDefaultModules;
    }
}
