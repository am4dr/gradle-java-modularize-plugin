package com.github.am4dr.gradle.java_modularize.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public enum SampleTargetJars {

    UNNAMED("unnamed"), NAMED("named"), AUTONAMED("autonamed");

    public File file;

    SampleTargetJars(String name) {
        final URL resource = SampleTargetJars.class.getClassLoader().getResource(name + ".jar");
        try {
            this.file = new File(Objects.requireNonNull(resource).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
