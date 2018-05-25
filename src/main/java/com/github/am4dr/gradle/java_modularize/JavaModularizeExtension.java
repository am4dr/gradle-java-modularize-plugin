package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

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
        module(name, descriptor, ModuleSpec.DEFAULT_RECURSIVE);
    }

    public void module(String name, String descriptor, boolean recursive) {
        final ModuleSpec moduleSpec = modules.maybeCreate(name);
        moduleSpec.descriptors.add(ModuleSpec.descriptor(descriptor));
        moduleSpec.recursive = recursive;
    }

    public void module(String name, Configuration configuration) {
        module(name, configuration, ModuleSpec.DEFAULT_RECURSIVE);
    }
    public void module(String name, Configuration configuration, boolean recursive) {
        final ModuleSpec moduleSpec = modules.maybeCreate(name);
        moduleSpec.descriptors.add(ModuleSpec.descriptor(configuration));
        moduleSpec.recursive = recursive;
    }

    public void module(String name, Dependency dependency) {

    }
    public void module(String name, Dependency dependency, boolean recursive) {
        final ModuleSpec moduleSpec = modules.maybeCreate(name);
        moduleSpec.descriptors.add(ModuleSpec.descriptor(dependency));
        moduleSpec.recursive = recursive;
    }

    public void module(String name, Action<ModuleSpec> config) {
        config.execute(modules.maybeCreate(name));
    }

    public static class ModuleSpec {

        static boolean DEFAULT_RECURSIVE = false;

        String name;
        Set<ModuleDescriptor> descriptors = new HashSet<>();
        boolean recursive = DEFAULT_RECURSIVE;

        public ModuleSpec(String name) {
            this.name = name;
        }

        public static ModuleDescriptor descriptor(String mavenCoordinates) {
            return new ModuleDescriptor.StringModuleDescriptor(mavenCoordinates);
        }
        public static ModuleDescriptor descriptor(Configuration configuration) {
            return new ModuleDescriptor.ConfigurationModuleDescriptor(configuration);
        }
        public static ModuleDescriptor descriptor(Dependency dependency) {
            return new ModuleDescriptor.DependencyModuleDescriptor(dependency);
        }
    }
}
