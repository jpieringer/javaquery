package one.pieringer.javaquery;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;

import one.pieringer.javaquery.analyzer.ASTUtils;
import one.pieringer.javaquery.analyzer.Analyzer;
import one.pieringer.javaquery.analyzer.SourceCodeAnalyzerFactory;
import one.pieringer.javaquery.database.EmbeddedDatabase;
import one.pieringer.javaquery.database.GraphPersistence;
import one.pieringer.javaquery.plantuml.PlantUmlToFileWriter;
import one.pieringer.javaquery.plantuml.PlantUmlTransformer;
import one.pieringer.javaquery.plantuml.QueryRunner;
import scala.reflect.io.File;

public class Main {

    private static final String OPTION_ANALYZE = "analyze";
    private static final String OPTION_DEPENDENCIES = "dependencies";
    private static final String OPTION_QUERY = "query";
    private static final String OPTION_DATABASE_URI = "databaseUri";
    private static final String OPTION_STEREOTYPE = "stereotype";
    private static final String OPTION_STEREOTYPE_QUERY = "stereotypeQuery";
    private static final String OPTION_OUT_SVG = "outSvg";
    private static final String OPTION_OUT_PDF = "outPdf";

    private static final Path EMBEDDED_DATABASE_DIRECTORY = Paths.get("database");
    private static final String EMBEDDED_DATABASE_NAME = "neo4j";

    private final Analyzer analyzer;
    private final QueryRunner queryRunner;
    private final EmbeddedDatabase embeddedDatabase;

    public static void main(String[] args) throws Exception {
        System.out.println("Args: " + Arrays.deepToString(args));

        if (args.length == 1 && args[0].startsWith("@")) {
            args = Files.readAllLines(Path.of(args[0].substring(1))).toArray(new String[0]);
        }

        final var analyzer = new Analyzer(new SourceCodeAnalyzerFactory(new ASTUtils()));
        final var queryRunner = new QueryRunner(new PlantUmlTransformer(), new PlantUmlToFileWriter());
        final var embeddedDatabase = new EmbeddedDatabase(EMBEDDED_DATABASE_DIRECTORY, EMBEDDED_DATABASE_NAME);

        final var main = new Main(analyzer, queryRunner, embeddedDatabase);
        main.run(parseArguments(args));
    }

    @Nonnull
    private static CommandLineOptions parseArguments(@Nonnull final String[] args) throws ParseException {
        Objects.requireNonNull(args);

        final Options options = new Options();
        options.addOption(OPTION_ANALYZE, true, "Analyze the source code of the specified folders separated with a ';'.");
        options.addOption(OPTION_DEPENDENCIES, true, "The dependencies of the source code that is used for resolving types. Jar files or folders separated with a ';'.");
        options.addOption(OPTION_QUERY, true, "Run the specified Cypher query.");
        options.addOption(OPTION_DATABASE_URI, true,
                "The URI of the database that should be connected to. The embedded database is used if this parameter is omitted.");
        options.addOption(OPTION_STEREOTYPE, true, "The name of the stereotype that should be attached to certain classes.");
        options.addOption(OPTION_STEREOTYPE_QUERY, true, "The query that returns all classes to which the previous specified stereotype should be attached.");
        options.addOption(OPTION_OUT_SVG, true, "The path where the generated SVG diagram should be stored.");
        options.addOption(OPTION_OUT_PDF, true, "The path where the generated PDF diagram should be stored.");
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, args);

        final String analyze = cmd.getOptionValue(OPTION_ANALYZE, null);
        final String dependencyOptionString = cmd.getOptionValue(OPTION_DEPENDENCIES, null);
        final String query = cmd.getOptionValue(OPTION_QUERY, null);

        List<String> sourceDirectories = new ArrayList<>();
        if (analyze != null) {
            sourceDirectories = Arrays.asList(StringUtils.split(analyze, File.pathSeparator()));
        }

        List<String> dependencies = new ArrayList<>();
        if (analyze != null) {
            dependencies = Arrays.asList(StringUtils.split(dependencyOptionString, File.pathSeparator()));
        }

        final String[] stereotypes = ArrayUtils.nullToEmpty(cmd.getOptionValues(OPTION_STEREOTYPE));
        final String[] stereotypeQueries = ArrayUtils.nullToEmpty(cmd.getOptionValues(OPTION_STEREOTYPE_QUERY));

        if (!ArrayUtils.isSameLength(stereotypes, stereotypeQueries)) {
            throw new IllegalArgumentException("Number of " + OPTION_STEREOTYPE + " arguments and " + OPTION_STEREOTYPE_QUERY + " arguments must be equal.");
        }

        final HashMap<String, String> stereotypeQueryMap = new HashMap<>();
        for (int i = 0; i < stereotypes.length; i++) {
            stereotypeQueryMap.put(stereotypes[i], stereotypeQueries[i]);
        }

        return new CommandLineOptions(
                analyze != null,
                sourceDirectories,
                dependencies,
                query,
                stereotypeQueryMap,
                cmd.getOptionValue(OPTION_DATABASE_URI, null),
                cmd.getOptionValue(OPTION_OUT_SVG, null),
                cmd.getOptionValue(OPTION_OUT_PDF, null));
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


    private void performWork(@Nonnull final CommandLineOptions commandLineOptions, @Nonnull final String databaseUri)
            throws IOException, TranscoderException {
        final SessionFactory sessionFactory = setupDatabase(databaseUri);
        final GraphPersistence graphPersistence = new GraphPersistence(sessionFactory);

        try {
            if (commandLineOptions.doAnalyze) {
                analyzer.analyze(commandLineOptions.sourceDirectories, commandLineOptions.dependencies, graphPersistence);
            }

            if (commandLineOptions.query != null) {
                queryRunner.runQuery(commandLineOptions.query, commandLineOptions.stereotypeQueries, graphPersistence,
                        commandLineOptions.outSvgPath, commandLineOptions.outPdfPath);
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
        @Nonnull
        final List<String> sourceDirectories;
        @Nonnull
        final List<String> dependencies;
        @CheckForNull
        final String query;
        @Nonnull
        final HashMap<String, String> stereotypeQueries;
        @CheckForNull
        final String databaseUri;
        @CheckForNull
        final String outSvgPath;
        @CheckForNull
        final String outPdfPath;

        public CommandLineOptions(final boolean doAnalyze, @Nonnull final List<String> sourceDirectories,
                                  @Nonnull final List<String> dependencies, @CheckForNull final String query,
                                  @Nonnull final HashMap<String, String> stereotypeQueries,
                                  @CheckForNull final String databaseUri,
                                  @CheckForNull final String outSvgPath,
                                  @CheckForNull final String outPdfPath) {
            this.doAnalyze = doAnalyze;
            this.sourceDirectories = Objects.requireNonNull(sourceDirectories);
            this.dependencies = Objects.requireNonNull(dependencies);
            this.query = query;
            this.stereotypeQueries = Objects.requireNonNull(stereotypeQueries);
            this.databaseUri = databaseUri;
            this.outSvgPath = outSvgPath;
            this.outPdfPath = outPdfPath;
        }
    }
}
