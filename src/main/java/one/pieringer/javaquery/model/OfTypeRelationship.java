package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = OfTypeRelationship.TYPE)
public class OfTypeRelationship {
    public static final String TYPE = "OF_TYPE";

    @Id
    @GeneratedValue
    private Long relationshipId;

    @StartNode
    @Nonnull
    private final Field field;

    @EndNode
    @Nonnull
    private final Type fieldType;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public OfTypeRelationship() {
        this.field = null;
        this.fieldType = null;
    }

    public OfTypeRelationship(@Nonnull final Field field, @Nonnull final Type fieldType) {
        this.fieldType = Objects.requireNonNull(fieldType);
        this.field = Objects.requireNonNull(field);
    }

    @Nonnull
    public Field getField() {
        return field;
    }

    @Nonnull
    public Type getFieldType() {
        return fieldType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("field", fieldType)
                .add("declaringType", field)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OfTypeRelationship fieldRelationship = (OfTypeRelationship) o;
        return field.equals(fieldRelationship.field) &&
                fieldType.equals(fieldRelationship.fieldType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, fieldType);
    }
}
