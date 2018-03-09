package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class GradleJavaModularizePlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "com.github.am4dr.java-modularize";
    private static final String extensionName = "modularize";
    private static final Function<String, String> resolveConfigNamer = name -> "resolve" + capitalize(name);

    @Override
    public void apply(Project project) {
        final JavaModularizeExtension modularize = project.getExtensions().create(extensionName, JavaModularizeExtension.class, project);
        modularize.modules.whenObjectAdded(module -> {
            project.getConfigurations().maybeCreate(resolveConfigNamer.apply(module.name)).setVisible(false);
            project.getConfigurations().maybeCreate(module.name);
        });
        project.afterEvaluate(GradleJavaModularizePlugin::afterEval);
    }

    private static void afterEval(Project project) {
        final JavaModularizeExtension modularize = project.getExtensions().getByType(JavaModularizeExtension.class);
        modularize.modules.withType(JavaModularizeExtension.ModuleSpec.class, module -> {
            if (module.descriptor == null || module.descriptor.equals("")) {
                return;
            }
            final Configuration resolveConfig = project.getConfigurations().getByName(resolveConfigNamer.apply(module.name));
            project.getDependencies().add(resolveConfig.getName(), module.descriptor);
            final Set<ResolvedDependency> deps = resolveConfig.getResolvedConfiguration().getFirstLevelModuleDependencies();
            // TODO process children of the dependencies recursively
            // TODO check the jar does not contain module-info.class
            // TODO test output
            deps.stream().forEach(dep -> {
                dep.getModuleArtifacts().stream()
                        .filter(ar -> ar.getExtension().equals("jar"))
                        .forEach(ar -> {
                            final ArrayList<String> nameParts = new ArrayList<>(List.of(dep.getModuleGroup(), dep.getModuleName(), dep.getModuleVersion()));
                            final String classifier = ar.getClassifier();
                            if (classifier != null && !classifier.equals("")) {
                                nameParts.add(classifier);
                            }
                            String moduleId = String.join("_", nameParts);
                            final GenerateModuleInfoTask generateModuleInfo = project.getTasks().maybeCreate(taskName("generateModuleInfo", moduleId), GenerateModuleInfoTask.class);
                            final Provider<Directory> moduleInfoJavaDir = project.getLayout().getBuildDirectory().dir(GenerateModuleInfoTask.class.getSimpleName() + "/" + moduleId);
                            generateModuleInfo.getOutputDir().set(moduleInfoJavaDir);
                            generateModuleInfo.getTargetJar().set(ar.getFile());
                            final CompileModuleInfoJavaTask compileModuleInfo = project.getTasks().maybeCreate(taskName("compileModuleInfo", moduleId), CompileModuleInfoJavaTask.class);
                            final Provider<Directory> moduleInfoClassDir = project.getLayout().getBuildDirectory().dir(CompileModuleInfoJavaTask.class.getSimpleName() + "/" + moduleId);
                            compileModuleInfo.getOutputDir().set(moduleInfoClassDir);
                            compileModuleInfo.getTargetJar().set(ar.getFile());
                            compileModuleInfo.getInfoFile().set(generateModuleInfo.getOutputDir().file("module-info.java"));
                            final PatchToJarTask patchJar = project.getTasks().maybeCreate(taskName("patchJar", moduleId), PatchToJarTask.class);
                            final Provider<Directory> patchedJarOutputDir = project.getLayout().getBuildDirectory().dir(PatchToJarTask.class.getSimpleName() + "/" + moduleId);
                            patchJar.getOutputDir().set(patchedJarOutputDir);
                            patchJar.getTargetJar().set(ar.getFile());
                            patchJar.getInfoFile().set(compileModuleInfo.getOutputDir().file("module-info.class"));
                            project.getArtifacts().add(module.name, patchJar.getPatchedJar());
                        });
            });
        });
        project.getTasks().forEach(System.out::println);
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String taskName(String verb, String str) {
        return verb + capitalize(str);
    }
}
