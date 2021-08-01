package one.pieringer.javaquery.analyzer;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserAnonymousClassDeclaration;
import com.google.common.collect.Iterables;
import one.pieringer.javaquery.FullyQualifiedNameUtils;
import one.pieringer.javaquery.model.GraphBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

public class SourceCodeAnalyzer {

    private static final Logger LOG = LogManager.getLogger(SourceCodeAnalyzer.class);

    @Nonnull
    private final JavaParserWrapper javaParserWrapper;

    public SourceCodeAnalyzer(@Nonnull final JavaParserWrapper javaParserWrapper) {
        this.javaParserWrapper = Objects.requireNonNull(javaParserWrapper);
    }

    @Nonnull
    public Set<Object> analyze(@Nonnull final SourceCodeProvider sourceCodeProvider) {
        Objects.requireNonNull(sourceCodeProvider);

        final GraphBuilder graphBuilder = new GraphBuilder();
        final Visitor visitor = new Visitor(javaParserWrapper, graphBuilder);

        sourceCodeProvider.visitJavaFiles(javaFile -> {
            try {
                visitor.visit(javaParserWrapper.parseFile(javaFile), new JavaFileContext());
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to analyze file " + javaFile.getAbsolutePath(), e);
            } catch (AssertionError e) {
                throw new AssertionError("Failed to analyze file " + javaFile.getAbsolutePath(), e);
            }
        });

        return graphBuilder.getGraph();
    }

    private static class Visitor extends VoidVisitorAdapter<JavaFileContext> {
        @Nonnull
        private final JavaParserWrapper javaParserWrapper;
        @Nonnull
        private final GraphBuilder graphBuilder;

        public Visitor(@Nonnull final JavaParserWrapper javaParserWrapper, @Nonnull final GraphBuilder graphBuilder) {
            this.javaParserWrapper = Objects.requireNonNull(javaParserWrapper);
            this.graphBuilder = Objects.requireNonNull(graphBuilder);
        }

        @Override
        public void visit(ObjectCreationExpr expr, JavaFileContext context) {
            performVisit(expr, context);
            super.visit(expr, context);
        }

        private void performVisit(@Nonnull final ObjectCreationExpr expr, @Nonnull final JavaFileContext context) {
            Objects.requireNonNull(expr);
            Objects.requireNonNull(context);

            final ElementNames containingType = javaParserWrapper.getParentTypeDeclaration(expr, context.typeDeclarations);

            try {
                final ResolvedConstructorDeclaration resolvedConstructorDeclaration = expr.resolve();

                ElementNames createdType;
                if (resolvedConstructorDeclaration.declaringType() instanceof final JavaParserAnonymousClassDeclaration anonymousClassDeclaration) {
                    createdType = FullyQualifiedNameUtils.getAnonymousClassName(containingType, context.anonymousTypeDeclarationsCounter, anonymousClassDeclaration.getSuperTypeDeclaration().getName());
                    context.typeDeclarations.put(expr, createdType);
                    context.fieldInitializationStorageMap.put(createdType, new FieldInitializationStorage());
                    context.anonymousTypeDeclarationsCounter++;

                    final ElementNames superType = javaParserWrapper.getSimplifiedType(anonymousClassDeclaration.getSuperClass().orElseThrow());
                    graphBuilder.addType(createdType);
                    graphBuilder.addType(superType);
                    graphBuilder.addInheritsRelationship(createdType, superType);
                } else {
                    createdType = new ElementNames(resolvedConstructorDeclaration.declaringType().getQualifiedName(), resolvedConstructorDeclaration.declaringType().getName());
                    graphBuilder.addType(createdType);
                }

                final List<ElementNames> parameterTypes = new ArrayList<>(resolvedConstructorDeclaration.getNumberOfParams());
                for (int i = 0; i < resolvedConstructorDeclaration.getNumberOfParams(); ++i) {
                    parameterTypes.add(javaParserWrapper.getExactType(resolvedConstructorDeclaration.getParam(i).getType()));
                }
                final ElementNames constructor = FullyQualifiedNameUtils.getConstructorName(createdType, parameterTypes);

                graphBuilder.addConstructor(constructor);
                graphBuilder.addHasConstructorRelationship(createdType, constructor);

                final ElementNames parentNamedCallable = javaParserWrapper.getParentNamedCallable(expr, context.typeDeclarations);
                if (parentNamedCallable != null) {
                    graphBuilder.addInvokeRelationship(parentNamedCallable, constructor);
                    return;
                }

                if (javaParserWrapper.isInFieldDeclaration(expr) || javaParserWrapper.isInInitializer(expr)) {
                    context.fieldInitializationStorageMap.get(containingType).addInvokedConstructor(constructor);
                } else {
                    throw new AssertionError("No parent method and no parent field declaration found for: " + expr);
                }
            } catch (UnsolvedSymbolException e) {
                LOG.debug("Symbol resolving failed in {}. Ignoring object creation expression '{}'. Message: {}", containingType.fullyQualified(), expr, e.getMessage());
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException occurred during processing {}. Ignoring object creation expression '{}'. Message: {}" + containingType.fullyQualified(), expr, e.getMessage());
            } catch (RuntimeException e) {
                rethrowIfNoResolveException(containingType, e);
            }
        }

        @Override
        public void visit(FieldDeclaration n, JavaFileContext context) {
            performVisit(n, context);
            super.visit(n, context);
        }

        private void performVisit(@Nonnull final FieldDeclaration n, @Nonnull final JavaFileContext context) {
            Objects.requireNonNull(n);
            Objects.requireNonNull(context);

            final ElementNames containingType = javaParserWrapper.getParentTypeDeclaration(n, context.typeDeclarations);

            if (n.getVariables().getFirst().isEmpty()) {
                return;
            }

            try {
                final ElementNames fieldType = javaParserWrapper.getSimplifiedType(n.getVariables().getFirst().get().getType());
                if (fieldType == null) {
                    return;
                }

                graphBuilder.addType(fieldType);

                for (VariableDeclarator variableDeclarator : n.getVariables()) {
                    final ElementNames field = FullyQualifiedNameUtils.getFieldName(containingType, variableDeclarator.getNameAsString());
                    graphBuilder.addField(field);
                    graphBuilder.addHasFieldRelationship(containingType, field);
                    graphBuilder.addOfTypeRelationship(field, fieldType);
                }
            } catch (UnsolvedSymbolException e) {
                LOG.debug("Symbol resolving failed in {}. Ignoring field declaration '{}'. Message: {}", containingType.fullyQualified(), n, e.getMessage());
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException occurred during processing {}. Ignoring field declaration '{}'. Message: {}", containingType.fullyQualified(), n, e.getMessage());
            } catch (RuntimeException e) {
                rethrowIfNoResolveException(containingType, e);
            }
        }

        // TODO add RecordDeclaration

        @Override
        public void visit(ClassOrInterfaceDeclaration n, JavaFileContext context) {
            if (n.isLocalClassDeclaration()) {
                // TODO Support local class declarations
                return;
            }

            performVisit(n, context);

            super.visit(n, context);

            final ElementNames declaredType = context.typeDeclarations.get(n);
            addFieldInitializationCallsToTheConstructors(declaredType, context.fieldInitializationStorageMap.remove(declaredType));
        }

        private void performVisit(@Nonnull final ClassOrInterfaceDeclaration n, @Nonnull final JavaFileContext context) {
            Objects.requireNonNull(n);
            Objects.requireNonNull(context);

            final ElementNames declaredType = new ElementNames(n.getFullyQualifiedName().orElseThrow(), n.getNameAsString());
            context.typeDeclarations.put(n, declaredType);
            context.fieldInitializationStorageMap.put(declaredType, new FieldInitializationStorage());
            graphBuilder.addType(declaredType);

            for (ClassOrInterfaceType type : Iterables.concat(n.getExtendedTypes(), n.getImplementedTypes())) {
                try {
                    final ElementNames superType = javaParserWrapper.getSimplifiedType(type);
                    if (superType == null) {
                        return;
                    }

                    graphBuilder.addType(superType);
                    graphBuilder.addInheritsRelationship(declaredType, superType);
                } catch (UnsolvedSymbolException e) {
                    LOG.debug("Symbol resolving failed in {}. Ignoring class declaration {}. Message: {}", declaredType.fullyQualified(), declaredType.fullyQualified(), e.getMessage());
                } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                    LOG.debug("UnsupportedOperationException occurred during processing {}. Ignoring class declaration {}. Message: {}", declaredType.fullyQualified(), declaredType.fullyQualified(), e.getMessage());
                } catch (RuntimeException e) {
                    rethrowIfNoResolveException(declaredType, e);
                }
            }
        }

        /**
         * Field initialization is done before the constructors are called. Add it to all existing constructors and
         * create a default constructor if none is defined.
         */
        private void addFieldInitializationCallsToTheConstructors(@Nonnull final ElementNames declaredType, @Nonnull final FieldInitializationStorage fieldInitializationStorage) {
            Objects.requireNonNull(declaredType);
            Objects.requireNonNull(fieldInitializationStorage);

            if (!fieldInitializationStorage.hasFieldInitializationStored()) {
                return;
            }

            final ArrayList<ElementNames> constructors = new ArrayList<>(fieldInitializationStorage.getConstructors());

            if (constructors.isEmpty()) {
                final ElementNames defaultConstructor = FullyQualifiedNameUtils.getConstructorName(declaredType, Collections.emptyList());
                graphBuilder.addConstructor(defaultConstructor);
                graphBuilder.addHasConstructorRelationship(declaredType, defaultConstructor);
                constructors.add(defaultConstructor);
            }

            for (ElementNames constructor : constructors) {
                for (ElementNames accessedField : fieldInitializationStorage.getAccessedFields()) {
                    graphBuilder.addAccessRelationship(constructor, accessedField);
                }
                for (ElementNames invokedConstructor : fieldInitializationStorage.getInvokedConstructors()) {
                    graphBuilder.addInvokeRelationship(constructor, invokedConstructor);
                }
                for (ElementNames invokedMethod : fieldInitializationStorage.getInvokedMethods()) {
                    graphBuilder.addInvokeRelationship(constructor, invokedMethod);
                }
            }
        }

        @Override
        public void visit(EnumDeclaration n, JavaFileContext context) {
            performVisit(n, context);
            super.visit(n, context);
        }

        private void performVisit(@Nonnull final EnumDeclaration n, @Nonnull final JavaFileContext context) {
            Objects.requireNonNull(n);
            Objects.requireNonNull(context);

            final ElementNames declaredType = new ElementNames(n.getFullyQualifiedName().orElseThrow(), n.getNameAsString());
            context.typeDeclarations.put(n, declaredType);
            context.fieldInitializationStorageMap.put(declaredType, new FieldInitializationStorage());
            graphBuilder.addType(declaredType);
        }

        @Override
        public void visit(MethodCallExpr n, JavaFileContext context) {
            performVisit(n, context);
            super.visit(n, context);
        }

        private void performVisit(@Nonnull final MethodCallExpr n, @Nonnull final JavaFileContext context) {
            Objects.requireNonNull(n);
            Objects.requireNonNull(context);

            final ElementNames containingType = javaParserWrapper.getParentTypeDeclaration(n, context.typeDeclarations);

            try {
                final ResolvedMethodDeclaration resolvedMethodDeclaration = n.resolve();
                final List<ElementNames> parameterTypes = new ArrayList<>(resolvedMethodDeclaration.getNumberOfParams());
                for (int i = 0; i < resolvedMethodDeclaration.getNumberOfParams(); ++i) {
                    parameterTypes.add(javaParserWrapper.getExactType(resolvedMethodDeclaration.getParam(i).getType()));
                }
                final ElementNames declaringType = new ElementNames(resolvedMethodDeclaration.declaringType().getQualifiedName(), resolvedMethodDeclaration.declaringType().getName());
                final ElementNames method = FullyQualifiedNameUtils.getMethodName(declaringType, resolvedMethodDeclaration.getName(), parameterTypes);

                graphBuilder.addType(declaringType);
                graphBuilder.addMethod(method);
                graphBuilder.addHasMethodRelationship(declaringType, method);

                final ElementNames parentNamedCallable = javaParserWrapper.getParentNamedCallable(n, context.typeDeclarations);
                if (parentNamedCallable != null) {
                    graphBuilder.addInvokeRelationship(parentNamedCallable, method);
                    return;
                }

                if (javaParserWrapper.isInFieldDeclaration(n) || javaParserWrapper.isInInitializer(n)) {
                    context.fieldInitializationStorageMap.get(containingType).addInvokedMethod(method);
                } else {
                    throw new AssertionError("No parent method and no parent field declaration found for " + n);
                }
            } catch (UnsolvedSymbolException e) {
                LOG.debug("Symbol resolving failed in {}. Ignoring method call expression '{}'. Message: {}", containingType.fullyQualified(), n, e.getMessage());
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException occurred during processing {}. Ignoring method call expression '{}'. Message: {}", containingType.fullyQualified(), n, e.getMessage());
            } catch (RuntimeException e) {
                rethrowIfNoResolveException(containingType, e);
            }
        }

        @Override
        public void visit(FieldAccessExpr n, JavaFileContext context) {
            performVisit(n, context);
            super.visit(n, context);
        }

        private void performVisit(@Nonnull final FieldAccessExpr n, @Nonnull final JavaFileContext context) {
            Objects.requireNonNull(n);
            Objects.requireNonNull(context);

            final ElementNames containingType = javaParserWrapper.getParentTypeDeclaration(n, context.typeDeclarations);

            try {
                final ResolvedValueDeclaration valueDeclaration = n.resolve();
                if (valueDeclaration instanceof ResolvedFieldDeclaration resolvedFieldDeclaration) {
                    // Type that contains the accessed field
                    final ElementNames typeThatContainsTheField = new ElementNames(resolvedFieldDeclaration.declaringType().asReferenceType().getQualifiedName(), resolvedFieldDeclaration.declaringType().asReferenceType().getName());
                    graphBuilder.addType(typeThatContainsTheField);

                    // The accessed field
                    final ElementNames field = FullyQualifiedNameUtils.getFieldName(typeThatContainsTheField, resolvedFieldDeclaration.getName());
                    graphBuilder.addField(field);
                    graphBuilder.addHasFieldRelationship(typeThatContainsTheField, field);

                    // Type of the accessed field
                    final ElementNames typeOfField = javaParserWrapper.getSimplifiedType(resolvedFieldDeclaration.getType());
                    graphBuilder.addType(typeOfField);
                    graphBuilder.addOfTypeRelationship(field, typeOfField);

                    // The actual access field relationship
                    final ElementNames parentNamedCallable = javaParserWrapper.getParentNamedCallable(n, context.typeDeclarations);
                    if (parentNamedCallable != null) {
                        graphBuilder.addAccessRelationship(parentNamedCallable, field);
                        return;
                    }

                    if (javaParserWrapper.isInFieldDeclaration(n) || javaParserWrapper.isInInitializer(n)) {
                        context.fieldInitializationStorageMap.get(javaParserWrapper.getParentTypeDeclaration(n, context.typeDeclarations)).addAccessedField(field);
                    } else {
                        throw new AssertionError("No parent method and no parent field declaration found for: " + n);
                    }
                }
            } catch (UnsolvedSymbolException e) {
                LOG.debug("Symbol resolving failed in {}. Ignoring field access expression '{}'. Message: {}", containingType.fullyQualified(), n, e.getMessage());
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException occurred during processing {}. Ignoring field access expression '{}'. Message: {}", containingType.fullyQualified(), n, e.getMessage());
            } catch (RuntimeException e) {
                rethrowIfNoResolveException(containingType, e);
            }
        }

        @Override
        public void visit(ConstructorDeclaration n, JavaFileContext context) {
            performVisit(n, context);
            super.visit(n, context);
        }

        private void performVisit(@Nonnull final ConstructorDeclaration n, @Nonnull final JavaFileContext context) {
            Objects.requireNonNull(n);
            Objects.requireNonNull(context);

            final ElementNames containingType = javaParserWrapper.getParentTypeDeclaration(n, context.typeDeclarations);

            try {
                final List<ElementNames> parameterTypes = javaParserWrapper.getParameterTypes(n.getParameters());
                final ElementNames constructor = FullyQualifiedNameUtils.getConstructorName(containingType, parameterTypes);
                graphBuilder.addConstructor(constructor);
                graphBuilder.addHasConstructorRelationship(containingType, constructor);
                context.fieldInitializationStorageMap.get(containingType).addConstructor(constructor);
            } catch (UnsolvedSymbolException e) {
                LOG.debug("Symbol resolving failed in {}. Ignoring constructor declaration '{}'. Message: {}", containingType.fullyQualified(), n.getDeclarationAsString(false, false, false), e.getMessage());
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException occurred during processing {}. Ignoring constructor declaration '{}'. Message: {}", containingType.fullyQualified(), n.getDeclarationAsString(false, false, false), e.getMessage());
            } catch (RuntimeException e) {
                rethrowIfNoResolveException(containingType, e);
            }
        }

        @Override
        public void visit(MethodDeclaration n, JavaFileContext context) {
            performVisit(n, context);

            super.visit(n, context);
        }

        private void performVisit(@Nonnull final MethodDeclaration n, @Nonnull final JavaFileContext context) {
            Objects.requireNonNull(n);
            Objects.requireNonNull(context);

            final ElementNames containingType = javaParserWrapper.getParentTypeDeclaration(n, context.typeDeclarations);

            try {
                final List<ElementNames> parameterTypes = javaParserWrapper.getParameterTypes(n.getParameters());
                final ElementNames method = FullyQualifiedNameUtils.getMethodName(containingType, n.getNameAsString(), parameterTypes);
                graphBuilder.addMethod(method);
                graphBuilder.addHasMethodRelationship(containingType, method);
            } catch (UnsolvedSymbolException e) {
                LOG.debug("Symbol resolving failed in {}. Ignoring method declaration '{}'. Message: {}", containingType.fullyQualified(), n.getDeclarationAsString(false, false, false), e.getMessage());
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException occurred during processing {}. Ignoring method declaration '{}'. Message: {}", containingType.fullyQualified(), n.getDeclarationAsString(false, false, false), e.getMessage());
            } catch (RuntimeException e) {
                rethrowIfNoResolveException(containingType, e);
            }
        }

        @Override
        public void visit(InitializerDeclaration n, JavaFileContext context) {
            final ElementNames containingType = javaParserWrapper.getParentTypeDeclaration(n, context.typeDeclarations);

            if (n.isStatic()) {
                LOG.debug("Ignoring static initializer of {} as it is not supported.", containingType.fullyQualified());
                return;
            }

            super.visit(n, context);
        }

        @Override
        public void visit(EnumConstantDeclaration n, JavaFileContext context) {
            final ElementNames containingType = javaParserWrapper.getParentTypeDeclaration(n, context.typeDeclarations);

            LOG.debug("Ignoring enum constant declaration of {} as it is not supported.", containingType.fullyQualified());
        }

        @Override
        public void visit(AnnotationDeclaration n, JavaFileContext arg) {
            LOG.debug("Ignoring annotation declaration of {} as it is not supported.", n.getName());
        }

        @Override
        public void visit(NormalAnnotationExpr n, JavaFileContext arg) {
            LOG.debug("Ignoring annotation expression of {} as it is not supported.", n.getName());
        }

        @Override
        public void visit(SingleMemberAnnotationExpr n, JavaFileContext arg) {
            LOG.debug("Ignoring annotation expression of {} as it is not supported.", n.getName());
        }

        private void rethrowIfNoResolveException(@Nonnull final ElementNames containingType,
                                                 @Nonnull final RuntimeException e) {
            Objects.requireNonNull(containingType);
            Objects.requireNonNull(e);

            if (e.getMessage().contains("Unable to calculate the type of a parameter of a method call.")) {
                LOG.debug("Symbol resolving failed in {}. Ignoring it.", containingType.fullyQualified());
                return;
            }

            throw e;
        }
    }

    private static class JavaFileContext {
        /**
         * The declared type's name of a type declaring nodes in the AST.
         */
        @Nonnull
        public final HashMap<Node, ElementNames> typeDeclarations = new HashMap<>();
        public int anonymousTypeDeclarationsCounter = 0;
        @Nonnull
        public final HashMap<ElementNames, FieldInitializationStorage> fieldInitializationStorageMap = new HashMap<>();
    }
}
