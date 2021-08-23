package one.pieringer.javaquery.analyzer;

import one.pieringer.javaquery.FullyQualifiedNameUtils;
import org.eclipse.jdt.core.dom.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ASTUtils {
    @Nonnull
    public ElementNames getName(@Nonnull final ITypeBinding typeBinding, @Nonnull final HashMap<ITypeBinding, ElementNames> typeDeclarations) {
        Objects.requireNonNull(typeBinding);
        Objects.requireNonNull(typeDeclarations);

        if (typeBinding.isAnonymous() || typeBinding.isLocal()) {
            return typeDeclarations.get(typeBinding);
        }

        return new ElementNames(typeBinding.getQualifiedName(), typeBinding.getName());
    }

    @Nonnull
    public ElementNames getSimplifiedType(@Nonnull final ITypeBinding typeBinding, @Nonnull final HashMap<ITypeBinding, ElementNames> anonymousTypeDeclarations) {
        Objects.requireNonNull(typeBinding);
        Objects.requireNonNull(anonymousTypeDeclarations);

        if (typeBinding.isClass() || typeBinding.isInterface() || typeBinding.isEnum() || typeBinding.isPrimitive()) {
            return getName(typeBinding, anonymousTypeDeclarations);
        } else if (typeBinding.isArray()) {
            return getSimplifiedType(typeBinding.getComponentType(), anonymousTypeDeclarations);
        }

        throw new UnsupportedOperationException("Type " + typeBinding + " is not implemented");
    }

    @Nonnull
    public ElementNames getExactType(@Nonnull final ITypeBinding typeBinding, @Nonnull final HashMap<ITypeBinding, ElementNames> anonymousTypeDeclarations) {
        Objects.requireNonNull(typeBinding);
        Objects.requireNonNull(anonymousTypeDeclarations);

        if (typeBinding.isClass() || typeBinding.isInterface() || typeBinding.isEnum() || typeBinding.isPrimitive() || typeBinding.isTypeVariable()) {
            return getName(typeBinding, anonymousTypeDeclarations);
        } else if (typeBinding.isArray()) {
            final ElementNames componentType = getExactType(typeBinding.getComponentType(), anonymousTypeDeclarations);
            return new ElementNames(componentType.fullyQualified() + "[]", componentType.simple() + "[]");
        }

        throw new UnsupportedOperationException("Type " + typeBinding + " is not implemented");
    }

    /**
     * Gets the name of the passed {@link IMethodBinding}.
     */
    @Nonnull
    public ElementNames getMethodBindingName(@Nonnull final IMethodBinding methodBinding, @Nonnull final HashMap<ITypeBinding, ElementNames> anonymousTypeDeclarations) {
        Objects.requireNonNull(methodBinding);
        Objects.requireNonNull(anonymousTypeDeclarations);

        final ElementNames declaringType = getName(methodBinding.getDeclaringClass(), anonymousTypeDeclarations);
        final List<ElementNames> parameterTypes = Arrays.stream(methodBinding.getMethodDeclaration().getParameterTypes()).map(typeBinding -> getExactType(typeBinding, anonymousTypeDeclarations)).collect(Collectors.toList());

        if (methodBinding.isConstructor()) {
            return FullyQualifiedNameUtils.getConstructorName(declaringType, parameterTypes);
        } else {
            return FullyQualifiedNameUtils.getMethodName(declaringType, methodBinding.getName(), parameterTypes);
        }
    }

    @CheckForNull
    public ElementNames getParentTypeDeclaration(@Nonnull final ASTNode node, @Nonnull final HashMap<ITypeBinding, ElementNames> anonymousTypeDeclarations) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(anonymousTypeDeclarations);

        ASTNode parentNode = node;
        while ((parentNode = parentNode.getParent()) != null) {
            if (parentNode instanceof AbstractTypeDeclaration abstractTypeDeclaration) {
                final ITypeBinding typeBinding = abstractTypeDeclaration.resolveBinding();
                if (typeBinding == null) {
                    return null;
                }
                return getName(typeBinding, anonymousTypeDeclarations);
            } else if (parentNode instanceof AnonymousClassDeclaration anonymousClassDeclaration) {
                final ITypeBinding typeBinding = anonymousClassDeclaration.resolveBinding();
                if (typeBinding == null) {
                    return null;
                }
                return getName(typeBinding, anonymousTypeDeclarations);
            }
        }

        throw new AssertionError("No class or interface declaration parent found for: " + node);
    }

    /**
     * Get the parent callable / field declaration. One of the following types:
     * - {@link MethodDeclaration}
     * - {@link FieldDeclaration}
     * - {@link Initializer}
     */
    @Nonnull
    public ASTNode getEnclosingNode(@Nonnull final ASTNode node) {
        Objects.requireNonNull(node);

        ASTNode parentNode = node;
        while ((parentNode = parentNode.getParent()) != null) {
            // TODO Do we need to check that we are in the most outer parent method? e.g. would we otherwise get a lambda back as method?
            if (parentNode instanceof MethodDeclaration
                    || parentNode instanceof FieldDeclaration
                    || parentNode instanceof Initializer) {
                return parentNode;
            }
        }

        throw new AssertionError("No accountable parent node found for :" + node);
    }
}
