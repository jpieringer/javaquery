package one.pieringer.javaquery.model;

import one.pieringer.javaquery.FullyQualifiedNameUtils;
import one.pieringer.javaquery.analyzer.ElementNames;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class GraphBuilder {

    @Nonnull
    private final HashMap<String, Object> graph = new HashMap<>();

    public void addType(@Nonnull final ElementNames type) {
        Objects.requireNonNull(type);
        createObjectIfMissing(type.fullyQualified(), () -> new Type(type.fullyQualified(), type.simple()));
    }

    public void addInheritsRelationship(@Nonnull final ElementNames subType, @Nonnull final ElementNames superType) {
        Objects.requireNonNull(subType);
        Objects.requireNonNull(superType);
        final String fullyQualifiedHasConstructorRelationshipName =
                FullyQualifiedNameUtils.getFullyQualifiedInheritsRelationshipName(subType, superType);
        createObjectIfMissing(fullyQualifiedHasConstructorRelationshipName,
                () -> new InheritanceRelationship(getElement(subType.fullyQualified()), getElement(superType.fullyQualified())));
    }

    public void addConstructor(@Nonnull final ElementNames constructor) {
        Objects.requireNonNull(constructor);
        createObjectIfMissing(constructor.fullyQualified(), () -> new Constructor(constructor.fullyQualified(), constructor.simple()));
    }

    public void addHasConstructorRelationship(@Nonnull final ElementNames type, @Nonnull final ElementNames constructor) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(constructor);
        final String fullyQualifiedHasConstructorRelationshipName =
                FullyQualifiedNameUtils.getFullyQualifiedHasConstructorRelationshipName(type, constructor);
        createObjectIfMissing(fullyQualifiedHasConstructorRelationshipName,
                () -> new HasConstructorRelationship(getElement(type.fullyQualified()), getElement(constructor.fullyQualified())));
    }

    public void addMethod(@Nonnull final ElementNames method) {
        Objects.requireNonNull(method);
        createObjectIfMissing(method.fullyQualified(), () -> new Method(method.fullyQualified(), method.simple()));
    }

    public void addHasMethodRelationship(@Nonnull final ElementNames type, @Nonnull final ElementNames method) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(method);
        final String fullyQualifiedHasConstructorRelationshipName =
                FullyQualifiedNameUtils.getFullyQualifiedHasMethodRelationshipName(type, method);
        createObjectIfMissing(fullyQualifiedHasConstructorRelationshipName,
                () -> new HasMethodRelationship(getElement(type.fullyQualified()), getElement(method.fullyQualified())));
    }


    public void addField(@Nonnull final ElementNames field) {
        Objects.requireNonNull(field);
        createObjectIfMissing(field.fullyQualified(), () -> new Field(field.fullyQualified(), field.simple()));
    }

    public void addHasFieldRelationship(@Nonnull final ElementNames type, @Nonnull final ElementNames field) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(field);
        final String fullyQualifiedHasFieldRelationshipName =
                FullyQualifiedNameUtils.getFullyQualifiedHasFieldRelationshipName(type, field);
        createObjectIfMissing(fullyQualifiedHasFieldRelationshipName,
                () -> new HasFieldRelationship(getElement(type.fullyQualified()), getElement(field.fullyQualified())));
    }

    public void addOfTypeRelationship(@Nonnull final ElementNames field, @Nonnull final ElementNames type) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(type);
        final String fullyQualifiedOfTypeRelationshipName =
                FullyQualifiedNameUtils.getFullyQualifiedOfTypeRelationshipName(field, type);
        createObjectIfMissing(fullyQualifiedOfTypeRelationshipName,
                () -> new OfTypeRelationship(getElement(field.fullyQualified()), getElement(type.fullyQualified())));
    }

    public void addCreateInstanceRelationship(@Nonnull final ElementNames executable, @Nonnull final ElementNames constructor) {
        Objects.requireNonNull(executable);
        Objects.requireNonNull(constructor);
        final String fullyQualifiedOfTypeRelationshipName =
                FullyQualifiedNameUtils.getFullyQualifiedCreateInstanceRelationshipName(executable, constructor);
        createObjectIfMissing(fullyQualifiedOfTypeRelationshipName,
                () -> new CreateInstanceRelationship(getElement(executable.fullyQualified()), getElement(constructor.fullyQualified())));
    }

    public void addAccessRelationship(@Nonnull final ElementNames executable, @Nonnull final ElementNames field) {
        Objects.requireNonNull(executable);
        Objects.requireNonNull(field);
        final String fullyQualifiedOfTypeRelationshipName =
                FullyQualifiedNameUtils.getFullyQualifiedAccessRelationshipName(executable, field);
        createObjectIfMissing(fullyQualifiedOfTypeRelationshipName,
                () -> new AccessRelationship(getElement(executable.fullyQualified()), getElement(field.fullyQualified())));
    }

    public void addInvokeRelationship(@Nonnull final ElementNames invokingExecutable, @Nonnull final ElementNames invokedExecutable) {
        Objects.requireNonNull(invokingExecutable);
        Objects.requireNonNull(invokedExecutable);
        final String fullyQualifiedOfTypeRelationshipName =
                FullyQualifiedNameUtils.getFullyQualifiedInvokeRelationshipName(invokingExecutable, invokedExecutable);
        createObjectIfMissing(fullyQualifiedOfTypeRelationshipName,
                () -> new InvokeRelationship(getElement(invokingExecutable.fullyQualified()), getElement(invokedExecutable.fullyQualified())));
    }

    public Set<Object> getGraph() {
        return new HashSet<>(graph.values());
    }

    private <T> void createObjectIfMissing(@Nonnull final String fullyQualifiedName, @Nonnull final Supplier<T> newObjectSupplier) {
        if (!graph.containsKey(fullyQualifiedName)) {
            graph.put(fullyQualifiedName, newObjectSupplier.get());
        }
    }

    @Nonnull
    private <T> T getElement(@Nonnull final String fullyQualifiedName) {
        Objects.requireNonNull(fullyQualifiedName);
        if (!graph.containsKey(fullyQualifiedName)) {
            throw new NoSuchElementException("The element with the fully qualified name " + fullyQualifiedName + " was not found.");
        }

        return (T) graph.get(fullyQualifiedName);
    }
}
