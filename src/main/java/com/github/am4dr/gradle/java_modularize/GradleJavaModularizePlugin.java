package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.*;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class GradleJavaModularizePlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "com.github.am4dr.java-modularize";
    private static final String extensionName = "modularize";
    private static final Function<String, String> resolverConfigNamer = name -> "resolve" + capitalize(name);

    @Override
    public void apply(Project project) {
        final JavaModularizeExtension modularize = project.getExtensions().create(extensionName, JavaModularizeExtension.class, project);
        modularize.modules.whenObjectAdded(module -> {
            project.getConfigurations().maybeCreate(resolverConfigNamer.apply(module.name)).setVisible(false);
            project.getConfigurations().maybeCreate(module.name);
        });
        project.getTasks().maybeCreate("modularize");
        project.afterEvaluate(GradleJavaModularizePlugin::afterEval);
    }

    private static void afterEval(Project project) {
        final Task modularizeUmbrellaTask = project.getTasks().maybeCreate("modularize");
        final JavaModularizeExtension modularizeExtension = project.getExtensions().getByType(JavaModularizeExtension.class);
        modularizeExtension.modules.withType(JavaModularizeExtension.ModuleSpec.class, module -> {
            final Task modularizeTask = project.getTasks().maybeCreate("modularize" + capitalize(module.name));
            modularizeUmbrellaTask.dependsOn(modularizeTask);
            final String resolverConfigName = resolverConfigNamer.apply(module.name);
            final Configuration resolverConfig = project.getConfigurations().getByName(resolverConfigName);
            module.descriptors.stream()
                    .filter(it -> it != null && !it.equals(""))
                    .forEach(desc -> project.getDependencies().add(resolverConfigName, desc));
            final ResolvedConfiguration resolvedConfiguration = resolverConfig.getResolvedConfiguration();
            final List<ResolvedDependency> deps = new ArrayList<>(resolvedConfiguration.getFirstLevelModuleDependencies());
            final Consumer<ResolvedDependency> modularizeArtifacts = dep -> dep.getModuleArtifacts().stream()
                    .filter(ar -> ar.getType().equals("jar"))
                    .forEach(ar -> {
                        final boolean containsModuleInfo = containsModuleInfo(ar.getFile());
                        if (containsModuleInfo) {
                            project.getArtifacts().add(module.name, ar.getFile(), copyArtifactCoordinatesFrom(ar));
                            return;
                        }

                        final ConfigurableFileCollection files = project.files(getAllDependencyJarFiles(dep));
                        List<String> moduleId = createModuleId(dep, ar);
                        final GenerateModuleInfoTask generateModuleInfo = getGenerateModuleInfoTask(project, ar, moduleId);
                        generateModuleInfo.getDependencies().setFrom(files);
                        final CompileModuleInfoJavaTask compileModuleInfo = getCompileModuleInfoJavaTask(project, ar, moduleId);
                        compileModuleInfo.getDependencies().setFrom(files);
                        final InjectModuleInfoTask injectModuleInfo = getInjectModuleInfoTask(project, ar, moduleId);
                        compileModuleInfo.getInfoFile().set(generateModuleInfo.getOutputDir().file("module-info.java"));
                        compileModuleInfo.getInputs().files(generateModuleInfo.getOutputs());
                        injectModuleInfo.getInfoFile().set(compileModuleInfo.getModuleInfoClassFile());
                        injectModuleInfo.getInputs().files(compileModuleInfo.getOutputs());
                        modularizeTask.dependsOn(injectModuleInfo);

                        project.getArtifacts().add(module.name, injectModuleInfo.getPatchedJar(), copyArtifactCoordinatesFrom(ar));
                    });
            deps.forEach(modularizeArtifacts);
            if (module.recursive) {
                Set<ResolvedDependency> children = deps.stream().flatMap(it -> it.getChildren().stream()).collect(Collectors.toSet());
                while (!children.isEmpty()) {
                    children.forEach(modularizeArtifacts);
                    children = children.stream().flatMap(it -> it.getChildren().stream()).collect(Collectors.toSet());
                }
            }
        });
    }

    private static Action<ConfigurablePublishArtifact> copyArtifactCoordinatesFrom(ResolvedArtifact ar) {
        return artifact -> {
            artifact.setName(ar.getName());
            artifact.setType("jar");
            artifact.setExtension("jar");
            artifact.setClassifier(ar.getClassifier());
        };
    }

    private static Set<File> getAllDependencyJarFiles(ResolvedDependency dep) {
        return dep.getChildren().stream()
                .flatMap(it -> it.getAllModuleArtifacts().stream())
                .filter(it -> it.getType().equals("jar"))
                .map(ResolvedArtifact::getFile)
                .collect(Collectors.toSet());
    }

    private static boolean containsModuleInfo(File targetJar) {
        try {
            return new JarFile(targetJar).getJarEntry("module-info.class") != null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> createModuleId(ResolvedDependency dep, ResolvedArtifact ar) {
        final ArrayList<String> nameParts = new ArrayList<>(List.of(dep.getModuleGroup(), dep.getModuleName(), dep.getModuleVersion()));
        final String classifier = ar.getClassifier();
        if (classifier != null && !classifier.equals("")) {
            nameParts.add(classifier);
        }
        return nameParts;
    }

    private static InjectModuleInfoTask getInjectModuleInfoTask(Project project, ResolvedArtifact ar, List<String> moduleId) {
        final InjectModuleInfoTask injectModuleInfo = project.getTasks().maybeCreate(taskName("injectModuleInfo", moduleId), InjectModuleInfoTask.class);
        final Provider<Directory> injectedJarOutputDir = project.getLayout().getBuildDirectory().dir(InjectModuleInfoTask.class.getSimpleName() + "/" + moduleId);
        injectModuleInfo.getOutputDir().set(injectedJarOutputDir);
        injectModuleInfo.getTargetJar().set(ar.getFile());
        return injectModuleInfo;
    }

    private static CompileModuleInfoJavaTask getCompileModuleInfoJavaTask(Project project, ResolvedArtifact ar, List<String> moduleId) {
        final CompileModuleInfoJavaTask compileModuleInfo = project.getTasks().maybeCreate(taskName("compileModuleInfo", moduleId), CompileModuleInfoJavaTask.class);
        final Provider<Directory> moduleInfoClassDir = project.getLayout().getBuildDirectory().dir(CompileModuleInfoJavaTask.class.getSimpleName() + "/" + moduleId);
        compileModuleInfo.getOutputDir().set(moduleInfoClassDir);
        compileModuleInfo.getTargetJar().set(ar.getFile());
        return compileModuleInfo;
    }

    private static GenerateModuleInfoTask getGenerateModuleInfoTask(Project project, ResolvedArtifact ar, List<String> moduleId) {
        final GenerateModuleInfoTask generateModuleInfo = project.getTasks().maybeCreate(taskName("generateModuleInfo", moduleId), GenerateModuleInfoTask.class);
        final Provider<Directory> moduleInfoJavaDir = project.getLayout().getBuildDirectory().dir(GenerateModuleInfoTask.class.getSimpleName() + "/" + moduleId);
        generateModuleInfo.getOutputDir().set(moduleInfoJavaDir);
        generateModuleInfo.getTargetJar().set(ar.getFile());
        return generateModuleInfo;
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String taskName(String verb, List<String> str) {
        return str.stream().map(GradleJavaModularizePlugin::capitalize).reduce(verb, (l ,r) -> l + "_" + r);
    }
}
