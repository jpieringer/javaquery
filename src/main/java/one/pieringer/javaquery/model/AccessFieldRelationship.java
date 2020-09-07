package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = AccessFieldRelationship.TYPE)
public class AccessFieldRelationship {
    public static final String TYPE = "ACCESS_FIELD";

    @Id
    @GeneratedValue
    private Long relationshipId;

    @StartNode
    @Nonnull
    private final Type containingType;

    @Nonnull
    private final String fieldName;

    @EndNode
    @Nonnull
    private final Type fieldDeclaringType;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public AccessFieldRelationship() {
        this.containingType = null;
        this.fieldDeclaringType = null;
        this.fieldName = "not initialized";
    }

    public AccessFieldRelationship(@Nonnull final Type containingType, @Nonnull final String fieldName, @Nonnull final Type fieldDeclaringType) {
        this.containingType = Objects.requireNonNull(containingType);
        this.fieldName = Objects.requireNonNull(fieldName);
        this.fieldDeclaringType = Objects.requireNonNull(fieldDeclaringType);
    }

    @Nonnull
    public Type getContainingType() {
        return containingType;
    }

    @Nonnull
    public String getFieldName() {
        return fieldName;
    }

    @Nonnull
    public Type getFieldDeclaringType() {
        return fieldDeclaringType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("containingType", containingType)
                .add("fieldName", fieldName)
                .add("fieldDeclaringType", fieldDeclaringType)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessFieldRelationship fieldRelationship = (AccessFieldRelationship) o;
        return containingType.equals(fieldRelationship.containingType) &&
                fieldName.equals(fieldRelationship.fieldName) &&
                fieldDeclaringType.equals(fieldRelationship.fieldDeclaringType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containingType, fieldName, fieldDeclaringType);
    }
}
