package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

public class GradleJavaModularizePlugin implements Plugin<Project> {

    public static String PLUGIN_ID = "com.github.am4dr.java-modularize";

    @Override
    public void apply(Project project) {
        final JavaModularizeExtension modularize = project.getExtensions().create("modularize", JavaModularizeExtension.class, project);
        project.afterEvaluate(p -> {
            System.out.println("after eval");
            modularize.modules.withType(JavaModularizeExtension.ModuleSpec.class, module -> {
                if (module.descriptor == null || module.descriptor.equals("")) {
                    return;
                }
                final Configuration config = project.getConfigurations().maybeCreate(module.name);
                project.getDependencies().add(config.getName(), module.descriptor);
            });
        });
    }
}
