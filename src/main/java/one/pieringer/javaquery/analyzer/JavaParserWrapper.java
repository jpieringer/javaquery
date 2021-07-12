package one.pieringer.javaquery.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import one.pieringer.javaquery.FullyQualifiedNameUtils;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JavaParserWrapper {

    @Nonnull
    public final JavaParser javaParser;

    public JavaParserWrapper(@Nonnull final JavaParser javaParser) {
        this.javaParser = Objects.requireNonNull(javaParser);
    }

    @Nonnull
    public CompilationUnit parseFile(@Nonnull final File javaFile) {
        Objects.requireNonNull(javaFile);

        try {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(javaFile);
            if (!parseResult.isSuccessful() || parseResult.getProblems().size() > 0) {
                throw new RuntimeException("Parsing of java file " + javaFile + " failed.");
            }
            if (parseResult.getResult().isEmpty()) {
                throw new IllegalArgumentException("Parsing of " + javaFile + " did not lead any result.");
            }

            return parseResult.getResult().get();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Did not find file " + javaFile, e);
        }
    }

    /**
     * Gets the type that should be used within the graph.
     * - Arrays are reduced to the type of the single element.
     * - Primitive types are ignored.
     */

    @CheckForNull
    public ElementNames getSimplifiedType(@Nonnull final Type type) {
        Objects.requireNonNull(type);

        if (type instanceof ClassOrInterfaceType) {
            final ResolvedReferenceType resolvedReferenceType = type.asClassOrInterfaceType().resolve();
            final ResolvedReferenceTypeDeclaration resolvedReferenceTypeDeclaration = resolvedReferenceType.getTypeDeclaration().orElseThrow();
            return new ElementNames(resolvedReferenceTypeDeclaration.getQualifiedName(), resolvedReferenceTypeDeclaration.getName());
        } else if (type instanceof ArrayType) {
            return getSimplifiedType(type.asArrayType().getElementType());
        } else if (type instanceof PrimitiveType) {
            return null; // Ignored as primitive types are not interesting in the graph.
        }

        throw new NotImplementedException("Unknown type " + type);
    }

    /**
     * Gets the type that should be used within the graph. E.g. Arrays are reduced to the type of the single element.
     */
    @Nonnull
    public ElementNames getSimplifiedType(@Nonnull final ResolvedType resolvedType) {
        Objects.requireNonNull(resolvedType);

        if (resolvedType.isReferenceType()) {
            final ResolvedReferenceType resolvedReferenceType = resolvedType.asReferenceType();
            return new ElementNames(resolvedReferenceType.getQualifiedName(), resolvedReferenceType.getTypeDeclaration().orElseThrow().getName());
        } else if (resolvedType.isPrimitive()) {
            return new ElementNames(resolvedType.asPrimitive().describe(), resolvedType.asPrimitive().describe());
        } else if (resolvedType.isArray()) {
            return getSimplifiedType(resolvedType.asArrayType().getComponentType());
        }

        throw new AssertionError("Type " + resolvedType + " is not implemented");
    }

    /**
     * Gets the type that should be used within the graph. E.g. Arrays are reduced to the type of the single element.
     */
    @Nonnull
    public ElementNames getExactType(@Nonnull final ResolvedType resolvedType) {
        Objects.requireNonNull(resolvedType);

        if (resolvedType.isReferenceType()) {
            final ResolvedReferenceType resolvedReferenceType = resolvedType.asReferenceType();
            return new ElementNames(resolvedReferenceType.getQualifiedName(), resolvedReferenceType.getTypeDeclaration().orElseThrow().getName());
        } else if (resolvedType.isPrimitive()) {
            return new ElementNames(resolvedType.asPrimitive().describe(), resolvedType.asPrimitive().describe());
        } else if (resolvedType.isArray()) {
            final ElementNames componentType = getExactType(resolvedType.asArrayType().getComponentType());
            return new ElementNames(componentType.fullyQualified() + "[]", componentType.simple() + "[]");
        }

        throw new AssertionError("Type " + resolvedType + " is not implemented");
    }

    @Nonnull
    public ElementNames getParentClassOrInterfaceType(@Nonnull final Node node) {
        Objects.requireNonNull(node);

        // TODO Is this really correct?
//            if (((ClassOrInterfaceDeclaration) node).isLocalClassDeclaration()) {
//                // A local class declaration does not by itself have a fully qualified name, use the one
//                // from the parent class.
//                return getParentClassOrInterfaceDeclaration(node.getParentNode().get());
//            }

        if (node instanceof ClassOrInterfaceDeclaration ||
                node instanceof EnumDeclaration ||
                node instanceof AnnotationDeclaration) {
            final TypeDeclaration<?> typeDeclaration = (TypeDeclaration<?>) node;
            return new ElementNames(typeDeclaration.getFullyQualifiedName().orElseThrow(), typeDeclaration.getNameAsString());
        }
        // TODO add RecordDeclaration

        if (node.getParentNode().isPresent()) {
            return getParentClassOrInterfaceType(node.getParentNode().get());
        }

        throw new AssertionError("No class or interface declaration parent found.");
    }


    @Nonnull
    public List<ElementNames> getParameterTypes(@Nonnull final NodeList<Parameter> parameters) {
        Objects.requireNonNull(parameters);

        return parameters.stream().map(p -> {
            final ResolvedParameterDeclaration resolvedParameterDeclaration = p.resolve();
            return getExactType(resolvedParameterDeclaration.getType());
        }).collect(Collectors.toList());
    }

    @Nonnull
    public CallableDeclaration<?> getParentMethodDeclaration(@Nonnull final Node node) {
        Objects.requireNonNull(node);

        // TODO Do  we need to check that we are in the most outer parent method? e.g. would we otherwise get a lambda back as method?

        if (node instanceof CallableDeclaration<?>) {
            return (CallableDeclaration<?>) node;
        }

        if (node.getParentNode().isPresent()) {
            return getParentMethodDeclaration(node.getParentNode().get());
        }

        throw new AssertionError("No callable (method or constructor) declaration parent found.");
    }

    @Nonnull
    public ElementNames getParentNamedCallable(@Nonnull final Node node) {
        final ElementNames containingType = getParentClassOrInterfaceType(node);

        final CallableDeclaration<?> callableDeclaration = getParentMethodDeclaration(node);
        final List<ElementNames> parameterTypeNames = getParameterTypes(callableDeclaration.getParameters());

        if (callableDeclaration instanceof MethodDeclaration) {
            return FullyQualifiedNameUtils.getMethodName(containingType, callableDeclaration.asMethodDeclaration().getNameAsString(), parameterTypeNames);
        } else if (callableDeclaration instanceof ConstructorDeclaration) {
            return FullyQualifiedNameUtils.getConstructorName(containingType, parameterTypeNames);
        }

        throw new AssertionError("Unknown sub class of CallableDeclaration");
    }
}
