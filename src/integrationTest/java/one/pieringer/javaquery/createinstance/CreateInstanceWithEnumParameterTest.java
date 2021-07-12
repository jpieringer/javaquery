package one.pieringer.javaquery.createinstance;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.*;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateInstanceWithEnumParameterTest {
    @Test
    void verifyCreateInstanceIsDetected() throws URISyntaxException {
        final Type typeOfNewInstance = new Type("pkg.TypeOfNewInstance", "TypeOfNewInstance");
        final Type enumType = new Type("pkg.EnumType", "EnumType");
        final Constructor typeOfNewInstanceConstructor = new Constructor("pkg.TypeOfNewInstance.<init>(pkg.EnumType)", "<init>(EnumType)");
        final HasConstructorRelationship typeOfNewInstanceHasConstructorRelationship = new HasConstructorRelationship(typeOfNewInstance, typeOfNewInstanceConstructor);

        final Type typeWithNewInstance = new Type("pkg.TypeWithNewInstance", "TypeWithNewInstance");
        final Method method = new Method("pkg.TypeWithNewInstance.method()", "method()");
        final HasMethodRelationship hasMethodRelationship = new HasMethodRelationship(typeWithNewInstance, method);
        final CreateInstanceRelationship createInstanceRelationship = new CreateInstanceRelationship(method, typeOfNewInstanceConstructor);
        final Set<Object> expectedElements = Set.of(typeOfNewInstance, enumType, typeOfNewInstanceConstructor, typeOfNewInstanceHasConstructorRelationship, typeWithNewInstance, method, hasMethodRelationship, createInstanceRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(CreateInstanceWithEnumParameterTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
