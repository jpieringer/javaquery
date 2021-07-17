package one.pieringer.javaquery;

import one.pieringer.javaquery.analyzer.ElementNames;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FullyQualifiedNameUtils {
    @Nonnull
    public static ElementNames getAnonymousClassName(@Nonnull final ElementNames containingType, final int anonymousTypeDeclarationsCounter, @Nonnull final String name) {
        Objects.requireNonNull(containingType);
        Objects.requireNonNull(name);

        final String fullyQualifiedName = containingType.fullyQualified() + "$" + anonymousTypeDeclarationsCounter + name;
        final String simpleName = containingType.simple() + "$" + anonymousTypeDeclarationsCounter + name;

        return new ElementNames(fullyQualifiedName, simpleName);
    }

    /**
     * Get the simple and fully qualified constructor name from its type name and parameters.
     */
    @Nonnull
    public static ElementNames getConstructorName(@Nonnull final ElementNames type, @Nonnull final List<ElementNames> parameterTypes) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(parameterTypes);

        final String fullyQualifiedName = type.fullyQualified() + ".<init>(" + parameterTypes.stream().map(ElementNames::fullyQualified).collect(Collectors.joining(", ")) + ")";
        final String simpleName = "<init>(" + parameterTypes.stream().map(ElementNames::simple).collect(Collectors.joining(", ")) + ")";

        return new ElementNames(fullyQualifiedName, simpleName);
    }

    @Nonnull
    public static String getFullyQualifiedHasConstructorRelationshipName(@Nonnull final ElementNames type, @Nonnull final ElementNames constructor) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(constructor);
        return type.fullyQualified() + " -[:HAS_CONSTRUCTOR]-> " + constructor.fullyQualified();
    }

    @Nonnull
    public static ElementNames getMethodName(@Nonnull final ElementNames type, @Nonnull final String methodName, @Nonnull final List<ElementNames> parameterTypes) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(parameterTypes);

        final String fullyQualifiedName = type.fullyQualified() + "." + methodName + "(" + parameterTypes.stream().map(ElementNames::fullyQualified).collect(Collectors.joining(", ")) + ")";
        final String simpleName = methodName + "(" + parameterTypes.stream().map(ElementNames::simple).collect(Collectors.joining(", ")) + ")";

        return new ElementNames(fullyQualifiedName, simpleName);
    }

    @Nonnull
    public static String getFullyQualifiedHasMethodRelationshipName(@Nonnull final ElementNames type, @Nonnull final ElementNames method) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(method);
        return type.fullyQualified() + " -[:HAS_METHOD]-> " + method.fullyQualified();
    }

    @Nonnull
    public static String getFullyQualifiedInheritsRelationshipName(@Nonnull final ElementNames subType, @Nonnull final ElementNames superType) {
        Objects.requireNonNull(subType);
        Objects.requireNonNull(superType);
        return subType.fullyQualified() + " -[:INHERITS]-> " + superType.fullyQualified();
    }

    @Nonnull
    public static ElementNames getFieldName(@Nonnull final ElementNames containingType, @Nonnull final String fieldName) {
        Objects.requireNonNull(containingType);
        Objects.requireNonNull(fieldName);
        return new ElementNames(containingType.fullyQualified() + "." + fieldName, fieldName);
    }

    @Nonnull
    public static String getFullyQualifiedHasFieldRelationshipName(@Nonnull final ElementNames type, @Nonnull final ElementNames field) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(field);
        return type.fullyQualified() + " -[:HAS_FIELD]-> " + field.fullyQualified();
    }

    @Nonnull
    public static String getFullyQualifiedOfTypeRelationshipName(@Nonnull final ElementNames field, @Nonnull final ElementNames type) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(type);
        return field.fullyQualified() + " -[:OF_TYPE]-> " + type.fullyQualified();
    }

    @Nonnull
    public static String getFullyQualifiedCreateInstanceRelationshipName(@Nonnull final ElementNames invokingExecutable, @Nonnull final ElementNames createdType) {
        Objects.requireNonNull(invokingExecutable);
        Objects.requireNonNull(createdType);
        return invokingExecutable.fullyQualified() + " -[:CREATE_INSTANCE]-> " + createdType.fullyQualified();
    }

    @Nonnull
    public static String getFullyQualifiedAccessRelationshipName(@Nonnull final ElementNames invokingExecutable, @Nonnull final ElementNames field) {
        Objects.requireNonNull(invokingExecutable);
        Objects.requireNonNull(field);
        return invokingExecutable.fullyQualified() + " -[:ACCESS]-> " + field.fullyQualified();
    }

    @Nonnull
    public static String getFullyQualifiedInvokeRelationshipName(@Nonnull final ElementNames invokingExecutable, @Nonnull final ElementNames invokedExecutable) {
        Objects.requireNonNull(invokingExecutable);
        Objects.requireNonNull(invokedExecutable);
        return invokingExecutable.fullyQualified() + " -[:INVOKE]-> " + invokedExecutable.fullyQualified();
    }


}
