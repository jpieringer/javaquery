package one.pieringer.javaquery.plantuml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import javax.annotation.Nonnull;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class PlantUmlToSvgTransformer {

    public void generatePng(@Nonnull final String plantUml, @Nonnull final String outputPath) throws IOException {
        Objects.requireNonNull(plantUml);
        Objects.requireNonNull(outputPath);
        SourceStringReader reader = new SourceStringReader(plantUml);
        reader.outputImage(new FileOutputStream(outputPath), new FileFormatOption(FileFormat.SVG));
    }
}
