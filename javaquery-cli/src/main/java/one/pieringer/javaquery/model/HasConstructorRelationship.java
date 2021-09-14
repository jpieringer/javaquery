package one.pieringer.javaquery.model;

import javax.annotation.Nonnull;
import java.util.Objects;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import com.google.common.base.MoreObjects;

@RelationshipEntity(type = HasConstructorRelationship.TYPE)
public class HasConstructorRelationship {
    public static final String TYPE = "HAS_CONSTRUCTOR";

    @Id
    @Nonnull
    private final String relationshipId;

    @StartNode
    @Nonnull
    private final Type declaringType;

    @EndNode
    @Nonnull
    private final Constructor constructor;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public HasConstructorRelationship() {
        this.declaringType = null;
        this.constructor = null;
        this.relationshipId = null;
    }

    public HasConstructorRelationship(@Nonnull final Type declaringType, @Nonnull final Constructor constructor) {
        this.declaringType = Objects.requireNonNull(declaringType);
        this.constructor = Objects.requireNonNull(constructor);
        this.relationshipId = declaringType.getFullyQualifiedName() + "|" + constructor.getFullyQualifiedName();
    }

    @Nonnull
    public Type getDeclaringType() {
        return declaringType;
    }

    @Nonnull
    public Constructor getConstructor() {
        return constructor;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("declaringType", declaringType)
                .add("constructor", constructor)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HasConstructorRelationship fieldRelationship = (HasConstructorRelationship) o;
        return declaringType.equals(fieldRelationship.declaringType) &&
                constructor.equals(fieldRelationship.constructor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, constructor);
    }
}
