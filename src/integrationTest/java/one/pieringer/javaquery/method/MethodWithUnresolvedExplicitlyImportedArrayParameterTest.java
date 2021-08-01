package one.pieringer.javaquery.method;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.HasMethodRelationship;
import one.pieringer.javaquery.model.Method;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodWithUnresolvedExplicitlyImportedArrayParameterTest {
    @Test
    void verifyMethodIsDetected() throws URISyntaxException {
        final Type type = new Type("pkg.TypeWithMethod", "TypeWithMethod");
        final Method method = new Method("pkg.TypeWithMethod.method(unresolvedpkg.UnresolvedType[])", "method(UnresolvedType[])");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(type, method);
        final Set<Object> expectedElements = Set.of(type, method, hasMethodRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(MethodWithUnresolvedExplicitlyImportedArrayParameterTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
