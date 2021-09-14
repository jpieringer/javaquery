package one.pieringer.javaquery.invoke;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InvokeConstructorWithPrimitiveParameterTest {
    @Test
    void verifyInvokeConstructorIsDetected() throws URISyntaxException {
        final Type typeOfNewInstance = Type.createClass("pkg.TypeOfNewInstance", "TypeOfNewInstance");
        final Constructor typeOfNewInstanceConstructor = new Constructor("pkg.TypeOfNewInstance.<init>(int)", "<init>(int)");
        final HasConstructorRelationship typeOfNewInstanceHasConstructorRelationship = new HasConstructorRelationship(typeOfNewInstance, typeOfNewInstanceConstructor);

        final Type typeWithNewInstance = Type.createClass("pkg.TypeWithNewInstance", "TypeWithNewInstance");
        final Method method = new Method("pkg.TypeWithNewInstance.method()", "method()");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(typeWithNewInstance, method);
        final InvokeRelationship invokeRelationship = new InvokeRelationship(method, typeOfNewInstanceConstructor);
        final Set<Object> expectedElements = Set.of(typeOfNewInstance, typeOfNewInstanceConstructor, typeOfNewInstanceHasConstructorRelationship, typeWithNewInstance, method, hasMethodRelationship, invokeRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(InvokeConstructorWithPrimitiveParameterTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
