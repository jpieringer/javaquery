package one.pieringer.javaquery;

import one.pieringer.javaquery.analyzer.Analyzer;
import one.pieringer.javaquery.analyzer.SourceCodeAnalyzerFactory;
import one.pieringer.javaquery.database.EmbeddedDatabase;
import one.pieringer.javaquery.database.GraphPersistence;
import one.pieringer.javaquery.plantuml.PlantUmlToSvgTransformer;
import one.pieringer.javaquery.plantuml.PlantUmlTransformer;
import one.pieringer.javaquery.plantuml.QueryRunner;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import scala.reflect.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {

    private static final String OPTION_ANALYZE = "analyze";
    private static final String OPTION_QUERY = "query";
    private static final String OPTION_DATABASE_URI = "databaseUri";

    private static final Path EMBEDDED_DATABASE_DIRECTORY = Paths.get("database");
    private static final String EMBEDDED_DATABASE_NAME = "neo4j";

    private final Analyzer analyzer;
    private final QueryRunner queryRunner;
    private final EmbeddedDatabase embeddedDatabase;

    public static void main(String[] args) throws Exception {
        final var analyzer = new Analyzer(new SourceCodeAnalyzerFactory());
        final var queryRunner = new QueryRunner(new PlantUmlTransformer(), new PlantUmlToSvgTransformer());
        final var embeddedDatabase = new EmbeddedDatabase(EMBEDDED_DATABASE_DIRECTORY, EMBEDDED_DATABASE_NAME);

        final var main = new Main(analyzer, queryRunner, embeddedDatabase);
        main.run(parseArguments(args));
    }

    @Nonnull
    private static CommandLineOptions parseArguments(@Nonnull final String[] args) throws ParseException {
        Objects.requireNonNull(args);

        final Options options = new Options();
        options.addOption(OPTION_ANALYZE, true, "Analyze the source code of the specified folders separated with a ';'.");
        options.addOption(OPTION_QUERY, true, "Run the specified Cypher query.");
        options.addOption(OPTION_DATABASE_URI, true,
                "The URI of the database that should be connected to. The embedded database is used if this parameter is omitted.");
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, args);

        final String analyze = cmd.getOptionValue(OPTION_ANALYZE, null);
        final String query = cmd.getOptionValue(OPTION_QUERY, null);

        List<String> sourceDirectories = new ArrayList<>();
        if (analyze != null) {
            sourceDirectories = Arrays.asList(StringUtils.split(analyze, File.pathSeparator()));
        }

        return new CommandLineOptions(
                analyze != null,
                sourceDirectories,
                query,
                cmd.getOptionValue(OPTION_DATABASE_URI, null));
    }


    public Main(@Nonnull final Analyzer analyzer, @Nonnull final QueryRunner queryRunner,
                @Nonnull final EmbeddedDatabase embeddedDatabase) {
        this.analyzer = Objects.requireNonNull(analyzer);
        this.queryRunner = Objects.requireNonNull(queryRunner);
        this.embeddedDatabase = Objects.requireNonNull(embeddedDatabase);
    }

    public void run(@Nonnull final CommandLineOptions commandLineOptions) throws Exception {
        Objects.requireNonNull(commandLineOptions);

        if (commandLineOptions.databaseUri == null) {
            try (final AutoCloseable ignored = embeddedDatabase.startEmbeddedDatabase()) {
                performWork(commandLineOptions, embeddedDatabase.getDatabaseUri());
            }
        } else {
            performWork(commandLineOptions, commandLineOptions.databaseUri);
        }
    }


    private void performWork(@Nonnull final CommandLineOptions commandLineOptions, @Nonnull final String databaseUri) throws IOException {
        final SessionFactory sessionFactory = setupDatabase(databaseUri);
        final GraphPersistence graphPersistence = new GraphPersistence(sessionFactory);

        try {
            if (commandLineOptions.doAnalyze) {
                analyzer.analyze(commandLineOptions.sourceDirectories, graphPersistence);
            }

            if (commandLineOptions.query != null) {
                queryRunner.runQuery(commandLineOptions.query, graphPersistence);
            }
        } finally {
            sessionFactory.close();
        }
    }

    @Nonnull
    private SessionFactory setupDatabase(@CheckForNull String databaseUri) {
        final Configuration configuration = new Configuration.Builder()
                .uri(databaseUri)
                .build();

        return new SessionFactory(configuration, "one.pieringer.javaquery.model");
    }

    private static class CommandLineOptions {
        final boolean doAnalyze;
        final List<String> sourceDirectories;
        final String query;
        final String databaseUri;

        public CommandLineOptions(final boolean doAnalyze, @Nonnull final List<String> sourceDirectories,
                                  @CheckForNull final String query,
                                  @CheckForNull final String databaseUri) {
            this.doAnalyze = doAnalyze;
            this.sourceDirectories = Objects.requireNonNull(sourceDirectories);
            this.query = query;
            this.databaseUri = databaseUri;
        }
    }
}
