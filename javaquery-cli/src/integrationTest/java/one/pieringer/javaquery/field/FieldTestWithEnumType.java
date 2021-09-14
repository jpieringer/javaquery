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

public class FieldTestWithEnumType {
    @Test
    void verifyFieldIsDetected() throws URISyntaxException {
        final Type typeWithField = Type.createClass("pkg.TypeWithField", "TypeWithField");
        final Type enumType = Type.createEnum("pkg.EnumType", "EnumType");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final HasFieldRelationship hasFieldRelationship = new HasFieldRelationship(typeWithField, field);
        final OfTypeRelationship ofTypeRelationship = new OfTypeRelationship(field, enumType);
        final Set<Object> expectedElements = Set.of(typeWithField, field, enumType, hasFieldRelationship, ofTypeRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(FieldTestWithEnumType.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
