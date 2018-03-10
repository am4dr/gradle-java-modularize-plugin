package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.util.HashSet;
import java.util.Set;

// TODO add dependency transitivity flag
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

    public static class ModuleSpec {

        String name;
        Set<String> descriptors;

        public ModuleSpec(String name) {
            this.name = name;
            this.descriptors = new HashSet<>();
        }
    }
}
