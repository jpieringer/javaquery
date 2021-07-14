package one.pieringer.javaquery.invoke;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.HasMethodRelationship;
import one.pieringer.javaquery.model.InvokeRelationship;
import one.pieringer.javaquery.model.Method;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InvokeMethodWithTypeArgumentTest {
    @Test
    void verifyInvokeMethodIsDetected() throws URISyntaxException {
        final Type typeWithMethod = new Type("java.util.Collections", "Collections");
        final Method method = new Method("java.util.Collections.singleton(T)", "singleton(T)");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(typeWithMethod, method);

        final Type typeThatInvokesMethod = new Type("pkg.TypeThatInvokesMethod", "TypeThatInvokesMethod");
        final Method invokingMethod = new Method("pkg.TypeThatInvokesMethod.invokingMethod()", "invokingMethod()");
        final HasMethodRelationship hasInvokingMethodRelationship = new HasMethodRelationship(typeThatInvokesMethod, invokingMethod);

        final InvokeRelationship invokeRelationship = new InvokeRelationship(invokingMethod, method);

        final Set<Object> expectedElements = Set.of(typeWithMethod, method, hasMethodRelationship, typeThatInvokesMethod, invokingMethod, hasInvokingMethodRelationship, invokeRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(InvokeMethodWithTypeArgumentTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
