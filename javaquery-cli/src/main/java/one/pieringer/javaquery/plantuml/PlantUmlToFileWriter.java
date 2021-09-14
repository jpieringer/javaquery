package one.pieringer.javaquery.plantuml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.*;
import java.util.Objects;

public class PlantUmlToFileWriter {

    public void generateDiagrams(@Nonnull final String plantUml, @CheckForNull final String svgOutputPath,
                                 @CheckForNull final String pdfOutputPath) throws IOException, TranscoderException {
        Objects.requireNonNull(plantUml);

        final SourceStringReader reader = new SourceStringReader(plantUml);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        reader.outputImage(byteArrayOutputStream, new FileFormatOption(FileFormat.SVG));
        final byte[] svgByteArray = byteArrayOutputStream.toByteArray();

        if (svgOutputPath != null) {
            try (final FileOutputStream fileOutputStream = new FileOutputStream(svgOutputPath)) {
                fileOutputStream.write(svgByteArray);
            }
        }

        if (pdfOutputPath != null) {
            final PDFTranscoder transcoder = new PDFTranscoder();
            final ByteArrayInputStream fileInputStream = new ByteArrayInputStream(svgByteArray);
            try (final FileOutputStream fileOutputStream = new FileOutputStream(new File(pdfOutputPath))) {
                final TranscoderInput transcoderInput = new TranscoderInput(fileInputStream);
                final TranscoderOutput transcoderOutput = new TranscoderOutput(fileOutputStream);
                transcoder.transcode(transcoderInput, transcoderOutput);
            }
        }
    }
}
