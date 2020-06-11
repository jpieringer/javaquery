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
    final Set<FieldRelationship> fieldRelationships;
    @Nonnull
    final Set<InheritanceRelationship> inheritanceRelationships;
    @Nonnull
    final Set<InvokeRelationship> invokeRelationships;
    @Nonnull
    final Set<Type> types;

    public ResultSet(@Nonnull final Set<CreateInstanceRelationship> createInstanceRelationships,
                     @Nonnull final Set<FieldRelationship> fieldRelationships,
                     @Nonnull final Set<InheritanceRelationship> inheritanceRelationships,
                     @Nonnull final Set<InvokeRelationship> invokeRelationships,
                     @Nonnull final Set<Type> types) {
        this.createInstanceRelationships = Objects.requireNonNull(createInstanceRelationships);
        this.fieldRelationships = Objects.requireNonNull(fieldRelationships);
        this.inheritanceRelationships = Objects.requireNonNull(inheritanceRelationships);
        this.invokeRelationships = Objects.requireNonNull(invokeRelationships);
        this.types = Objects.requireNonNull(types);
    }

    @Nonnull
    public Set<CreateInstanceRelationship> getCreateInstanceRelationships() {
        return createInstanceRelationships;
    }

    @Nonnull
    public Set<FieldRelationship> getFieldRelationships() {
        return fieldRelationships;
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
    public Set<Type> getTypes() {
        return types;
    }

    public static class ResultSetBuilder {
        @Nonnull
        private final Set<CreateInstanceRelationship> createInstanceRelationships = new HashSet<>();
        @Nonnull
        private final Set<FieldRelationship> fieldRelationships = new HashSet<>();
        @Nonnull
        private final Set<InheritanceRelationship> inheritanceRelationships = new HashSet<>();
        @Nonnull
        private final Set<InvokeRelationship> invokeRelationships = new HashSet<>();
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
        public ResultSetBuilder addCreateInstanceRelationships(@Nonnull final Set<CreateInstanceRelationship> createInstanceRelationships) {
            this.createInstanceRelationships.addAll(createInstanceRelationships);
            return this;
        }

        @Nonnull
        public ResultSetBuilder addFieldRelationship(@Nonnull final FieldRelationship fieldRelationship) {
            fieldRelationships.add(Objects.requireNonNull(fieldRelationship));
            return this;
        }


        @Nonnull
        public ResultSetBuilder addFieldRelationships(@Nonnull final Set<FieldRelationship> fieldRelationships) {
            this.fieldRelationships.addAll(Objects.requireNonNull(fieldRelationships));
            return this;
        }

        @Nonnull
        public ResultSetBuilder addInheritanceRelationship(@Nonnull final InheritanceRelationship inheritanceRelationship) {
            inheritanceRelationships.add(Objects.requireNonNull(inheritanceRelationship));
            return this;
        }

        @Nonnull
        public ResultSetBuilder addInheritanceRelationships(@Nonnull Set<InheritanceRelationship> inheritanceRelationships) {
            this.inheritanceRelationships.addAll(Objects.requireNonNull(inheritanceRelationships));
            return this;
        }

        @Nonnull
        public ResultSetBuilder addInvokeRelationship(@Nonnull final InvokeRelationship invokeRelationship) {
            invokeRelationships.add(Objects.requireNonNull(invokeRelationship));
            return this;
        }

        @Nonnull
        public ResultSetBuilder addInvokeRelationships(@Nonnull Set<InvokeRelationship> invokeRelationships) {
            this.invokeRelationships.addAll(Objects.requireNonNull(invokeRelationships));
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
            return new ResultSet(createInstanceRelationships, fieldRelationships, inheritanceRelationships, invokeRelationships, types);
        }
    }
}
