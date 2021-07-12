package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

// TODO do we really need a separate relationship for this or could we use the regular INVOKE relationship?
@RelationshipEntity(type = CreateInstanceRelationship.TYPE)
public class CreateInstanceRelationship {

    public static final String TYPE = "CREATE_INSTANCE";

    @Id
    @GeneratedValue
    private Long relationshipId;

    @StartNode
    @Nonnull
    private final Executable invokingExecutable;

    @EndNode
    @Nonnull
    private final Constructor invokedConstructor;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public CreateInstanceRelationship() {
        this.invokingExecutable = null;
        this.invokedConstructor = null;
    }

    public CreateInstanceRelationship(@Nonnull Executable invokingExecutable, @Nonnull Constructor invokedConstructor) {
        this.invokingExecutable = Objects.requireNonNull(invokingExecutable);
        this.invokedConstructor = Objects.requireNonNull(invokedConstructor);
    }

    @Nonnull
    public Executable getInvokingExecutable() {
        return invokingExecutable;
    }

    @Nonnull
    public Constructor getInvokedConstructor() {
        return invokedConstructor;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("invokingExecutable", invokingExecutable)
                .add("invokedConstructor", invokedConstructor)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateInstanceRelationship fieldRelationship = (CreateInstanceRelationship) o;
        return invokingExecutable.equals(fieldRelationship.invokingExecutable) &&
                invokedConstructor.equals(fieldRelationship.invokedConstructor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invokingExecutable, invokedConstructor);
    }
}
