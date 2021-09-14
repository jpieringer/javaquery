package one.pieringer.javaquery.type;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AnonymousClassTest {

    @Test
    void verifyTypeIsDetected() throws URISyntaxException {
        final Type singleType = Type.createClass("pkg.SingleType", "SingleType");
        final Method method = new Method("pkg.SingleType.method()", "method()");

        final Type anonymousClass = Type.createClass("pkg.SingleType$1", "SingleType$1");
        final Method runMethod = new Method("pkg.SingleType$1.run()", "run()");
        final Constructor constructor = new Constructor("pkg.SingleType$1.<init>()", "<init>()");
        final Type superType = Type.createInterface("java.lang.Runnable", "Runnable");


        final Set<Object> expectedElements = Set.of(
                singleType,
                method,
                new HasMethodRelationship(singleType, method),
                anonymousClass,
                runMethod,
                new HasMethodRelationship(anonymousClass, runMethod),
                constructor,
                new HasConstructorRelationship(anonymousClass, constructor),
                superType,
                new InheritanceRelationship(anonymousClass, superType),
                new InvokeRelationship(method, constructor)
        );

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(AnonymousClassTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
