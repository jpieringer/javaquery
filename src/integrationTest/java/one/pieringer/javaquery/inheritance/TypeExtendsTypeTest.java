package one.pieringer.javaquery.inheritance;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.InheritanceRelationship;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeExtendsTypeTest {
    @Test
    void verifyInheritanceIsDetected() throws URISyntaxException {
        final Type superType = Type.createClass("pkg.SuperType", "SuperType");
        final Type subType = Type.createClass("pkg.SubType", "SubType");
        final InheritanceRelationship inheritanceRelationship = new InheritanceRelationship(subType, superType);
        final Set<Object> expectedElements = Set.of(superType, subType, inheritanceRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(TypeExtendsTypeTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
