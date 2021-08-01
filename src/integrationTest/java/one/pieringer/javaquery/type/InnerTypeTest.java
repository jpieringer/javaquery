package one.pieringer.javaquery.type;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Type;

public class InnerTypeTest {

    @Test
    void verifyTypeIsDetected() throws URISyntaxException {
        final Set<Object> expectedElements = Set.of(
                new Type("pkg.SingleType", "SingleType"),
                new Type("pkg.SingleType.InnerClass", "InnerClass")
        );

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(InnerTypeTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
