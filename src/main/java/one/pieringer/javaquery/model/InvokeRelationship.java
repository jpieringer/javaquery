package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = InvokeRelationship.TYPE)
public class InvokeRelationship {
    public static final String TYPE = "INVOKE";

    @Id
    @GeneratedValue
    private Long relationshipId;

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
    }

    public InvokeRelationship(@Nonnull final Executable invokingExecutable, @Nonnull final Executable invokedExecutable) {
        this.invokingExecutable = Objects.requireNonNull(invokingExecutable);
        this.invokedExecutable = Objects.requireNonNull(invokedExecutable);
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
