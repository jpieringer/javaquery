package one.pieringer.javaquery.plantuml;

import one.pieringer.javaquery.database.GraphPersistence;
import one.pieringer.javaquery.database.ResultSet;
import one.pieringer.javaquery.model.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class QueryRunner {

    private static final Logger LOG = LogManager.getLogger(QueryRunner.class);
    private static final String OUTPUT_SVG = "output.svg";

    @Nonnull
    final PlantUmlTransformer plantUmlTransformer;
    @Nonnull
    final PlantUmlToSvgTransformer plantUmlToSvgTransformer;

    public QueryRunner(@Nonnull final PlantUmlTransformer plantUmlTransformer,
                       @Nonnull final PlantUmlToSvgTransformer plantUmlToSvgTransformer) {
        this.plantUmlTransformer = Objects.requireNonNull(plantUmlTransformer);
        this.plantUmlToSvgTransformer = Objects.requireNonNull(plantUmlToSvgTransformer);
    }

    public void runQuery(@Nonnull final String query, @Nonnull final HashMap<String, String> stereotypeQueries,
                         @Nonnull final GraphPersistence graphPersistence, @CheckForNull String outPath) throws IOException {
        Objects.requireNonNull(query);
        Objects.requireNonNull(graphPersistence);

        LOG.info("Run query...");

        final ResultSet resultSet = graphPersistence.executeQuery(query);
        final Map<Type, List<String>> classToStereotypesMap = executeStereotypeQueries(stereotypeQueries, graphPersistence);

        final String plantUml = plantUmlTransformer.transform(resultSet, classToStereotypesMap);
        if (outPath == null) {
            outPath = OUTPUT_SVG;
        }
        plantUmlToSvgTransformer.generateSvg(plantUml, outPath);

        LOG.info("Finished query...");
    }

    @Nonnull
    private Map<Type, List<String>> executeStereotypeQueries(@Nonnull final HashMap<String, String> stereotypeQueries,
                                                             @Nonnull final GraphPersistence graphPersistence) {
        final HashMap<Type, List<String>> classToStereotypesMap = new HashMap<>();
        for (Map.Entry<String, String> entry : stereotypeQueries.entrySet()) {
            final ResultSet resultSet = graphPersistence.executeQuery(entry.getValue());
            resultSet.getTypes().forEach(
                    type -> classToStereotypesMap.computeIfAbsent(type, k -> new ArrayList<>()).add(entry.getKey()));
        }

        return classToStereotypesMap;
    }
}
