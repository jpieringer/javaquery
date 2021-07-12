package one.pieringer.javaquery.createinstance;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.Constructor;
import one.pieringer.javaquery.model.CreateInstanceRelationship;
import one.pieringer.javaquery.model.HasConstructorRelationship;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateInstanceInConstructorTest {
    @Test
    void verifyCreateInstanceIsDetected() throws URISyntaxException {
        final Type typeOfNewInstance = new Type("pkg.TypeOfNewInstance", "TypeOfNewInstance");
        final Constructor typeOfNewInstanceConstructor = new Constructor("pkg.TypeOfNewInstance.<init>()", "<init>()");
        final HasConstructorRelationship typeOfNewInstanceHasConstructorRelationship = new HasConstructorRelationship(typeOfNewInstance, typeOfNewInstanceConstructor);

        final Type typeWithNewInstance = new Type("pkg.TypeWithNewInstance", "TypeWithNewInstance");
        final Constructor typeWithNewInstanceConstructor = new Constructor("pkg.TypeWithNewInstance.<init>()", "<init>()");
        final HasConstructorRelationship typeWithNewInstanceHasConstructorRelationship = new HasConstructorRelationship(typeWithNewInstance, typeWithNewInstanceConstructor);

        final CreateInstanceRelationship createInstanceRelationship = new CreateInstanceRelationship(typeWithNewInstanceConstructor, typeOfNewInstanceConstructor);
        final Set<Object> expectedElements = Set.of(typeOfNewInstance, typeOfNewInstanceConstructor, typeOfNewInstanceHasConstructorRelationship, typeWithNewInstance, typeWithNewInstanceConstructor, typeWithNewInstanceHasConstructorRelationship, createInstanceRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(CreateInstanceInConstructorTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
