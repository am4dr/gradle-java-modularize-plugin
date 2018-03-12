package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.util.HashSet;
import java.util.Set;

public class JavaModularizeExtension {

    final NamedDomainObjectContainer<ModuleSpec> modules;

    public JavaModularizeExtension(Project project) {
        modules = project.container(ModuleSpec.class);
    }

    public void modules(Action<NamedDomainObjectContainer<ModuleSpec>> action) {
        action.execute(modules);
    }

    public void module(String name, String descriptor) {
        modules.maybeCreate(name).descriptors.add(descriptor);
    }

    public void module(String name, String descriptor, boolean recursive) {
        final ModuleSpec moduleSpec = modules.maybeCreate(name);
        moduleSpec.descriptors.add(descriptor);
        moduleSpec.recursive = recursive;
    }

    public void module(String name, Action<ModuleSpec> config) {
        config.execute(modules.maybeCreate(name));
    }

    public static class ModuleSpec {

        String name;
        Set<String> descriptors = new HashSet<>();
        boolean recursive = false;

        public ModuleSpec(String name) {
            this.name = name;
        }
    }
}
