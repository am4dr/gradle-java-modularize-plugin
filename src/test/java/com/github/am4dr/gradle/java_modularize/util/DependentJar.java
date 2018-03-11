package com.github.am4dr.gradle.java_modularize.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public enum DependentJar {

    DEPENDENT("dependent");

    public String id;
    public File file;
    private static final String version = "1.1";

    DependentJar(String name) {
        this.id = "com.github.am4dr.gradle.java-modularize-plugin:test-dependent-target-sample:"+version;
        final URL resource = DependentJar.class.getClassLoader().getResource(String.format("test-dependent-target-sample-%s.jar", version));
        try {
            this.file = new File(Objects.requireNonNull(resource).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
