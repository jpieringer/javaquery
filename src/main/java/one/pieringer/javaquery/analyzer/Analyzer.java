package one.pieringer.javaquery.analyzer;

import one.pieringer.javaquery.database.GraphPersistence;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Analyzer {

    private static final Logger LOG = LogManager.getLogger(Analyzer.class);

    @Nonnull
    final SourceCodeAnalyzerFactory sourceCodeAnalyzerFactory;

    public Analyzer(@Nonnull final SourceCodeAnalyzerFactory sourceCodeAnalyzerFactory) {
        this.sourceCodeAnalyzerFactory = Objects.requireNonNull(sourceCodeAnalyzerFactory);
    }

    public void analyze(@Nonnull final List<String> sourceDirectories, @Nonnull final GraphPersistence graphPersistence) {
        Objects.requireNonNull(sourceDirectories);
        Objects.requireNonNull(graphPersistence);

        LOG.info("Analyzing...");

        final SourceCodeProvider sourceCodeProvider = new SourceCodeProvider(
                sourceDirectories.stream().map(File::new).collect(Collectors.toList())
                        .toArray(new File[sourceDirectories.size()]));
        final SourceCodeAnalyzer sourceCodeAnalyzer = sourceCodeAnalyzerFactory.create(sourceCodeProvider);

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
