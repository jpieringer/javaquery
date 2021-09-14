package one.pieringer.javaquery.constructor;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Constructor;
import one.pieringer.javaquery.model.HasConstructorRelationship;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstructorWithEnumParameterTest {
    @Test
    void verifyConstructorIsDetected() throws URISyntaxException {
        final Type enumType = Type.createEnum("pkg.EnumType", "EnumType");
        final Type type = Type.createClass("pkg.TypeWithConstructor", "TypeWithConstructor");
        final Constructor constructor = new Constructor("pkg.TypeWithConstructor.<init>(pkg.EnumType)", "<init>(EnumType)");
        final HasConstructorRelationship hasConstructorRelationship = new HasConstructorRelationship(type, constructor);
        final Set<Object> expectedElements = Set.of(enumType, type, constructor, hasConstructorRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(ConstructorWithEnumParameterTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
