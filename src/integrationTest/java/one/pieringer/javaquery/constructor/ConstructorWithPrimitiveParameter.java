package one.pieringer.javaquery.constructor;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Constructor;
import one.pieringer.javaquery.model.HasConstructorRelationship;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstructorWithPrimitiveParameter {
    @Test
    void verifyConstructorIsDetected() throws URISyntaxException {
        final Type type = new Type("pkg.TypeWithConstructor", "TypeWithConstructor");
        final Constructor constructor = new Constructor("pkg.TypeWithConstructor.<init>(int)", "<init>(int)");
        final HasConstructorRelationship hasConstructorRelationship = new HasConstructorRelationship(type, constructor);
        final Set<Object> expectedElements = Set.of(type, constructor, hasConstructorRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(ConstructorWithPrimitiveParameter.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
