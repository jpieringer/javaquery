package one.pieringer.javaquery.gradleplugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaqueryTask extends DefaultTask {

    private static final Logger LOG = LoggerFactory.getLogger(JavaqueryTask.class);

    @CheckForNull
    private Iterable<File> sourceDirs;
    @CheckForNull
    private Iterable<File> auxClassPaths;
    @CheckForNull
    private String databaseUri;

    /**
     * @return The source directories that are parsed.
     */
    @Classpath
    @InputFiles
    @CheckForNull
    public Iterable<File> getSourceDirs() {
        return sourceDirs;
    }

    /**
     * Sets the source directories that should be parsed.
     */
    public void setSourceDirs(@Nonnull final Iterable<File> sourceDirs) {
        this.sourceDirs = Objects.requireNonNull(sourceDirs);
    }

    /**
     * @return The class path that is used for resolving the types in the parsed source.
     */
    @Classpath
    @InputFiles
    @CheckForNull
    public Iterable<File> getAuxClassPaths() {
        return auxClassPaths;
    }

    /**
     * Sets the class path that is used for resolving the types in the parsed source.
     */
    public void setAuxClassPaths(@Nonnull final Iterable<File> auxClassPaths) {
        this.auxClassPaths = Objects.requireNonNull(auxClassPaths);
    }

    /**
     * @return The Neo4J database URI for the database where the parsed AST should be stored.
     */
    @Input
    @CheckForNull
    public String getDatabaseUri() {
        return databaseUri;
    }

    /**
     * Sets the Neo4J database URI for the database where the parsed AST should be stored.
     */
    public void setDatabaseUri(@Nonnull final String databaseUri) {
        this.databaseUri = Objects.requireNonNull(databaseUri);
    }

    /**
     * Call the javaquery analyzer to parse the AST and store it in the specified database.
     *
     * @throws IOException if writing temporary files failed.
     */
    @TaskAction
    public void parse() throws IOException {
        final Path javaqueryCliJar = Files.createTempFile(null, ".jar");
        final Path argumentsFilePath = Files.createTempFile(null, ".args");
        try {
            extractJavaqueryCliJar(javaqueryCliJar);
            writeArgumentsFile(argumentsFilePath);

            final List<String> command = new ArrayList<>();
            command.add(getJavaExecutablePath());
            command.add("-jar");
            command.add(javaqueryCliJar.toString());
            command.add("@" + argumentsFilePath);

            try {
                final Process process = new ProcessBuilder(command).inheritIO().start();
                process.waitFor();
                int exitValue = process.exitValue();
                if (exitValue != 0) {
                    throw new RuntimeException("Javaquery failed with exit code '" + exitValue + "'. Command: '" + String.join(" ", command) + "'");
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Starting the javaquery-cli failed. Command: '" + String.join(" ", command) + "'", e);
            }
        } finally {
            argumentsFilePath.toFile().delete();
            javaqueryCliJar.toFile().delete();
        }
    }

    @Nonnull
    private String getJavaExecutablePath() {
        String javaHome = System.getProperty("java16.home");
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }
        if (javaHome == null) {
            throw new IllegalArgumentException("The java home cannot be found. Checked 'java16.home' and 'java.home'.");
        }

        return javaHome + File.separatorChar + "bin" + File.separatorChar + "java";
    }

    private void extractJavaqueryCliJar(@Nonnull final Path javaqueryFullJar) throws IOException {
        final InputStream javaQueryCliFullResourceStream = JavaqueryTask.class.getResourceAsStream("/javaquery-cli-full.jar");
        if (javaQueryCliFullResourceStream == null) {
            throw new IllegalStateException("Could not find the javaquery-cli-full.jar in the classpath.");
        }

        Files.copy(javaQueryCliFullResourceStream, javaqueryFullJar, StandardCopyOption.REPLACE_EXISTING);
    }

    private void writeArgumentsFile(@Nonnull final Path argumentsFilePath) throws IOException {
        if (sourceDirs == null) {
            throw new IllegalArgumentException("Calling the javaquery analyzer failed because the sourceDirs property is not set.");
        }
        if (auxClassPaths == null) {
            throw new IllegalArgumentException("Calling the javaquery analyzer failed because the sourceDirs property is not set.");
        }
        if (databaseUri == null) {
            throw new IllegalArgumentException("Calling the javaquery analyzer failed because database URI is not set.");
        }
        final List<String> arguments = new ArrayList<>();
        arguments.add("-analyze");
        arguments.add(StreamSupport.stream(sourceDirs.spliterator(), false).map(File::getAbsolutePath).collect(Collectors.joining(";")));
        arguments.add("-dependencies");
        arguments.add(StreamSupport.stream(auxClassPaths.spliterator(), false).map(File::getAbsolutePath).collect(Collectors.joining(";")));
        arguments.add("-databaseUri");
        arguments.add(databaseUri);

        LOG.debug("Writing arguments file with content: " + String.join("\n", arguments));

        Files.write(argumentsFilePath, arguments);
    }
}
