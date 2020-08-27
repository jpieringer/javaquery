package one.pieringer.javaquery.analyzer;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SourceCodeProvider {

    private static final Logger LOG = LogManager.getLogger(SourceCodeProvider.class);

    @Nonnull
    private final List<File> sourceFolders;

    public SourceCodeProvider(@Nonnull final File... sourceFolders) {
        this.sourceFolders = Arrays.asList(Objects.requireNonNull(sourceFolders));
    }

    @Nonnull
    public List<File> getSourceFolders() {
        return sourceFolders;
    }

    public void visitJavaFiles(@Nonnull final Consumer<File> javaFileConsumer) {
        int i = 0;
        for (File sourceFolder : sourceFolders) {
            i++;
            Iterator<File> it = FileUtils.iterateFiles(sourceFolder, new String[]{"java"}, true);
            int fileCount = 0;
            while (it.hasNext()) {
                javaFileConsumer.accept(it.next());
                fileCount++;
            }
            LOG.info("({}/{}) Visited (Files: {}) {}", i, sourceFolders.size(), fileCount, sourceFolder);
        }
    }
}
