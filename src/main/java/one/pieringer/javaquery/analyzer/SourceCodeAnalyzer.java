package one.pieringer.javaquery.analyzer;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.google.common.collect.Iterables;
import one.pieringer.javaquery.database.ResultSet;
import one.pieringer.javaquery.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SourceCodeAnalyzer {

    private static final Logger LOG = LogManager.getLogger(SourceCodeAnalyzer.class);

    @Nonnull
    private final JavaParserWrapper javaParserWrapper;

    public SourceCodeAnalyzer(@Nonnull final JavaParserWrapper javaParserWrapper) {
        this.javaParserWrapper = Objects.requireNonNull(javaParserWrapper);
    }

    @Nonnull
    public ResultSet analyze(@Nonnull final SourceCodeProvider sourceCodeProvider) {
        Objects.requireNonNull(sourceCodeProvider);

        final TypeSet typeSet = new TypeSet();
        final ResultSet.ResultSetBuilder resultSetBuilder = new ResultSet.ResultSetBuilder();
        final Visitor visitor = new Visitor(javaParserWrapper, typeSet, resultSetBuilder);

        sourceCodeProvider.visitJavaFiles(javaFile -> visitor.visit(javaParserWrapper.parseFile(javaFile), null));

        return resultSetBuilder.addTypes(typeSet.getTypes()).build();
    }

    private static class Visitor extends VoidVisitorAdapter<Void> {
        @Nonnull
        private final JavaParserWrapper javaParserWrapper;
        @Nonnull
        private final TypeSet typeSet;
        @Nonnull
        private final ResultSet.ResultSetBuilder resultSetBuilder;

        public Visitor(@Nonnull final JavaParserWrapper javaParserWrapper, @Nonnull final TypeSet typeSet, @Nonnull final ResultSet.ResultSetBuilder resultSetBuilder) {
            this.javaParserWrapper = Objects.requireNonNull(javaParserWrapper);
            this.typeSet = Objects.requireNonNull(typeSet);
            this.resultSetBuilder = Objects.requireNonNull(resultSetBuilder);
        }

        @Override
        public void visit(ObjectCreationExpr expr, Void ignored) {
            super.visit(expr, ignored);
            Objects.requireNonNull(expr);

            String containingType = javaParserWrapper.getParentClassOrInterfaceDeclaration(expr);

            ClassOrInterfaceType type = expr.getType();
            try {
                final String fullyQualifiedName = javaParserWrapper.getType(type, expr.findCompilationUnit().orElse(null));
                if (fullyQualifiedName != null) {
                    resultSetBuilder.addCreateInstanceRelationship(new CreateInstanceRelationship(typeSet.getOrCreateType(containingType), typeSet.getOrCreateType(fullyQualifiedName)));
                }
            } catch (UnsolvedSymbolException e) {
                LOG.debug("Cannot resolve {} in {}.", type, containingType);
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException during getting the type for " + type.asString(), e);
            }
        }

        @Override
        public void visit(FieldDeclaration n, Void ignored) {
            super.visit(n, ignored);
            Objects.requireNonNull(n);

            String containingType = javaParserWrapper.getParentClassOrInterfaceDeclaration(n);

            if (n.getVariables().getFirst().isEmpty()) {
                return;
            }

            com.github.javaparser.ast.type.Type type = n.getVariables().getFirst().get().getType();
            try {
                final String fullyQualifiedName = javaParserWrapper.getType(type, n.findCompilationUnit().orElse(null));
                if (fullyQualifiedName != null) {
                    for (VariableDeclarator variableDeclarator : n.getVariables()) {
                        resultSetBuilder.addFieldRelationship(new FieldRelationship(typeSet.getOrCreateType(containingType), variableDeclarator.getNameAsString(), typeSet.getOrCreateType(fullyQualifiedName)));
                    }
                }
            } catch (UnsolvedSymbolException e) {
                LOG.debug("Cannot resolve {} in {}.", type, containingType);
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException during getting the type for " + type.asString(), e);
            }
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void ignored) {
            super.visit(n, ignored);
            Objects.requireNonNull(n);

            if (n.getFullyQualifiedName().isPresent()) {
                typeSet.getOrCreateType(n.getFullyQualifiedName().get());
            }

            String containingType = javaParserWrapper.getParentClassOrInterfaceDeclaration(n);

            for (ClassOrInterfaceType type : Iterables.concat(n.getExtendedTypes(), n.getImplementedTypes())) {
                try {
                    final String fullyQualifiedName = javaParserWrapper.getType(type, n.findCompilationUnit().orElse(null));
                    if (fullyQualifiedName != null) {
                        resultSetBuilder.addInheritanceRelationship(new InheritanceRelationship(typeSet.getOrCreateType(containingType), typeSet.getOrCreateType(fullyQualifiedName)));
                    }
                } catch (UnsolvedSymbolException e) {
                    LOG.debug("Cannot resolve {} in {}.", type, containingType);
                } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                    LOG.debug("UnsupportedOperationException during getting the type for " + type.asString(), e);
                }
            }
        }

        @Override
        public void visit(MethodCallExpr n, Void ignored) {
            super.visit(n, ignored);
            Objects.requireNonNull(n);

            String containingType = javaParserWrapper.getParentClassOrInterfaceDeclaration(n);

            if (n.getScope().isEmpty()) {
                return; // The scope is null for e.g. static import method calls
            }

            String fullyQualifiedName;
            try {
                fullyQualifiedName = n.getScope().get().calculateResolvedType().describe();
            } catch (RuntimeException e) {    // Don't know why the RuntimeException is thrown.
                LOG.debug("Got a RuntimeException " + n + " in " + containingType + ".", e);
                return;
            }

            resultSetBuilder.addInvokeRelationship(new InvokeRelationship(typeSet.getOrCreateType(containingType), n.getNameAsString(), typeSet.getOrCreateType(fullyQualifiedName)));
        }

        @Override
        public void visit(FieldAccessExpr n, Void arg) {
            super.visit(n, arg);
            Objects.requireNonNull(n);

            String containingType = javaParserWrapper.getParentClassOrInterfaceDeclaration(n);

            ResolvedValueDeclaration valueDeclaration;
            try {
                valueDeclaration = n.resolve();
            } catch (RuntimeException e) {
                LOG.debug("Got a RuntimeException " + n + " in " + containingType + ".", e);
                return;
            }

            try {
                if (valueDeclaration instanceof ResolvedFieldDeclaration) {
                    ResolvedFieldDeclaration resolvedFieldDeclaration = (ResolvedFieldDeclaration) valueDeclaration;
                    if (resolvedFieldDeclaration.declaringType().isType()) {
                        resultSetBuilder.addAccessFieldRelationship(
                                new AccessFieldRelationship(typeSet.getOrCreateType(containingType),
                                        n.getNameAsString(),
                                        typeSet.getOrCreateType(resolvedFieldDeclaration.declaringType().asReferenceType().getQualifiedName())));
                    }
                }

            } catch (UnsolvedSymbolException e) {
                LOG.debug("Cannot resolve {} in {}.", valueDeclaration, containingType);
            } catch (UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("UnsupportedOperationException during getting the type for " + valueDeclaration.toString(), e);
            }
        }
    }
}
