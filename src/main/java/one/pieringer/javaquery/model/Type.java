package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

import javax.annotation.Nonnull;
import java.util.Objects;

@NodeEntity
public class Type {

    public static final String NAME = "name";
    public static final String FULLY_QUALIFIED_NAME = "fullyQualifiedName";

    @Id
    @GeneratedValue
    private Long id;

    @Nonnull
    @Property(NAME)
    private final String name;

    @Nonnull
    @Property(FULLY_QUALIFIED_NAME)
    private final String fullyQualifiedName;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    public Type() {
        fullyQualifiedName = "not-initialized";
        name = "not-initialized";
    }

    public Type(@Nonnull final String fullyQualifiedName) {
        this.fullyQualifiedName = Objects.requireNonNull(fullyQualifiedName);
        this.name = StringUtils.substringAfterLast(StringUtils.substringBefore(fullyQualifiedName, "<"), ".");
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fullyQualifiedName", fullyQualifiedName)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type aType = (Type) o;
        return fullyQualifiedName.equals(aType.fullyQualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName);
    }
}
