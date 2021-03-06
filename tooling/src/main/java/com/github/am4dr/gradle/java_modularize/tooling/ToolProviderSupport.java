package com.github.am4dr.gradle.java_modularize.tooling;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.spi.ToolProvider;

import static java.lang.String.format;

public class ToolProviderSupport {

    public static ToolProvider getToolProvider(String name) {
        final Optional<ToolProvider> tool = ToolProvider.findFirst(name);
        if (!tool.isPresent()) {
            throw new IllegalStateException(format("%s is not found by %s", name, ToolProvider.class.getName()));
        }
        return tool.get();
    }

    public static Result run(ToolProvider tool, String... args) {
        try (final StringWriter swOut = new StringWriter(); final PrintWriter pwOut = new PrintWriter(swOut);
             final StringWriter swErr = new StringWriter(); final PrintWriter pwErr = new PrintWriter(swErr)) {
            return new Result(tool.run(pwOut, pwErr, args), swOut.toString(), swErr.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Result run(String name, String... args) {
        return run(getToolProvider(name), args);
    }
    public static Result run(String name, List<String> args) {
        return run(getToolProvider(name), args.toArray(new String[]{}));
    }

    public static class Result {

        public final int exitCode;
        public final String out;
        public final String err;

        public Result(int exitCode, String out, String err) {
            this.exitCode = exitCode;
            this.out = out;
            this.err = err;
        }

        @Override
        public String toString() {
            return String.format("exit code: %d%n%nOutput: %s%n%nError: %s", exitCode, out, err);
        }
    }
}
