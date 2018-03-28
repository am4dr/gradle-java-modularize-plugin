package com.github.am4dr.gradle.java_modularize.testing.target;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public enum DependentJars {

    DEPENDENT("dependent");

    public final String id;
    public final File file;
    private static final String version = "2.0";

    DependentJars(String name) {
        this.id = "com.github.am4dr.gradle.java-modularize-plugin:test-dependent-target-sample:"+version;
        final URL resource = DependentJars.class.getClassLoader().getResource(String.format("test-dependent-target-sample-%s.jar", version));
        try {
            this.file = new File(Objects.requireNonNull(resource).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
