package com.github.am4dr.gradle.java_modularize.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public enum DependentJar {

    DEPENDENT("dependent");

    public String id;
    public File file;

    DependentJar(String name) {
        this.id = "com.github.am4dr.gradle-java-modularize-plugin:test-target-dependent-sample:1.0";
        final URL resource = DependentJar.class.getClassLoader().getResource(String.format("test-target-dependent-sample-%s.jar", "1.0"));
        try {
            this.file = new File(Objects.requireNonNull(resource).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
