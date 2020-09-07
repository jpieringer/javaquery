package one.pieringer.javaquery.analyzer;

import one.pieringer.javaquery.database.GraphPersistence;
import one.pieringer.javaquery.database.ResultSet;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Objects;
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

        SourceCodeProvider sourceCodeProvider = new SourceCodeProvider(
                sourceDirectories.stream().map(File::new).collect(Collectors.toList())
                        .toArray(new File[sourceDirectories.size()]));
        SourceCodeAnalyzer sourceCodeAnalyzer = sourceCodeAnalyzerFactory.create(sourceCodeProvider);

        StopWatch stopWatch = StopWatch.createStarted();
        ResultSet resultSet = sourceCodeAnalyzer.analyze(sourceCodeProvider);
        LOG.info("Found {} types.", resultSet.getTypes().size());
        LOG.info("Found {} inheritance relationships.", resultSet.getInheritanceRelationships().size());
        LOG.info("Found {} field relationships.", resultSet.getFieldRelationships().size());
        LOG.info("Found {} create instance relationships.", resultSet.getCreateInstanceRelationships().size());
        LOG.info("Found {} invoke relationships.", resultSet.getInvokeRelationships().size());
        LOG.info("Found {} access field relationships.", resultSet.getAccessFieldRelationships().size());
        LOG.info("Analysis took {} sec.", stopWatch.getTime(TimeUnit.SECONDS));

        stopWatch = StopWatch.createStarted();
        graphPersistence.clear();
        LOG.info("Cleared stored graph in {} sec.", stopWatch.getTime(TimeUnit.SECONDS));

        stopWatch = StopWatch.createStarted();
        graphPersistence.persist(resultSet.getTypes());
        graphPersistence.persist(resultSet.getCreateInstanceRelationships());
        graphPersistence.persist(resultSet.getFieldRelationships());
        graphPersistence.persist(resultSet.getInheritanceRelationships());
        graphPersistence.persist(resultSet.getInvokeRelationships());
        graphPersistence.persist(resultSet.getAccessFieldRelationships());
        LOG.info("Stored graph in {} sec.", stopWatch.getTime(TimeUnit.SECONDS));
    }
}
