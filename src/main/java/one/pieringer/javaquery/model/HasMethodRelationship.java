package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = HasMethodRelationship.TYPE)
public class HasMethodRelationship {
    public static final String TYPE = "HAS_METHOD";

    @Id
    private final String relationshipId;

    @StartNode
    @Nonnull
    private final Type declaringType;

    @EndNode
    @Nonnull
    private final Method method;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public HasMethodRelationship() {
        this.declaringType = null;
        this.method = null;
        this.relationshipId = null;
    }

    public HasMethodRelationship(@Nonnull final Type declaringType, @Nonnull final Method method) {
        this.declaringType = Objects.requireNonNull(declaringType);
        this.method = Objects.requireNonNull(method);
        this.relationshipId = declaringType.getFullyQualifiedName() + "|" + method.getFullyQualifiedName();
    }

    @Nonnull
    public Type getDeclaringType() {
        return declaringType;
    }

    @Nonnull
    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("declaringType", declaringType)
                .add("method", method)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HasMethodRelationship fieldRelationship = (HasMethodRelationship) o;
        return declaringType.equals(fieldRelationship.declaringType) &&
                method.equals(fieldRelationship.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, method);
    }
}
