package com.github.am4dr.gradle.java_modularize.testing.target;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public enum StandaloneJars {

    UNNAMED("unnamed"), NAMED("named"), AUTONAMED("autonamed");

    public final String id;
    public final File file;
    private static final String version = "2.0";

    StandaloneJars(String name) {
        this.id = String.format("com.github.am4dr.gradle.java-modularize-plugin:test-target-sample:%s:%s", version, name);
        final URL resource = StandaloneJars.class.getClassLoader().getResource(String.format("test-target-sample-%s-%s.jar", version, name));
        try {
            this.file = new File(Objects.requireNonNull(resource).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
