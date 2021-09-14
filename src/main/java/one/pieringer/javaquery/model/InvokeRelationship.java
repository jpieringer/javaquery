package one.pieringer.javaquery.model;

import javax.annotation.Nonnull;
import java.util.Objects;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import com.google.common.base.MoreObjects;

@RelationshipEntity(type = InvokeRelationship.TYPE)
public class InvokeRelationship {
    public static final String TYPE = "INVOKE";

    @Id
    @Nonnull
    private final String relationshipId;

    @StartNode
    @Nonnull
    private final Executable invokingExecutable;

    @EndNode
    @Nonnull
    private final Executable invokedExecutable;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public InvokeRelationship() {
        this.invokingExecutable = null;
        this.invokedExecutable = null;
        this.relationshipId = null;
    }

    public InvokeRelationship(@Nonnull final Executable invokingExecutable, @Nonnull final Executable invokedExecutable) {
        this.invokingExecutable = Objects.requireNonNull(invokingExecutable);
        this.invokedExecutable = Objects.requireNonNull(invokedExecutable);
        this.relationshipId = invokingExecutable.getFullyQualifiedName() + "|" + invokedExecutable.getFullyQualifiedName();
    }

    @Nonnull
    public Executable getInvokingExecutable() {
        return invokingExecutable;
    }

    @Nonnull
    public Executable getInvokedExecutable() {
        return invokedExecutable;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("containingType", invokingExecutable)
                .add("invokedType", invokedExecutable)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeRelationship fieldRelationship = (InvokeRelationship) o;
        return invokingExecutable.equals(fieldRelationship.invokingExecutable) &&
                invokedExecutable.equals(fieldRelationship.invokedExecutable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invokingExecutable, invokedExecutable);
    }
}
