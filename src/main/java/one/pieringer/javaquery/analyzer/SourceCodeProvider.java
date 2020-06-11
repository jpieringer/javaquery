package one.pieringer.javaquery.analyzer;

import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SourceCodeProvider {

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
        for (File sourceFolder : sourceFolders) {
            Iterator<File> it = FileUtils.iterateFiles(sourceFolder, new String[]{"java"}, true);
            while (it.hasNext()) {
                javaFileConsumer.accept(it.next());
            }
        }
    }
}
