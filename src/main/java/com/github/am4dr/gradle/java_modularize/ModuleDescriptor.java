package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.artifacts.Configuration;

// FIXME use interface instead of abstract class
// static methods declared on interfaces can't be called correctly.
// [Default methods on managed model interfaces cannot be called on Java 9 · Issue #721 · gradle/gradle]( https://github.com/gradle/gradle/issues/721 )
public abstract class ModuleDescriptor {

    static ModuleDescriptor of(String mavenCoordinate) {
        return new StringModuleDescriptor(mavenCoordinate);
    }
    static ModuleDescriptor of(Configuration configuration) {
        return new ConfigurationModuleDescriptor(configuration);
    }

    static class StringModuleDescriptor extends ModuleDescriptor {

        final String mavenCoordinate;

        StringModuleDescriptor(String mavenCoordinate) {
            this.mavenCoordinate = mavenCoordinate;
        }
    }

    static class ConfigurationModuleDescriptor extends ModuleDescriptor {

        final Configuration configuration;

        ConfigurationModuleDescriptor(Configuration configuration) {
            this.configuration = configuration;
        }
    }
}
