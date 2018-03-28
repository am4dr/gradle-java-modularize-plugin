package com.github.am4dr.gradle.java_modularize;

import com.github.am4dr.gradle.java_modularize.tooling.ToolProviderSupport;
import com.github.am4dr.gradle.java_modularize.tooling.Tooling;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;

public class InjectModuleInfoTask extends DefaultTask {

    private RegularFileProperty targetJar = newInputFile();
    private RegularFileProperty infoFile = newInputFile();
    private DirectoryProperty outputDir = newOutputDirectory();

    @TaskAction
    void run() throws IOException {
        final File tempDir = getTemporaryDir();
        final ToolProviderSupport.Result result = Tooling.injectModuleInfo(targetJar.get().getAsFile(), infoFile.getAsFile().get(), tempDir);
        if (result.exitCode != 0) {
            throw new TaskExecutionException(this, new IllegalStateException("exit code is not 0: " + result.err));
        }
        getProject().copy(c -> c.from(getProject().fileTree(tempDir)).into(outputDir.getAsFile()));
    }

    @InputFile
    public RegularFileProperty getTargetJar() {
        return targetJar;
    }

    @InputFiles
    public RegularFileProperty getInfoFile() {
        return infoFile;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    @OutputFile
    public Provider<RegularFile> getPatchedJar() {
        return outputDir.file(targetJar.map(RegularFile::getAsFile).map(File::getName));
    }
}
