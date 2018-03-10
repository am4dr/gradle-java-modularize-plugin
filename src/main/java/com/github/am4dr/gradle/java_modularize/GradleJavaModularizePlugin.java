package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

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
        final JavaModularizeExtension modularize = project.getExtensions().getByType(JavaModularizeExtension.class);
        modularize.modules.withType(JavaModularizeExtension.ModuleSpec.class, module -> {
            final Task modularizeTask = project.getTasks().maybeCreate("modularize" + capitalize(module.name));
            final String resolverConfigName = resolverConfigNamer.apply(module.name);
            final Configuration resolverConfig = project.getConfigurations().getByName(resolverConfigName);
            module.descriptors.stream()
                    .filter(it -> it != null && !it.equals(""))
                    .forEach(desc -> project.getDependencies().add(resolverConfigName, desc));
            final Set<ResolvedDependency> deps = resolverConfig.getResolvedConfiguration().getFirstLevelModuleDependencies();
            // TODO process children of the dependencies recursively
            // TODO check the jar does not contain module-info.class
            deps.forEach(dep -> {
                dep.getModuleArtifacts().stream()
                        .filter(ar -> ar.getType().equals("jar"))
                        .forEach(ar -> {
                            String moduleId = createModuleId(dep, ar);
                            final GenerateModuleInfoTask generateModuleInfo = getGenerateModuleInfoTask(project, ar, moduleId);
                            final CompileModuleInfoJavaTask compileModuleInfo = getCompileModuleInfoJavaTask(project, ar, moduleId);
                            final PatchToJarTask patchJar = getPatchToJarTask(project, ar, moduleId);
                            compileModuleInfo.getInfoFile().set(generateModuleInfo.getOutputDir().file("module-info.java"));
                            compileModuleInfo.getInputs().files(generateModuleInfo.getOutputs());
                            patchJar.getInfoFile().set(compileModuleInfo.getModuleInfoClassFile());
                            patchJar.getInputs().files(compileModuleInfo.getOutputs());

                            project.getArtifacts().add(module.name, patchJar.getPatchedJar(), artifact -> {
                                artifact.setName(ar.getName());
                                artifact.setType("jar");
                                artifact.setExtension("jar");
                                artifact.setClassifier(ar.getClassifier());
                            });
                            modularizeTask.dependsOn(patchJar);
                            modularizeUmbrellaTask.dependsOn(modularizeTask);
                        });
            });
        });
    }

    private static String createModuleId(ResolvedDependency dep, ResolvedArtifact ar) {
        final ArrayList<String> nameParts = new ArrayList<>(List.of(dep.getModuleGroup(), dep.getModuleName(), dep.getModuleVersion()));
        final String classifier = ar.getClassifier();
        if (classifier != null && !classifier.equals("")) {
            nameParts.add(classifier);
        }
        return String.join("_", nameParts);
    }

    private static PatchToJarTask getPatchToJarTask(Project project, ResolvedArtifact ar, String moduleId) {
        final PatchToJarTask patchJar = project.getTasks().maybeCreate(taskName("patchJar", moduleId), PatchToJarTask.class);
        final Provider<Directory> patchedJarOutputDir = project.getLayout().getBuildDirectory().dir(PatchToJarTask.class.getSimpleName() + "/" + moduleId);
        patchJar.getOutputDir().set(patchedJarOutputDir);
        patchJar.getTargetJar().set(ar.getFile());
        return patchJar;
    }

    private static CompileModuleInfoJavaTask getCompileModuleInfoJavaTask(Project project, ResolvedArtifact ar, String moduleId) {
        final CompileModuleInfoJavaTask compileModuleInfo = project.getTasks().maybeCreate(taskName("compileModuleInfo", moduleId), CompileModuleInfoJavaTask.class);
        final Provider<Directory> moduleInfoClassDir = project.getLayout().getBuildDirectory().dir(CompileModuleInfoJavaTask.class.getSimpleName() + "/" + moduleId);
        compileModuleInfo.getOutputDir().set(moduleInfoClassDir);
        compileModuleInfo.getTargetJar().set(ar.getFile());
        return compileModuleInfo;
    }

    private static GenerateModuleInfoTask getGenerateModuleInfoTask(Project project, ResolvedArtifact ar, String moduleId) {
        final GenerateModuleInfoTask generateModuleInfo = project.getTasks().maybeCreate(taskName("generateModuleInfo", moduleId), GenerateModuleInfoTask.class);
        final Provider<Directory> moduleInfoJavaDir = project.getLayout().getBuildDirectory().dir(GenerateModuleInfoTask.class.getSimpleName() + "/" + moduleId);
        generateModuleInfo.getOutputDir().set(moduleInfoJavaDir);
        generateModuleInfo.getTargetJar().set(ar.getFile());
        return generateModuleInfo;
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String taskName(String verb, String str) {
        return verb + capitalize(str);
    }
}
