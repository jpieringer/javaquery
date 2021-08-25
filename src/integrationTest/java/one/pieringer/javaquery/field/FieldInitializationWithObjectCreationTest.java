package one.pieringer.javaquery.field;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldInitializationWithObjectCreationTest {
    @Test
    void verifyFieldInitializationIsDetected() throws URISyntaxException {
        final Type typeWithField = Type.createClass("pkg.TypeWithField", "TypeWithField");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final Constructor typeWithFieldConstructor = new Constructor("pkg.TypeWithField.<init>()", "<init>()");

        final Type bigIntegerType = Type.createClass("java.math.BigInteger", "BigInteger");
        final Constructor bigIntegerConstructor = new Constructor("java.math.BigInteger.<init>(long)", "<init>(long)");

        final Set<Object> expectedElements = Set.of(
                typeWithField,
                field,
                new HasFieldRelationship(typeWithField, field),
                new OfTypeRelationship(field, bigIntegerType),
                typeWithFieldConstructor,
                new HasConstructorRelationship(typeWithField, typeWithFieldConstructor),

                bigIntegerType,
                bigIntegerConstructor,
                new HasConstructorRelationship(bigIntegerType, bigIntegerConstructor),

                new InvokeRelationship(typeWithFieldConstructor, bigIntegerConstructor));

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(FieldInitializationWithObjectCreationTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
