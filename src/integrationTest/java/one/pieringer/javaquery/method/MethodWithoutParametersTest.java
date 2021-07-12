package one.pieringer.javaquery.method;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodWithoutParametersTest {
    @Test
    void verifyMethodIsDetected() throws URISyntaxException {
        final Type type = new Type("pkg.TypeWithMethod", "TypeWithMethod");
        final Method method = new Method("pkg.TypeWithMethod.method()", "method()");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(type, method);
        final Set<Object> expectedElements = Set.of(type, method, hasMethodRelationship);


        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(MethodWithoutParametersTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
