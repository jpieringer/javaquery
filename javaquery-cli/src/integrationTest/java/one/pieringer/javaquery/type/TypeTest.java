package one.pieringer.javaquery.type;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeTest {

    @Test
    void verifyTypeIsDetected() throws URISyntaxException {
        final Set<Object> expectedElements = Set.of(
                Type.createClass("pkg.SingleType", "SingleType")
        );

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(TypeTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
