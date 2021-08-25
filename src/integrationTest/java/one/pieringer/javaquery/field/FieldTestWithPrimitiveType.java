package one.pieringer.javaquery.field;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Field;
import one.pieringer.javaquery.model.HasFieldRelationship;
import one.pieringer.javaquery.model.OfTypeRelationship;
import one.pieringer.javaquery.model.Type;

public class FieldTestWithPrimitiveType {
    @Test
    void verifyFieldIsDetected() throws URISyntaxException {
        final Type typeWithField = Type.createClass("pkg.TypeWithField", "TypeWithField");
        final Type intType = Type.createPrimitive("int", "int");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final HasFieldRelationship hasFieldRelationship = new HasFieldRelationship(typeWithField, field);
        final OfTypeRelationship ofTypeRelationship = new OfTypeRelationship(field, intType);
        final Set<Object> expectedElements = Set.of(typeWithField, intType, field, hasFieldRelationship, ofTypeRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(FieldTestWithPrimitiveType.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
