package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = HasFieldRelationship.TYPE)
public class HasFieldRelationship {
    public static final String TYPE = "HAS_FIELD";

    @Id
    private final String relationshipId;

    @StartNode
    @Nonnull
    private final Type declaringType;

    @EndNode
    @Nonnull
    private final Field field;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public HasFieldRelationship() {
        this.declaringType = null;
        this.field = null;
        this.relationshipId = null;
    }

    public HasFieldRelationship(@Nonnull final Type declaringType, @Nonnull final Field field) {
        this.declaringType = Objects.requireNonNull(declaringType);
        this.field = Objects.requireNonNull(field);
        this.relationshipId = declaringType.getFullyQualifiedName() + "|" + field.getFullyQualifiedName();
    }

    @Nonnull
    public Type getDeclaringType() {
        return declaringType;
    }

    @Nonnull
    public Field getField() {
        return field;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("declaringType", declaringType)
                .add("field", field)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HasFieldRelationship fieldRelationship = (HasFieldRelationship) o;
        return declaringType.equals(fieldRelationship.declaringType) &&
                field.equals(fieldRelationship.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, field);
    }
}
