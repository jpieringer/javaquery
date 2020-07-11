package one.pieringer.javaquery.plantuml;

import one.pieringer.javaquery.database.GraphPersistence;
import one.pieringer.javaquery.database.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

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

    public void runQuery(@Nonnull final String query, @Nonnull final GraphPersistence graphPersistence) throws IOException {
        Objects.requireNonNull(query);
        Objects.requireNonNull(graphPersistence);

        LOG.info("Run query...");

        ResultSet resultSet = graphPersistence.executeQuery(query);
        String plantUml = plantUmlTransformer.transform(resultSet);
        plantUmlToSvgTransformer.generatePng(plantUml, OUTPUT_SVG);

        LOG.info("Finished query...");

        try {
            Desktop.getDesktop().open(new File(OUTPUT_SVG));
        } catch (IOException e) {
            LOG.warn("Could not open class diagram with the default application.", e);
        }
    }
}
