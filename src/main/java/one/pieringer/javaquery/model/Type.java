package one.pieringer.javaquery.model;

import com.google.common.base.MoreObjects;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

import javax.annotation.Nonnull;
import java.util.Objects;

@NodeEntity
public class Type {

    public static final String FULLY_QUALIFIED_NAME = "fullyQualifiedName";
    public static final String NAME = "name";
    public static final String IS_CLASS = "isClass";
    public static final String IS_ENUM = "isEnum";
    public static final String IS_INTERFACE = "isInterface";
    public static final String IS_PRIMITIVE = "isPrimitive";
    public static final String IS_ABSTRACT = "isAbstract";

    public static Type createClass(@Nonnull final String fullyQualifiedName, @Nonnull final String name) {
        return new Type(fullyQualifiedName, name, true, false, false, false, false);
    }

    public static Type createAbstractClass(@Nonnull final String fullyQualifiedName, @Nonnull final String name) {
        return new Type(fullyQualifiedName, name, true, false, false, false, true);
    }

    public static Type createEnum(@Nonnull final String fullyQualifiedName, @Nonnull final String name) {
        return new Type(fullyQualifiedName, name, false, true, false, false, false);
    }

    public static Type createInterface(@Nonnull final String fullyQualifiedName, @Nonnull final String name) {
        return new Type(fullyQualifiedName, name, false, false, true, false, false);
    }

    public static Type createPrimitive(@Nonnull final String fullyQualifiedName, @Nonnull final String name) {
        return new Type(fullyQualifiedName, name, false, false, false, true, false);
    }

    @Id
    @Nonnull
    @Property(FULLY_QUALIFIED_NAME)
    private final String fullyQualifiedName;

    @Nonnull
    @Property(NAME)
    private final String name;

    @Property(IS_CLASS)
    private final boolean isClass;

    @Property(IS_ENUM)
    private final boolean isEnum;

    @Property(IS_INTERFACE)
    private final boolean isInterface;

    @Property(IS_PRIMITIVE)
    private final boolean isPrimitive;

    @Property(IS_ABSTRACT)
    private final boolean isAbstract;

    /**
     * This constructor is only used by Neo4J
     */
    @Deprecated
    public Type() {
        fullyQualifiedName = "not-initialized";
        name = "not-initialized";
        isClass = false;
        isEnum = false;
        isInterface = false;
        isPrimitive = false;
        isAbstract = false;
    }

    public Type(@Nonnull final String fullyQualifiedName, @Nonnull final String name, boolean isClass, boolean isEnum, boolean isInterface, boolean isPrimitive, boolean isAbstract) {
        this.fullyQualifiedName = Objects.requireNonNull(fullyQualifiedName);
        this.name = Objects.requireNonNull(name);
        this.isClass = isClass;
        this.isEnum = isEnum;
        this.isInterface = isInterface;
        this.isPrimitive = isPrimitive;
        this.isAbstract = isAbstract;
    }

    @Nonnull
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public boolean isClass() {
        return isClass;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fullyQualifiedName", fullyQualifiedName)
                .add("name", name)
                .add("isClass", isClass)
                .add("isEnum", isEnum)
                .add("isInterface", isInterface)
                .add("isPrimitive", isPrimitive)
                .add("isAbstract", isAbstract)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type aType = (Type) o;
        return fullyQualifiedName.equals(aType.fullyQualifiedName) &&
                name.equals(aType.name) &&
                isClass == aType.isClass &&
                isEnum == aType.isEnum &&
                isInterface == aType.isInterface &&
                isPrimitive == aType.isPrimitive &&
                isAbstract == aType.isAbstract;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName, name, isClass, isEnum, isInterface, isPrimitive, isAbstract);
    }
}
