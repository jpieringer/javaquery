package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = CreateInstanceRelationship.TYPE)
public class CreateInstanceRelationship {

    public static final String TYPE = "CREATE_INSTANCE";

    @Id
    @GeneratedValue
    private Long relationshipId;

    @StartNode
    @Nonnull
    private final Type containingType;

    @EndNode
    @Nonnull
    private final Type objectType;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public CreateInstanceRelationship() {
        this.containingType = null;
        this.objectType = null;
    }

    public CreateInstanceRelationship(@Nonnull Type containingType, @Nonnull Type objectType) {
        this.containingType = Objects.requireNonNull(containingType);
        this.objectType = Objects.requireNonNull(objectType);
    }

    @Nonnull
    public Type getContainingType() {
        return containingType;
    }

    @Nonnull
    public Type getObjectType() {
        return objectType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("containingType", containingType)
                .add("objectType", objectType)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateInstanceRelationship fieldRelationship = (CreateInstanceRelationship) o;
        return containingType.equals(fieldRelationship.containingType) &&
                objectType.equals(fieldRelationship.objectType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containingType, objectType);
    }
}
