package one.pieringer.javaquery.invoke;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InvokeConstructorWithAnonymousClassTest {
    @Test
    void verifyInvokeConstructorIsDetected() throws URISyntaxException {
        final Type anonymousClass = new Type("pkg.TypeWithNewInstance$1", "TypeWithNewInstance$1");
        final Constructor anonymousClassConstructor = new Constructor("pkg.TypeWithNewInstance$1.<init>()", "<init>()");
        final Method anonymousClassMethod = new Method("pkg.TypeWithNewInstance$1.run()", "run()");
        final Type runnable = new Type("java.lang.Runnable", "Runnable");


        final Type typeWithNewInstance = new Type("pkg.TypeWithNewInstance", "TypeWithNewInstance");
        final Method method = new Method("pkg.TypeWithNewInstance.method()", "method()");

        final Set<Object> expectedElements = Set.of(
                anonymousClass,
                anonymousClassConstructor,
                new HasConstructorRelationship(anonymousClass, anonymousClassConstructor),
                anonymousClassMethod,
                new HasMethodRelationship(anonymousClass, anonymousClassMethod),
                runnable,
                new InheritanceRelationship(anonymousClass, runnable),

                typeWithNewInstance,
                method,
                new HasMethodRelationship(typeWithNewInstance, method),
                new InvokeRelationship(method, anonymousClassConstructor));

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(InvokeConstructorWithAnonymousClassTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
