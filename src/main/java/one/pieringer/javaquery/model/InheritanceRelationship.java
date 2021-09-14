package one.pieringer.javaquery.model;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = InheritanceRelationship.TYPE)
public class InheritanceRelationship {

    public static final String TYPE = "INHERITS";

    @Id
    @Nonnull
    private final String relationshipId;

    @StartNode
    @Nonnull
    private final Type subType;

    @EndNode
    @Nonnull
    private final Type superType;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    @SuppressWarnings("ConstantConditions")
    public InheritanceRelationship() {
        this.subType = null;
        this.superType = null;
        this.relationshipId = null;
    }

    public InheritanceRelationship(@Nonnull final Type subType, @Nonnull final Type superType) {
        this.subType = Objects.requireNonNull(subType);
        this.superType = Objects.requireNonNull(superType);
        this.relationshipId = subType.getFullyQualifiedName() + "|" + superType.getFullyQualifiedName();
    }

    @Nonnull
    public Type getSubType() {
        return subType;
    }

    @Nonnull
    public Type getSuperType() {
        return superType;
    }

    @Override
    public String toString() {
        return "InheritanceRelationship{" +
                ", subType=" + subType +
                ", superType=" + superType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InheritanceRelationship that = (InheritanceRelationship) o;
        return subType.equals(that.subType) &&
                superType.equals(that.superType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subType, superType);
    }
}
