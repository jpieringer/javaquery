import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import one.pieringer.javaquery.database.GraphPersistence;
import one.pieringer.javaquery.database.ResultSet;
import one.pieringer.javaquery.model.InheritanceRelationship;
import one.pieringer.javaquery.model.Type;

public class Neo4JTest {
    @Test
    public void test() {
        final Configuration configuration = new Configuration.Builder().uri("bolt://localhost:7687").build();
        final SessionFactory sessionFactory = new SessionFactory(configuration, "one.pieringer.javaquery.model");
        final GraphPersistence graphPersistence = new GraphPersistence(sessionFactory);

        try {
            graphPersistence.clear();

            final Type type = Type.createClass("a.Type", "Type");
            final Type type2 = Type.createClass("a.Type2", "Type2");
            final InheritanceRelationship inheritanceRelationship = new InheritanceRelationship(type, type2);
            graphPersistence.persist(Set.of(type, type2, inheritanceRelationship));

            final ResultSet result = graphPersistence.executeQuery("MATCH path = ()-[]->() WITH nodes(path) as n, relationships(path) AS r RETURN n, r");
            assertThat(result.getTypes()).containsExactlyInAnyOrder(type, type2);
            assertThat(result.getInheritanceRelationships()).containsExactlyInAnyOrder(inheritanceRelationship);

            final Type newType = Type.createClass("a.Type", "Type");
            final Type type3 = Type.createClass("a.Type3", "Type3");
            final InheritanceRelationship newInheritanceRelationship = new InheritanceRelationship(newType, type3);
            final InheritanceRelationship newInheritanceRelationship2 = new InheritanceRelationship(type, type2);
            graphPersistence.persist(Set.of(newType, type2, newInheritanceRelationship, newInheritanceRelationship2));

            final ResultSet result2 = graphPersistence.executeQuery("MATCH path = ()-[]->() WITH nodes(path) as n, relationships(path) AS r RETURN n, r");
            assertThat(result2.getTypes()).containsExactlyInAnyOrder(type, type2, type3);
            assertThat(result2.getInheritanceRelationships()).containsExactlyInAnyOrder(newInheritanceRelationship2, newInheritanceRelationship);

        } finally {
            sessionFactory.close();
        }
    }
}
