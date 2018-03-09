package com.github.am4dr.gradle.java_modularize.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public enum SampleTargetJars {

    UNNAMED("unnamed"), NAMED("named"), AUTONAMED("autonamed");

    public String id;
    public File file;

    SampleTargetJars(String name) {
        this.id = "com.github.am4dr.gradle-java-modularize-plugin:test-target-sample:1.0:"+name;
        final URL resource = SampleTargetJars.class.getClassLoader().getResource(String.format("test-target-sample-%s-%s.jar", "1.0", name));
        try {
            this.file = new File(Objects.requireNonNull(resource).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
