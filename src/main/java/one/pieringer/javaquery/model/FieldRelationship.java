package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = FieldRelationship.TYPE)
public class FieldRelationship {

    public static final String TYPE = "HAS_FIELD";

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
    private final Type fieldType;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public FieldRelationship() {
        this.containingType = null;
        this.fieldType = null;
        this.fieldName = "not initialized";
    }

    public FieldRelationship(@Nonnull final Type containingType, @Nonnull final String fieldName, @Nonnull final Type fieldType) {
        this.containingType = Objects.requireNonNull(containingType);
        this.fieldName = Objects.requireNonNull(fieldName);
        this.fieldType = Objects.requireNonNull(fieldType);
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
    public Type getFieldType() {
        return fieldType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("containingType", containingType)
                .add("fieldName", fieldName)
                .add("fieldType", fieldType)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldRelationship fieldRelationship = (FieldRelationship) o;
        return containingType.equals(fieldRelationship.containingType) &&
                fieldName.equals(fieldRelationship.fieldName) &&
                fieldType.equals(fieldRelationship.fieldType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containingType, fieldName, fieldType);
    }
}
