package one.pieringer.javaquery.access;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.AccessRelationship;
import one.pieringer.javaquery.model.Field;
import one.pieringer.javaquery.model.HasFieldRelationship;
import one.pieringer.javaquery.model.HasMethodRelationship;
import one.pieringer.javaquery.model.Method;
import one.pieringer.javaquery.model.OfTypeRelationship;
import one.pieringer.javaquery.model.Type;

public class AccessArrayFieldTest {
    @Test
    void verifyFieldAccessIsDetected() throws URISyntaxException {
        final Type typeWithField = Type.createClass("pkg.TypeWithField", "TypeWithField");
        final Type typeOfField = Type.createClass("pkg.TypeOfField", "TypeOfField");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final HasFieldRelationship hasFieldRelationship = new HasFieldRelationship(typeWithField, field);
        final OfTypeRelationship ofTypeRelationship = new OfTypeRelationship(field, typeOfField);

        final Type typeThatAccessesField = Type.createClass("pkg.TypeThatAccessesField", "TypeThatAccessesField");
        final Method method = new Method("pkg.TypeThatAccessesField.method()", "method()");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(typeThatAccessesField, method);

        final AccessRelationship accessRelationship = new AccessRelationship(method, field);
        final Set<Object> expectedElements = Set.of(typeOfField, typeWithField, field, hasFieldRelationship, ofTypeRelationship, typeThatAccessesField, method, hasMethodRelationship, accessRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(AccessArrayFieldTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
