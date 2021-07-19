package one.pieringer.javaquery.database;

import one.pieringer.javaquery.model.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ResultSet {
    @Nonnull
    private final Set<InheritanceRelationship> inheritanceRelationships;
    @Nonnull
    private final Set<InvokeRelationship> invokeRelationships;
    @Nonnull
    private final Set<AccessRelationship> accessRelationships;
    @Nonnull
    private final HashMap<Field, HasFieldRelationship> fieldToHasFieldRelationships;
    @Nonnull
    private final HashMap<Field, OfTypeRelationship> fieldToOfTypeRelationshipMap;
    @Nonnull
    private final HashMap<Method, HasMethodRelationship> methodToHasMethodRelationship;
    @Nonnull
    private final HashMap<Constructor, HasConstructorRelationship> constructorToHasConstructorRelationship;
    @Nonnull
    private final Set<Type> types;

    public ResultSet(@Nonnull final Set<InheritanceRelationship> inheritanceRelationships,
                     @Nonnull final Set<InvokeRelationship> invokeRelationships,
                     @Nonnull final Set<AccessRelationship> accessRelationships,
                     @Nonnull final HashMap<Field, HasFieldRelationship> fieldToHasFieldRelationships,
                     @Nonnull final HashMap<Field, OfTypeRelationship> fieldToOfTypeRelationshipMap,
                     @Nonnull final HashMap<Method, HasMethodRelationship> methodToHasMethodRelationship,
                     @Nonnull final HashMap<Constructor, HasConstructorRelationship> constructorToHasConstructorRelationship,
                     @Nonnull final Set<Type> types) {
        this.inheritanceRelationships = Objects.requireNonNull(inheritanceRelationships);
        this.invokeRelationships = Objects.requireNonNull(invokeRelationships);
        this.accessRelationships = Objects.requireNonNull(accessRelationships);
        this.fieldToHasFieldRelationships = Objects.requireNonNull(fieldToHasFieldRelationships);
        this.fieldToOfTypeRelationshipMap = Objects.requireNonNull(fieldToOfTypeRelationshipMap);
        this.methodToHasMethodRelationship = Objects.requireNonNull(methodToHasMethodRelationship);
        this.constructorToHasConstructorRelationship = Objects.requireNonNull(constructorToHasConstructorRelationship);
        this.types = Objects.requireNonNull(types);
    }

    @Nonnull
    public Set<InheritanceRelationship> getInheritanceRelationships() {
        return inheritanceRelationships;
    }

    @Nonnull
    public Set<InvokeRelationship> getInvokeRelationships() {
        return invokeRelationships;
    }

    @Nonnull
    public Set<AccessRelationship> getAccessRelationships() {
        return accessRelationships;
    }

    @Nonnull
    public Set<HasFieldRelationship> getHasFieldRelationships() {
        return new HashSet<>(fieldToHasFieldRelationships.values());
    }

    @CheckForNull
    public HasFieldRelationship getHasFieldRelationship(@Nonnull final Field field) {
        return fieldToHasFieldRelationships.get(Objects.requireNonNull(field));
    }

    @CheckForNull
    public OfTypeRelationship getOfTypeRelationship(@Nonnull final Field field) {
        return fieldToOfTypeRelationshipMap.get(Objects.requireNonNull(field));
    }

    @CheckForNull
    public HasMethodRelationship getHasMethodRelationship(@Nonnull final Method method) {
        return methodToHasMethodRelationship.get(Objects.requireNonNull(method));
    }

    @CheckForNull
    public HasConstructorRelationship getHasConstructorRelationship(@Nonnull final Constructor constructor) {
        return constructorToHasConstructorRelationship.get(Objects.requireNonNull(constructor));
    }

    @Nonnull
    public Set<Type> getTypes() {
        return types;
    }

    public static class ResultSetBuilder {
        @Nonnull
        private final Set<InheritanceRelationship> inheritanceRelationships = new HashSet<>();
        @Nonnull
        private final Set<InvokeRelationship> invokeRelationships = new HashSet<>();
        @Nonnull
        private final Set<AccessRelationship> accessRelationships = new HashSet<>();
        @Nonnull
        private final HashMap<Field, HasFieldRelationship> fieldToHasFieldRelationships = new HashMap<>();
        @Nonnull
        private final HashMap<Field, OfTypeRelationship> fieldToOfTypeRelationshipMap = new HashMap<>();
        @Nonnull
        private final HashMap<Method, HasMethodRelationship> methodToHasMethodRelationship = new HashMap<>();
        @Nonnull
        private final HashMap<Constructor, HasConstructorRelationship> constructorToHasConstructorRelationship = new HashMap<>();
        @Nonnull
        private final Set<Type> types = new HashSet<>();

        public ResultSetBuilder() {
        }

        public void addInheritanceRelationship(@Nonnull final InheritanceRelationship inheritanceRelationship) {
            inheritanceRelationships.add(Objects.requireNonNull(inheritanceRelationship));
        }

        public void addInvokeRelationship(@Nonnull final InvokeRelationship invokeRelationship) {
            invokeRelationships.add(Objects.requireNonNull(invokeRelationship));
        }

        public void addAccessFieldRelationship(@Nonnull final AccessRelationship accessRelationship) {
            this.accessRelationships.add(Objects.requireNonNull(accessRelationship));
        }

        public void addHasFieldRelationship(@Nonnull final HasFieldRelationship hasFieldRelationship) {
            Objects.requireNonNull(hasFieldRelationship);
            this.fieldToHasFieldRelationships.put(hasFieldRelationship.getField(), hasFieldRelationship);
        }

        public void addOfTypeRelationship(@Nonnull final OfTypeRelationship ofTypeRelationship) {
            Objects.requireNonNull(ofTypeRelationship);
            this.fieldToOfTypeRelationshipMap.put(ofTypeRelationship.getField(), ofTypeRelationship);
        }

        public void addHasMethodRelationship(@Nonnull final HasMethodRelationship hasMethodRelationship) {
            Objects.requireNonNull(hasMethodRelationship);
            this.methodToHasMethodRelationship.put(hasMethodRelationship.getMethod(), hasMethodRelationship);
        }

        public void addHasConstructorRelationship(@Nonnull final HasConstructorRelationship hasConstructorRelationship) {
            Objects.requireNonNull(hasConstructorRelationship);
            this.constructorToHasConstructorRelationship.put(hasConstructorRelationship.getConstructor(), hasConstructorRelationship);
        }

        public void addType(@Nonnull final Type type) {
            types.add(Objects.requireNonNull(type));
        }

        public ResultSet build() {
            return new ResultSet(inheritanceRelationships, invokeRelationships, accessRelationships,
                    fieldToHasFieldRelationships, fieldToOfTypeRelationshipMap, methodToHasMethodRelationship,
                    constructorToHasConstructorRelationship, types);
        }
    }
}
