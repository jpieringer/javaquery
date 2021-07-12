package one.pieringer.javaquery.analyzer;

import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.google.common.collect.Iterables;
import one.pieringer.javaquery.FullyQualifiedNameUtils;
import one.pieringer.javaquery.model.GraphBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

        sourceCodeProvider.visitJavaFiles(javaFile -> visitor.visit(javaParserWrapper.parseFile(javaFile), null));

        return graphBuilder.getGraph();
    }

    private static class Visitor extends VoidVisitorAdapter<Void> {
        @Nonnull
        private final JavaParserWrapper javaParserWrapper;
        @Nonnull
        private final GraphBuilder graphBuilder;

        public Visitor(@Nonnull final JavaParserWrapper javaParserWrapper, @Nonnull final GraphBuilder graphBuilder) {
            this.javaParserWrapper = Objects.requireNonNull(javaParserWrapper);
            this.graphBuilder = Objects.requireNonNull(graphBuilder);
        }

        @Override
        public void visit(ObjectCreationExpr expr, Void ignored) {
            performVisit(expr);
            super.visit(expr, ignored);
        }

        private void performVisit(final ObjectCreationExpr expr) {
            Objects.requireNonNull(expr);
            final ElementNames containingType = javaParserWrapper.getParentClassOrInterfaceType(expr.getType());

            try {
                final ElementNames createdType = javaParserWrapper.getSimplifiedType(expr.getType());
                final ElementNames parentNamedCallable = javaParserWrapper.getParentNamedCallable(expr);

                final ResolvedConstructorDeclaration resolvedConstructorDeclaration = expr.resolve();
                final List<ElementNames> parameterTypes = new ArrayList<>(resolvedConstructorDeclaration.getNumberOfParams());
                for (int i = 0; i < resolvedConstructorDeclaration.getNumberOfParams(); ++i) {
                    parameterTypes.add(javaParserWrapper.getExactType(resolvedConstructorDeclaration.getParam(i).getType()));
                }
                final ElementNames declaringType = new ElementNames(resolvedConstructorDeclaration.declaringType().getQualifiedName(), resolvedConstructorDeclaration.declaringType().getName());
                final ElementNames constructor = FullyQualifiedNameUtils.getConstructorName(declaringType, parameterTypes);

                graphBuilder.addType(createdType);
                graphBuilder.addConstructor(constructor);
                graphBuilder.addHasConstructorRelationship(declaringType, constructor);
                graphBuilder.addCreateInstanceRelationship(parentNamedCallable, constructor);

            } catch (UnsolvedSymbolException e) {
                LOG.debug("Cannot resolve {} in {}.", expr.getType(), containingType);
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException during getting the type for " + expr.getType().asString(), e);
            }
        }

        @Override
        public void visit(FieldDeclaration n, Void ignored) {
            performVisit(n);
            super.visit(n, ignored);
        }

        private void performVisit(final FieldDeclaration n) {
            Objects.requireNonNull(n);

            final ElementNames containingType = javaParserWrapper.getParentClassOrInterfaceType(n);
            graphBuilder.addType(containingType);

            if (n.getVariables().getFirst().isEmpty()) {
                return;
            }

            try {
                final ElementNames fieldType = javaParserWrapper.getSimplifiedType(n.getVariables().getFirst().get().getType());
                if (fieldType == null) {
                    LOG.debug("Cannot resolve {} in {}.", n.getVariables().getFirst().get().getType(), containingType.fullyQualified());
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
                LOG.debug("Cannot resolve {} in {}.", n.getVariables().getFirst().get().getType(), containingType);
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException during getting the type for " + n.getVariables().getFirst().get().getType(), e);
            }
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void ignored) {
            performVisit(n);
            super.visit(n, ignored);
        }

        private void performVisit(final ClassOrInterfaceDeclaration n) {
            Objects.requireNonNull(n);

            final ElementNames declaredType = new ElementNames(n.getFullyQualifiedName().orElseThrow(), n.getNameAsString());
            graphBuilder.addType(declaredType);

            for (ClassOrInterfaceType type : Iterables.concat(n.getExtendedTypes(), n.getImplementedTypes())) {
                try {
                    final ElementNames superType = javaParserWrapper.getSimplifiedType(type);
                    if (superType == null) {
                        LOG.debug("Cannot resolve {} in {}.", type, declaredType.fullyQualified());
                        return;
                    }

                    graphBuilder.addType(superType);
                    graphBuilder.addInheritsRelationship(declaredType, superType);
                } catch (UnsolvedSymbolException e) {
                    LOG.debug("Cannot resolve {} in {}.", type, declaredType.fullyQualified());
                } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                    LOG.debug("UnsupportedOperationException during getting the type for " + type.asString(), e);
                }
            }
        }

        @Override
        public void visit(EnumDeclaration n, Void arg) {
            performVisit(n);
            super.visit(n, arg);
        }

        private void performVisit(final EnumDeclaration n) {
            Objects.requireNonNull(n);

            final ElementNames declaredType = new ElementNames(n.getFullyQualifiedName().orElseThrow(), n.getNameAsString());
            graphBuilder.addType(declaredType);
        }

        @Override
        public void visit(MethodCallExpr n, Void ignored) {
            performVisit(n);
            super.visit(n, ignored);
        }

        private void performVisit(final MethodCallExpr n) {
            Objects.requireNonNull(n);

            final ElementNames parentNamedCallable = javaParserWrapper.getParentNamedCallable(n);

            if (n.getScope().isEmpty()) {
                return; // The scope is null for e.g. static import method calls
            }

            final ResolvedMethodDeclaration resolvedMethodDeclaration = n.resolve();
            final List<ElementNames> parameterTypes = new ArrayList<>(resolvedMethodDeclaration.getNumberOfParams());
            for (int i = 0; i < resolvedMethodDeclaration.getNumberOfParams(); ++i) {
                parameterTypes.add(javaParserWrapper.getExactType(resolvedMethodDeclaration.getParam(i).getType()));
            }
            ElementNames declaringType = new ElementNames(resolvedMethodDeclaration.declaringType().getQualifiedName(), resolvedMethodDeclaration.declaringType().getName());
            ElementNames method = FullyQualifiedNameUtils.getMethodName(declaringType, resolvedMethodDeclaration.getName(), parameterTypes);

            graphBuilder.addType(declaringType);
            graphBuilder.addMethod(method);
            graphBuilder.addHasMethodRelationship(declaringType, method);
            graphBuilder.addInvokeRelationship(parentNamedCallable, method);
        }


        @Override
        public void visit(FieldAccessExpr n, Void arg) {
            performVisit(n);
            super.visit(n, arg);
        }

        private void performVisit(final FieldAccessExpr n) {
            Objects.requireNonNull(n);

            final ElementNames containingType = javaParserWrapper.getParentClassOrInterfaceType(n);
            final ElementNames parentNamedCallable = javaParserWrapper.getParentNamedCallable(n);

            ResolvedValueDeclaration valueDeclaration;
            try {
                valueDeclaration = n.resolve();
            } catch (RuntimeException e) {
                LOG.debug("Got a RuntimeException " + n + " in " + containingType + ".", e);
                return;
            }

            try {
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
                    graphBuilder.addAccessRelationship(parentNamedCallable, field);
                }

            } catch (UnsolvedSymbolException e) {
                LOG.debug("Cannot resolve {} in {}.", valueDeclaration, containingType);
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException during getting the type for " + valueDeclaration.toString(), e);
            }
        }

        @Override
        public void visit(ConstructorDeclaration n, Void arg) {
            performVisit(n);
            super.visit(n, arg);
        }

        private void performVisit(final ConstructorDeclaration n) {
            Objects.requireNonNull(n);

            final ElementNames containingType = javaParserWrapper.getParentClassOrInterfaceType(n);
            graphBuilder.addType(containingType);

            final List<ElementNames> parameterTypes = javaParserWrapper.getParameterTypes(n.getParameters());
            final ElementNames constructor = FullyQualifiedNameUtils.getConstructorName(containingType, parameterTypes);
            graphBuilder.addConstructor(constructor);
            graphBuilder.addHasConstructorRelationship(containingType, constructor);
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            performVisit(n);

            super.visit(n, arg);

        }

        private void performVisit(final MethodDeclaration n) {
            Objects.requireNonNull(n);

            final ElementNames containingType = javaParserWrapper.getParentClassOrInterfaceType(n);
            graphBuilder.addType(containingType);

            final List<ElementNames> parameterTypes = javaParserWrapper.getParameterTypes(n.getParameters());
            final ElementNames method = FullyQualifiedNameUtils.getMethodName(containingType, n.getNameAsString(), parameterTypes);
            graphBuilder.addMethod(method);
            graphBuilder.addHasMethodRelationship(containingType, method);
        }
    }
}
