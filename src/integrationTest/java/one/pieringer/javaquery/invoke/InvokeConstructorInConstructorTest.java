package one.pieringer.javaquery.invoke;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Constructor;
import one.pieringer.javaquery.model.InvokeRelationship;
import one.pieringer.javaquery.model.HasConstructorRelationship;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InvokeConstructorInConstructorTest {
    @Test
    void verifyInvokeConstructorIsDetected() throws URISyntaxException {
        final Type typeOfNewInstance = new Type("pkg.TypeOfNewInstance", "TypeOfNewInstance");
        final Constructor typeOfNewInstanceConstructor = new Constructor("pkg.TypeOfNewInstance.<init>()", "<init>()");
        final HasConstructorRelationship typeOfNewInstanceHasConstructorRelationship = new HasConstructorRelationship(typeOfNewInstance, typeOfNewInstanceConstructor);

        final Type typeWithNewInstance = new Type("pkg.TypeWithNewInstance", "TypeWithNewInstance");
        final Constructor typeWithNewInstanceConstructor = new Constructor("pkg.TypeWithNewInstance.<init>()", "<init>()");
        final HasConstructorRelationship typeWithNewInstanceHasConstructorRelationship = new HasConstructorRelationship(typeWithNewInstance, typeWithNewInstanceConstructor);

        final InvokeRelationship invokeRelationship = new InvokeRelationship(typeWithNewInstanceConstructor, typeOfNewInstanceConstructor);
        final Set<Object> expectedElements = Set.of(typeOfNewInstance, typeOfNewInstanceConstructor, typeOfNewInstanceHasConstructorRelationship, typeWithNewInstance, typeWithNewInstanceConstructor, typeWithNewInstanceHasConstructorRelationship, invokeRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(InvokeConstructorInConstructorTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
