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

public class FieldTestWithArrayType {
    @Test
    void verifyFieldIsDetected() throws URISyntaxException {
        final Type typeWithField = new Type("pkg.TypeWithField", "TypeWithField");
        final Type stringType = new Type("java.lang.String", "String");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final HasFieldRelationship hasFieldRelationship = new HasFieldRelationship(typeWithField, field);
        final OfTypeRelationship ofTypeRelationship = new OfTypeRelationship(field, stringType);
        final Set<Object> expectedElements = Set.of(typeWithField, field, hasFieldRelationship, ofTypeRelationship, stringType);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(FieldTestWithArrayType.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
