package one.pieringer.javaquery.database;

import one.pieringer.javaquery.model.*;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import javax.annotation.Nonnull;
import java.util.*;

public class GraphPersistence {

    @Nonnull
    private final SessionFactory sessionFactory;

    public GraphPersistence(@Nonnull final SessionFactory sessionFactory) {
        this.sessionFactory = Objects.requireNonNull(sessionFactory);
    }

    public <T> void persist(@Nonnull final Set<T> classes) {
        Objects.requireNonNull(classes);
        Session session = sessionFactory.openSession();
        final var transaction = session.beginTransaction();
        session.save(classes, 0);
        transaction.commit();
    }

    public void clear() {
        Session session = sessionFactory.openSession();
        session.query("MATCH ()-[r]->() DELETE r", new HashMap<>());
        session.query("MATCH (n) DELETE n", new HashMap<>());
    }

    public ResultSet executeQuery(@Nonnull final String query) {
        Objects.requireNonNull(query);

        Session session = sessionFactory.openSession();
        Result result = session.query(query, Collections.emptyMap());
        ResultSet.ResultSetBuilder resultSetBuilder = new ResultSet.ResultSetBuilder();
        for (Map<String, Object> entries : result) {
            fillResultSetBuilder(entries.values(), resultSetBuilder);
        }

        return resultSetBuilder.build();
    }

    private void fillResultSetBuilder(Collection<?> entries, ResultSet.ResultSetBuilder resultSetBuilder) {
        for (Object entry : entries) {
            fillResultSetBuilder(entry, resultSetBuilder);
        }
    }

    private void fillResultSetBuilder(Object entry, ResultSet.ResultSetBuilder resultSetBuilder) {
        if (entry instanceof HasFieldRelationship) {
            resultSetBuilder.addHasFieldRelationship((HasFieldRelationship) entry);
        } else if (entry instanceof OfTypeRelationship) {
            resultSetBuilder.addOfTypeRelationship((OfTypeRelationship) entry);
        } else if (entry instanceof InheritanceRelationship) {
            resultSetBuilder.addInheritanceRelationship((InheritanceRelationship) entry);
        } else if (entry instanceof InvokeRelationship) {
            resultSetBuilder.addInvokeRelationship((InvokeRelationship) entry);
        } else if (entry instanceof AccessRelationship) {
            resultSetBuilder.addAccessFieldRelationship((AccessRelationship) entry);
        } else if (entry instanceof HasMethodRelationship) {
            resultSetBuilder.addHasMethodRelationship((HasMethodRelationship) entry);
        } else if (entry instanceof HasConstructorRelationship) {
            resultSetBuilder.addHasConstructorRelationship((HasConstructorRelationship) entry);
        } else if (entry instanceof Type) {
            resultSetBuilder.addType((Type) entry);
        } else if (entry instanceof Collection) {
            fillResultSetBuilder((Collection<?>) entry, resultSetBuilder);
        }
    }
}
