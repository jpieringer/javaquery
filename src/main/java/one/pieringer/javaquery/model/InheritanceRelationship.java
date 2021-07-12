package one.pieringer.javaquery.model;

import org.neo4j.ogm.annotation.*;

import javax.annotation.Nonnull;
import java.util.Objects;

@RelationshipEntity(type = InheritanceRelationship.TYPE)
public class InheritanceRelationship {

    public static final String TYPE = "INHERITS";

    @Id
    @GeneratedValue
    private Long relationshipId;

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
        this.relationshipId = null;
        this.subType = null;
        this.superType = null;
    }

    public InheritanceRelationship(@Nonnull final Type subType, @Nonnull final Type superType) {
        this.subType = Objects.requireNonNull(subType);
        this.superType = Objects.requireNonNull(superType);
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
