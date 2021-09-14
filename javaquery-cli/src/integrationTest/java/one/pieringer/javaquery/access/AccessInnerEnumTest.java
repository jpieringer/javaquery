package one.pieringer.javaquery.access;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.HasMethodRelationship;
import one.pieringer.javaquery.model.Method;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessInnerEnumTest {
    @Test
    void verifyEnumFieldAccessIsDetected() throws URISyntaxException {
        final Type outerClass = Type.createClass("pkg.OuterClass", "OuterClass");
        final Type innerEnumType = Type.createEnum("pkg.OuterClass.InnerEnumType", "InnerEnumType");

        final Type typeThatAccessesInnerEnum = Type.createClass("pkg.TypeThatAccessesInnerEnum", "TypeThatAccessesInnerEnum");
        final Method method = new Method("pkg.TypeThatAccessesInnerEnum.method()", "method()");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(typeThatAccessesInnerEnum, method);

        final Set<Object> expectedElements = Set.of(innerEnumType, outerClass, typeThatAccessesInnerEnum, method, hasMethodRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(AccessInnerEnumTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
