package one.pieringer.javaquery.analyzer;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

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

    public void visitJavaFiles(@Nonnull final BiConsumer<File, File> javaFileConsumer) {
        int i = 0;
        for (File sourceFolder : sourceFolders) {
            i++;
            LOG.info("({}/{}) Visiting {}", i, sourceFolders.size(), sourceFolder);
            Iterator<File> it = FileUtils.iterateFiles(sourceFolder, new String[]{"java"}, true);
            int fileCount = 0;
            while (it.hasNext()) {
                javaFileConsumer.accept(sourceFolder, it.next());
                fileCount++;
            }
            LOG.info("({}/{}) Parsed (Files: {})", i, sourceFolders.size(), fileCount);
        }
    }
}
