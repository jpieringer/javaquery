package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

import javax.annotation.Nonnull;
import java.util.Objects;

@NodeEntity
public class Field {

    public static final String FULLY_QUALIFIED_NAME = "fullyQualifiedName";
    public static final String NAME = "name";

    @Id
    @Nonnull
    @Property(FULLY_QUALIFIED_NAME)
    private final String fullyQualifiedName;

    @Nonnull
    @Property(NAME)
    private final String name;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    public Field() {
        fullyQualifiedName = "not-initialized";
        name = "not-initialized";
    }

    public Field(@Nonnull final String fullyQualifiedName, @Nonnull final String name) {
        this.fullyQualifiedName = Objects.requireNonNull(fullyQualifiedName);
        this.name = Objects.requireNonNull(name);
    }

    @Nonnull
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fullyQualifiedName", fullyQualifiedName)
                .add("name", name)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field aField = (Field) o;
        return fullyQualifiedName.equals(aField.fullyQualifiedName)
                && name.equals(aField.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName, name);
    }
}
