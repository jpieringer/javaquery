package one.pieringer.javaquery.gradleplugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaqueryTask extends DefaultTask {

    @Nonnull
    private FileCollection sourceDirs;
    @Nonnull
    private FileCollection auxClassPaths;
    @Nonnull
    private final Property<String> databaseUri;
    @Nonnull
    private final JavaqueryExecutor javaqueryExecutor;

    @Inject
    public JavaqueryTask(@Nonnull final ObjectFactory objectFactory) {
        Objects.requireNonNull(objectFactory);
        this.javaqueryExecutor = new JavaqueryExecutor();

        sourceDirs = objectFactory.fileCollection();
        auxClassPaths = objectFactory.fileCollection();
        databaseUri = objectFactory.property(String.class);
    }

    public void init(@Nonnull final JavaqueryExtension extension) {
        Objects.requireNonNull(extension);

        this.databaseUri.convention(extension.getDatabaseUri());
    }

    /**
     * @return The source directories that are parsed.
     */
    @Classpath
    @InputFiles
    @Nonnull
    public FileCollection getSourceDirs() {
        return sourceDirs;
    }

    /**
     * Sets the source directories that should be parsed.
     */
    public void setSourceDirs(@Nonnull final FileCollection sourceDirs) {
        this.sourceDirs = Objects.requireNonNull(sourceDirs);
    }

    /**
     * @return The class path that is used for resolving the types in the parsed source.
     */
    @Classpath
    @InputFiles
    @Nonnull
    public FileCollection getAuxClassPaths() {
        return auxClassPaths;
    }

    /**
     * Sets the class path that is used for resolving the types in the parsed source.
     */
    public void setAuxClassPaths(@Nonnull final FileCollection auxClassPaths) {
        this.auxClassPaths = Objects.requireNonNull(auxClassPaths);
    }

    /**
     * Call the javaquery analyzer to parse the AST and store it in the specified database.
     *
     * @throws IOException if writing temporary files failed.
     */
    @TaskAction
    public void parse() throws IOException {
        javaqueryExecutor.execute(createArguments());
    }

    @Nonnull
    private List<String> createArguments() {
        if (!databaseUri.isPresent()) {
            throw new IllegalArgumentException("Calling the javaquery analyzer failed because database URI is not set.");
        }
        final List<String> arguments = new ArrayList<>();
        arguments.add("-analyze");
        arguments.add(StreamSupport.stream(sourceDirs.spliterator(), false).map(File::getAbsolutePath).collect(Collectors.joining(";")));
        arguments.add("-dependencies");
        arguments.add(StreamSupport.stream(auxClassPaths.spliterator(), false).map(File::getAbsolutePath).collect(Collectors.joining(";")));
        arguments.add("-databaseUri");
        arguments.add(databaseUri.get());
        return arguments;
    }
}
