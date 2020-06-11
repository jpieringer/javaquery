package one.pieringer.javaquery.analyzer;

import one.pieringer.javaquery.model.Type;

import javax.annotation.Nonnull;
import java.util.*;

public class TypeSet {

    @Nonnull
    private final Set<Type> types = new HashSet<>();
    @Nonnull
    private final Map<String, Type> typeMap = new HashMap<>();

    @Nonnull
    public Set<Type> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    public Type addType(@Nonnull final Type type) {
        Objects.requireNonNull(type);

        if (typeMap.containsKey(type.getFullyQualifiedName())) {
            throw new IllegalArgumentException("Class with the name " + type.getFullyQualifiedName() + " exists already.");
        }

        this.types.add(type);
        this.typeMap.put(type.getFullyQualifiedName(), type);
        return type;
    }

    @Nonnull
    public Type getOrCreateType(final String fullyQualifiedName) {
        Objects.requireNonNull(fullyQualifiedName);

        Type type = this.typeMap.get(fullyQualifiedName);
        if (type != null) {
            return type;
        }

        return addType(new Type(fullyQualifiedName));
    }
}
