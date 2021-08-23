package one.pieringer.javaquery.type;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Constructor;
import one.pieringer.javaquery.model.HasConstructorRelationship;
import one.pieringer.javaquery.model.HasMethodRelationship;
import one.pieringer.javaquery.model.InvokeRelationship;
import one.pieringer.javaquery.model.Method;
import one.pieringer.javaquery.model.Type;

public class LocalClassTest {

    @Test
    void verifyTypeIsDetected() throws URISyntaxException {
        final Type singleType = new Type("pkg.SingleType", "SingleType");
        final Method method = new Method("pkg.SingleType.method()", "method()");

        final Type localClass = new Type("pkg.SingleType$1LocalClass", "SingleType$1LocalClass");
        final Method runMethod = new Method("pkg.SingleType$1LocalClass.run()", "run()");
        final Constructor constructor = new Constructor("pkg.SingleType$1LocalClass.<init>()", "<init>()");


        final Set<Object> expectedElements = Set.of(
                singleType,
                method,
                new HasMethodRelationship(singleType, method),
                localClass,
                runMethod,
                new HasMethodRelationship(localClass, runMethod),
                constructor,
                new HasConstructorRelationship(localClass, constructor),
                new InvokeRelationship(method, constructor)
        );

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(LocalClassTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
