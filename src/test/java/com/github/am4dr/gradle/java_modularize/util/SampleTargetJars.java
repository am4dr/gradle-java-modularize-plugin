package com.github.am4dr.gradle.java_modularize.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public enum SampleTargetJars {

    UNNAMED("unnamed"), NAMED("named"), AUTONAMED("autonamed");

    public String id;
    public File file;
    private static final String version = "1.1";

    SampleTargetJars(String name) {
        this.id = String.format("com.github.am4dr.gradle.java-modularize-plugin:test-target-sample:%s:%s", version, name);
        final URL resource = SampleTargetJars.class.getClassLoader().getResource(String.format("test-target-sample-%s-%s.jar", version, name));
        try {
            this.file = new File(Objects.requireNonNull(resource).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
