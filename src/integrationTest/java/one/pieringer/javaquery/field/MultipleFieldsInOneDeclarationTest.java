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

public class MultipleFieldsInOneDeclarationTest {
    @Test
    void verifyMultipleFieldsAreDetected() throws URISyntaxException {
        final Type typeWithField = new Type("pkg.TypeWithField", "TypeWithField");
        final Type typeOfField = new Type("pkg.TypeOfField", "TypeOfField");
        final Field field1 = new Field("pkg.TypeWithField.field1", "field1");
        final Field field2 = new Field("pkg.TypeWithField.field2", "field2");
        final HasFieldRelationship hasFieldRelationship1 = new HasFieldRelationship(typeWithField, field1);
        final HasFieldRelationship hasFieldRelationship2 = new HasFieldRelationship(typeWithField, field2);
        final OfTypeRelationship ofTypeRelationship1 = new OfTypeRelationship(field1, typeOfField);
        final OfTypeRelationship ofTypeRelationship2 = new OfTypeRelationship(field2, typeOfField);
        final Set<Object> expectedElements = Set.of(typeOfField, typeWithField,
                field1, field2,
                hasFieldRelationship1, hasFieldRelationship2,
                ofTypeRelationship1, ofTypeRelationship2);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(MultipleFieldsInOneDeclarationTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
