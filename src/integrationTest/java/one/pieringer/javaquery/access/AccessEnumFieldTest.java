package one.pieringer.javaquery.access;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessEnumFieldTest {
    @Test
    void verifyEnumFieldAccessIsDetected() throws URISyntaxException {
        final Type typeWithField = new Type("pkg.TypeWithField", "TypeWithField");
        final Type typeOfField = new Type("pkg.EnumTypeOfField", "EnumTypeOfField");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final HasFieldRelationship hasFieldRelationship = new HasFieldRelationship(typeWithField, field);
        final OfTypeRelationship ofTypeRelationship = new OfTypeRelationship(field, typeOfField);

        final Type typeThatAccessesField = new Type("pkg.TypeThatAccessesField", "TypeThatAccessesField");
        final Method method = new Method("pkg.TypeThatAccessesField.method()", "method()");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(typeThatAccessesField, method);

        final AccessRelationship accessRelationship = new AccessRelationship(method, field);
        final Set<Object> expectedElements = Set.of(typeOfField, typeWithField, field, hasFieldRelationship, ofTypeRelationship, typeThatAccessesField, method, hasMethodRelationship, accessRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(AccessEnumFieldTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
