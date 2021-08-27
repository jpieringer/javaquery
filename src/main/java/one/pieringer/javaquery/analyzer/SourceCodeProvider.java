package one.pieringer.javaquery.analyzer;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.common.collect.Streams;

public class SourceCodeProvider {

    private static final Logger LOG = LogManager.getLogger(SourceCodeProvider.class);

    @Nonnull
    private final List<File> sourceFolders;
    @Nonnull
    private final List<File> dependencySourceDirectories;
    @Nonnull
    private final List<File> dependencyJarFiles;

    public SourceCodeProvider(@Nonnull final List<File> sourceFolders,
                              @Nonnull final List<File> dependencySourceDirectories, @Nonnull final List<File> dependencyJarFiles) {
        this.sourceFolders = Objects.requireNonNull(sourceFolders);
        this.dependencySourceDirectories = Objects.requireNonNull(dependencySourceDirectories);
        this.dependencyJarFiles = Objects.requireNonNull(dependencyJarFiles);
    }

    @Nonnull
    public List<File> getSourceFolders() {
        return sourceFolders;
    }

    @Nonnull
    public List<File> getDependencySourceDirectories() {
        return dependencySourceDirectories;
    }

    @Nonnull
    public List<File> getDependencyJarFiles() {
        return dependencyJarFiles;
    }

    public Stream<JavaFile> getJavaFileStream() {
        return sourceFolders.stream()
                .map(sourceFolder -> Streams.stream(FileUtils.iterateFiles(sourceFolder, new String[]{"java"}, true))
                        .map(javaFile -> new JavaFile(sourceFolder, javaFile)))
                .reduce(Streams::concat)
                .orElseGet(Stream::empty);
    }

    public static record JavaFile(@Nonnull File sourceFolder, @Nonnull File javaFile) {
        public JavaFile(@Nonnull File sourceFolder, @Nonnull File javaFile) {
            this.sourceFolder = Objects.requireNonNull(sourceFolder);
            this.javaFile = Objects.requireNonNull(javaFile);
        }
    }
}
