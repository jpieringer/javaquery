package one.pieringer.javaquery.gradleplugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaqueryExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(JavaqueryExecutor.class);

    public void execute(@Nonnull final List<String> arguments) throws IOException {
        final Path javaqueryCliJar = Files.createTempFile(null, ".jar");
        final Path argumentsFilePath = Files.createTempFile(null, ".args");
        try {
            extractJavaqueryCliJar(javaqueryCliJar);
            writeArgumentsFile(argumentsFilePath, arguments);

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

    private void writeArgumentsFile(@Nonnull final Path argumentsFilePath, @Nonnull final List<String> arguments) throws IOException {
        LOG.debug("Writing arguments file with content: " + String.join("\n", arguments));

        Files.write(argumentsFilePath, arguments);
    }
}
