package one.pieringer.javaquery.method;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.HasMethodRelationship;
import one.pieringer.javaquery.model.Method;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodWithParameterTest {
    @Test
    void verifyMethodIsDetected() throws URISyntaxException {
        final Type type = Type.createClass("pkg.TypeWithMethod", "TypeWithMethod");
        final Method method = new Method("pkg.TypeWithMethod.method(java.lang.String)", "method(String)");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(type, method);
        final Set<Object> expectedElements = Set.of(type, method, hasMethodRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(MethodWithParameterTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
