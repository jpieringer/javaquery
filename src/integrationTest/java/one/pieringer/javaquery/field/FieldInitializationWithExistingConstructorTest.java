package one.pieringer.javaquery.field;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldInitializationWithExistingConstructorTest {
    @Test
    void verifyFieldInitializationIsDetected() throws URISyntaxException {
        final Type typeWithField = new Type("pkg.TypeWithField", "TypeWithField");
        final Field field = new Field("pkg.TypeWithField.field", "field");
        final Constructor typeWithFieldConstructor = new Constructor("pkg.TypeWithField.<init>(int)", "<init>(int)");

        final Type bigIntegerType = new Type("java.math.BigInteger", "BigInteger");
        final Method bigIntegerMethod = new Method("java.math.BigInteger.valueOf(long)", "valueOf(long)");

        final Set<Object> expectedElements = Set.of(
                typeWithField,
                field,
                new HasFieldRelationship(typeWithField, field),
                new OfTypeRelationship(field, bigIntegerType),
                typeWithFieldConstructor,
                new HasConstructorRelationship(typeWithField, typeWithFieldConstructor),

                bigIntegerType,
                bigIntegerMethod,
                new HasMethodRelationship(bigIntegerType, bigIntegerMethod),

                new InvokeRelationship(typeWithFieldConstructor, bigIntegerMethod));

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(FieldInitializationWithExistingConstructorTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
