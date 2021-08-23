package one.pieringer.javaquery.access;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessFieldWithThisTest {
    @Test
    void verifyFieldAccessIsDetected() throws URISyntaxException {
        final Type typeWithField = new Type("pkg.TypeWithField", "TypeWithField");
        final Type string = new Type("java.lang.String", "String");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final HasFieldRelationship hasFieldRelationship = new HasFieldRelationship(typeWithField, field);
        final OfTypeRelationship ofTypeRelationship = new OfTypeRelationship(field, string);

        final Method method = new Method("pkg.TypeWithField.method()", "method()");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(typeWithField, method);

        final AccessRelationship accessRelationship = new AccessRelationship(method, field);
        final Set<Object> expectedElements = Set.of(string, typeWithField, field, hasFieldRelationship, ofTypeRelationship, method, hasMethodRelationship, accessRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(AccessFieldWithThisTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
