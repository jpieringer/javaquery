package one.pieringer.javaquery.analyzer;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import one.pieringer.javaquery.database.GraphPersistence;

public class Analyzer {

    private static final Logger LOG = LogManager.getLogger(Analyzer.class);

    @Nonnull
    final SourceCodeAnalyzerFactory sourceCodeAnalyzerFactory;

    public Analyzer(@Nonnull final SourceCodeAnalyzerFactory sourceCodeAnalyzerFactory) {
        this.sourceCodeAnalyzerFactory = Objects.requireNonNull(sourceCodeAnalyzerFactory);
    }

    public void analyze(@Nonnull final List<String> sourceDirectories, @Nonnull final List<String> dependencies,
                        @Nonnull final GraphPersistence graphPersistence) {
        Objects.requireNonNull(sourceDirectories);
        Objects.requireNonNull(dependencies);
        Objects.requireNonNull(graphPersistence);

        LOG.info("Analyzing...");

        final List<File> dependencySourceDirectories = new ArrayList<>();
        final List<File> dependencyJarFiles = new ArrayList<>();

        for (String dependency : dependencies) {
            final File dependencyFile = new File(dependency);

            if (!dependencyFile.exists()) {
                LOG.warn("The following file does not exist {}.", dependencyFile);
            } else if (dependencyFile.isFile()) {
                if ("jar".equalsIgnoreCase(FilenameUtils.getExtension(dependency))) {
                    dependencyJarFiles.add(dependencyFile);
                } else {
                    throw new IllegalArgumentException("The provided dependency '" + dependency + "' is a file but does not have the extension jar.");
                }
            } else if (dependencyFile.isDirectory()) {
                dependencySourceDirectories.add(dependencyFile);
            } else {
                throw new IllegalArgumentException("The provided dependency '" + dependency + "' is not a file and not a directory.");
            }
        }

        final SourceCodeProvider sourceCodeProvider = new SourceCodeProvider(
                sourceDirectories.stream().map(File::new).collect(Collectors.toList()),
                dependencySourceDirectories,
                dependencyJarFiles);

        final SourceCodeAnalyzer sourceCodeAnalyzer = sourceCodeAnalyzerFactory.create();

        StopWatch stopWatch = StopWatch.createStarted();
        final Set<Object> graph = sourceCodeAnalyzer.analyze(sourceCodeProvider);
        LOG.info("Found {} nodes and relationships.", graph.size());
        LOG.info("Analysis took {} sec.", stopWatch.getTime(TimeUnit.SECONDS));

        stopWatch = StopWatch.createStarted();
        graphPersistence.clear();
        LOG.info("Cleared stored graph in {} sec.", stopWatch.getTime(TimeUnit.SECONDS));

        stopWatch = StopWatch.createStarted();
        graphPersistence.persist(graph);
        LOG.info("Stored graph in {} sec.", stopWatch.getTime(TimeUnit.SECONDS));
    }
}
