package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = InvokeRelationship.TYPE)
public class InvokeRelationship {
    public static final String TYPE = "INVOKES";

    @Id
    @GeneratedValue
    private Long relationshipId;

    @StartNode
    @Nonnull
    private final Type containingType;

    @Nonnull
    private final String invokedMethod;

    @EndNode
    @Nonnull
    private final Type invokedType;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public InvokeRelationship() {
        this.containingType = null;
        this.invokedType = null;
        this.invokedMethod = "not initialized";
    }

    public InvokeRelationship(@Nonnull final Type containingType, @Nonnull final String invokedMethod, @Nonnull final Type invokedType) {
        this.containingType = Objects.requireNonNull(containingType);
        this.invokedMethod = Objects.requireNonNull(invokedMethod);
        this.invokedType = Objects.requireNonNull(invokedType);
    }

    @Nonnull
    public Type getContainingType() {
        return containingType;
    }

    @Nonnull
    public String getInvokedMethod() {
        return invokedMethod;
    }

    @Nonnull
    public Type getInvokedType() {
        return invokedType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("containingType", containingType)
                .add("invokedMethod", invokedMethod)
                .add("invokedType", invokedType)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeRelationship fieldRelationship = (InvokeRelationship) o;
        return containingType.equals(fieldRelationship.containingType) &&
                invokedMethod.equals(fieldRelationship.invokedMethod) &&
                invokedType.equals(fieldRelationship.invokedType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containingType, invokedMethod, invokedType);
    }
}
