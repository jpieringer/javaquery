package one.pieringer.javaquery.database;

import one.pieringer.javaquery.model.*;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ResultSet {
    @Nonnull
    final Set<CreateInstanceRelationship> createInstanceRelationships;
    @Nonnull
    final Set<InheritanceRelationship> inheritanceRelationships;
    @Nonnull
    final Set<InvokeRelationship> invokeRelationships;
    @Nonnull
    final Set<AccessRelationship> accessRelationships;
    @Nonnull
    final Set<Type> types;

    public ResultSet(@Nonnull final Set<CreateInstanceRelationship> createInstanceRelationships,
                     @Nonnull final Set<InheritanceRelationship> inheritanceRelationships,
                     @Nonnull final Set<InvokeRelationship> invokeRelationships,
                     @Nonnull final Set<AccessRelationship> accessRelationships,
                     @Nonnull final Set<Type> types) {
        this.createInstanceRelationships = Objects.requireNonNull(createInstanceRelationships);
        this.inheritanceRelationships = Objects.requireNonNull(inheritanceRelationships);
        this.invokeRelationships = Objects.requireNonNull(invokeRelationships);
        this.accessRelationships = Objects.requireNonNull(accessRelationships);
        this.types = Objects.requireNonNull(types);
    }

    @Nonnull
    public Set<CreateInstanceRelationship> getCreateInstanceRelationships() {
        return createInstanceRelationships;
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
    public Set<AccessRelationship> getAccessFieldRelationships() {
        return accessRelationships;
    }

    @Nonnull
    public Set<Type> getTypes() {
        return types;
    }

    public static class ResultSetBuilder {
        @Nonnull
        private final Set<CreateInstanceRelationship> createInstanceRelationships = new HashSet<>();
        @Nonnull
        private final Set<InheritanceRelationship> inheritanceRelationships = new HashSet<>();
        @Nonnull
        private final Set<InvokeRelationship> invokeRelationships = new HashSet<>();
        @Nonnull
        private final Set<AccessRelationship> accessRelationships = new HashSet<>();
        @Nonnull
        private final Set<Type> types = new HashSet<>();

        public ResultSetBuilder() {
        }

        @Nonnull
        public ResultSetBuilder addCreateInstanceRelationship(@Nonnull final CreateInstanceRelationship createInstanceRelationship) {
            createInstanceRelationships.add(Objects.requireNonNull(createInstanceRelationship));
            return this;
        }

        @Nonnull
        public ResultSetBuilder addInheritanceRelationship(@Nonnull final InheritanceRelationship inheritanceRelationship) {
            inheritanceRelationships.add(Objects.requireNonNull(inheritanceRelationship));
            return this;
        }

        @Nonnull
        public ResultSetBuilder addInvokeRelationship(@Nonnull final InvokeRelationship invokeRelationship) {
            invokeRelationships.add(Objects.requireNonNull(invokeRelationship));
            return this;
        }

        @Nonnull
        public ResultSetBuilder addAccessFieldRelationship(@Nonnull final AccessRelationship accessRelationship) {
            this.accessRelationships.add(Objects.requireNonNull(accessRelationship));
            return this;
        }

        @Nonnull
        public ResultSetBuilder addType(@Nonnull final Type type) {
            types.add(Objects.requireNonNull(type));
            return this;
        }

        @Nonnull
        public ResultSetBuilder addTypes(@Nonnull Set<Type> types) {
            this.types.addAll(Objects.requireNonNull(types));
            return this;
        }

        public ResultSet build() {
            return new ResultSet(createInstanceRelationships, inheritanceRelationships, invokeRelationships, accessRelationships, types);
        }
    }
}
