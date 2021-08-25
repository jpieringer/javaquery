package one.pieringer.javaquery.field;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Field;
import one.pieringer.javaquery.model.HasFieldRelationship;
import one.pieringer.javaquery.model.OfTypeRelationship;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldTestWithReferenceType {
    @Test
    void verifyFieldIsDetected() throws URISyntaxException {
        final Type typeWithField = Type.createClass("pkg.TypeWithField", "TypeWithField");
        final Type typeOfField = Type.createClass("pkg.TypeOfField", "TypeOfField");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final HasFieldRelationship hasFieldRelationship = new HasFieldRelationship(typeWithField, field);
        final OfTypeRelationship ofTypeRelationship = new OfTypeRelationship(field, typeOfField);
        final Set<Object> expectedElements = Set.of(typeOfField, typeWithField, field, hasFieldRelationship, ofTypeRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(FieldTestWithReferenceType.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
