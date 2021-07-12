package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = AccessRelationship.TYPE)
public class AccessRelationship {
    public static final String TYPE = "ACCESS";

    @Id
    @GeneratedValue
    private Long relationshipId;

    @StartNode
    @Nonnull
    private final Executable accessingExecutable;

    @EndNode
    @Nonnull
    private final Field field;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public AccessRelationship() {
        this.accessingExecutable = null;
        this.field = null;
    }

    public AccessRelationship(@Nonnull final Executable accessingExecutable, @Nonnull final Field field) {
        this.accessingExecutable = Objects.requireNonNull(accessingExecutable);
        this.field = Objects.requireNonNull(field);
    }

    @Nonnull
    public Executable getAccessingExecutable() {
        return accessingExecutable;
    }

    @Nonnull
    public Field getField() {
        return field;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accessingExecutable", accessingExecutable)
                .add("field", field)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessRelationship fieldRelationship = (AccessRelationship) o;
        return accessingExecutable.equals(fieldRelationship.accessingExecutable) &&
                field.equals(fieldRelationship.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessingExecutable, field);
    }
}
