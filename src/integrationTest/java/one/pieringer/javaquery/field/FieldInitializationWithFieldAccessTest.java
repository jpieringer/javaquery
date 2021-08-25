package one.pieringer.javaquery.field;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldInitializationWithFieldAccessTest {
    @Test
    void verifyFieldInitializationIsDetected() throws URISyntaxException {
        final Type typeWithField = Type.createClass("pkg.TypeWithField", "TypeWithField");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final Constructor typeWithFieldConstructor = new Constructor("pkg.TypeWithField.<init>()", "<init>()");

        final Type integerType = Type.createClass("java.lang.Integer", "Integer");
        final Field integerField = new Field("java.lang.Integer.MAX_VALUE", "MAX_VALUE");

        final Type intType = Type.createPrimitive("int", "int");

        final Set<Object> expectedElements = Set.of(
                typeWithField,
                field,
                new HasFieldRelationship(typeWithField, field),
                new OfTypeRelationship(field, integerType),
                typeWithFieldConstructor,
                new HasConstructorRelationship(typeWithField, typeWithFieldConstructor),

                integerType,
                integerField,
                new HasFieldRelationship(integerType, integerField),
                intType,
                new OfTypeRelationship(integerField, intType),

                new AccessRelationship(typeWithFieldConstructor, integerField));

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(FieldInitializationWithFieldAccessTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
