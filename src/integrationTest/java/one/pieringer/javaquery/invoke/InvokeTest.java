package one.pieringer.javaquery.invoke;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.access.AccessFieldTest;
import one.pieringer.javaquery.model.HasMethodRelationship;
import one.pieringer.javaquery.model.InvokeRelationship;
import one.pieringer.javaquery.model.Method;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InvokeTest {
    @Test
    void verifyInvokeMethodIsDetected() throws URISyntaxException {
        final Type typeWithMethod = new Type("pkg.TypeWithMethod", "TypeWithMethod");
        final Method method = new Method("pkg.TypeWithMethod.method(java.lang.String, int)", "method(String, int)");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(typeWithMethod, method);

        final Type typeThatInvokesMethod = new Type("pkg.TypeThatInvokesMethod", "TypeThatInvokesMethod");
        final Method invokingMethod = new Method("pkg.TypeThatInvokesMethod.invokingMethod()", "invokingMethod()");
        final HasMethodRelationship hasInvokingMethodRelationship = new HasMethodRelationship(typeThatInvokesMethod, invokingMethod);

        final InvokeRelationship invokeRelationship = new InvokeRelationship(invokingMethod, method);

        final Set<Object> expectedElements = Set.of(typeWithMethod, method, hasMethodRelationship, typeThatInvokesMethod, invokingMethod, hasInvokingMethodRelationship, invokeRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(InvokeTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
