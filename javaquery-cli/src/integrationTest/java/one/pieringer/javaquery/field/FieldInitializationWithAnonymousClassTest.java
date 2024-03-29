package one.pieringer.javaquery.field;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldInitializationWithAnonymousClassTest {
    @Test
    void verifyFieldInitializationIsDetected() throws URISyntaxException {
        final Type anonymousClass = Type.createClass("pkg.TypeWithNewInstance$1", "TypeWithNewInstance$1");
        final Constructor anonymousClassConstructor = new Constructor("pkg.TypeWithNewInstance$1.<init>()", "<init>()");
        final Method anonymousClassMethod = new Method("pkg.TypeWithNewInstance$1.run()", "run()");
        final Type runnable = Type.createInterface("java.lang.Runnable", "Runnable");

        final Type typeWithNewInstance = Type.createClass("pkg.TypeWithNewInstance", "TypeWithNewInstance");
        final Field field = new Field("pkg.TypeWithNewInstance.field", "field");
        final Constructor constructor = new Constructor("pkg.TypeWithNewInstance.<init>()", "<init>()");

        final Set<Object> expectedElements = Set.of(
                anonymousClass,
                anonymousClassConstructor,
                new HasConstructorRelationship(anonymousClass, anonymousClassConstructor),
                anonymousClassMethod,
                new HasMethodRelationship(anonymousClass, anonymousClassMethod),
                runnable,
                new InheritanceRelationship(anonymousClass, runnable),

                typeWithNewInstance,
                field,
                new HasFieldRelationship(typeWithNewInstance, field),
                new OfTypeRelationship(field, runnable),
                constructor,
                new HasConstructorRelationship(typeWithNewInstance, constructor),
                new InvokeRelationship(constructor, anonymousClassConstructor));

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(FieldInitializationWithAnonymousClassTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
