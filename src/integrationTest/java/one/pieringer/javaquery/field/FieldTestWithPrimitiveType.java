package one.pieringer.javaquery.field;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldTestWithPrimitiveType {
    @Test
    void verifyFieldIsDetected() throws URISyntaxException {
        final Type typeWithField = new Type("pkg.TypeWithField", "TypeWithField");
        final Set<Object> expectedElements = Set.of(typeWithField);
        // Primitive type fields are not added to the graph.

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(FieldTestWithPrimitiveType.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
