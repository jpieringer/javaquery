package one.pieringer.javaquery.inheritance;

import one.pieringer.javaquery.AnalyzerTestRunner;
import one.pieringer.javaquery.model.InheritanceRelationship;
import one.pieringer.javaquery.model.Type;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeImplementsInterfaceTest {
    @Test
    void verifyInheritanceIsDetected() throws URISyntaxException {
        final Type superType = new Type("pkg.SuperInterface", "SuperInterface");
        final Type subType = new Type("pkg.SubType", "SubType");
        final InheritanceRelationship inheritanceRelationship = new InheritanceRelationship(subType, superType);
        final Set<Object> expectedElements = Set.of(superType, subType, inheritanceRelationship);

        final Set<Object> actualElements = AnalyzerTestRunner.analyzeClassesOfTest(TypeImplementsInterfaceTest.class);

        assertThat(actualElements).containsExactlyInAnyOrderElementsOf(expectedElements);
    }
}
