package com.github.am4dr.gradle.java_modularize;

import org.gradle.api.artifacts.Configuration;

public interface ModuleDescriptor {

    class StringModuleDescriptor implements ModuleDescriptor {

        final String mavenCoordinate;

        StringModuleDescriptor(String mavenCoordinate) {
            this.mavenCoordinate = mavenCoordinate;
        }
    }

    class ConfigurationModuleDescriptor implements ModuleDescriptor {

        final Configuration configuration;

        ConfigurationModuleDescriptor(Configuration configuration) {
            this.configuration = configuration;
        }
    }
}
