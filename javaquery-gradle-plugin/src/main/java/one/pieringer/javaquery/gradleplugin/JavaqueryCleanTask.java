package one.pieringer.javaquery.gradleplugin;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.gradle.api.DefaultTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

public class JavaqueryCleanTask extends DefaultTask {

    @Nonnull
    private final Property<String> databaseUri;
    @Nonnull
    private final JavaqueryExecutor javaqueryExecutor;

    @Inject
    public JavaqueryCleanTask(@Nonnull final ObjectFactory objectFactory) {
        databaseUri = Objects.requireNonNull(objectFactory).property(String.class);
        this.javaqueryExecutor = new JavaqueryExecutor();
    }

    public void init(@Nonnull final JavaqueryExtension extension) {
        Objects.requireNonNull(extension);

        this.databaseUri.convention(extension.getDatabaseUri());
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
        arguments.add("-clean");
        arguments.add("-databaseUri");
        arguments.add(databaseUri.get());
        return arguments;
    }
}
