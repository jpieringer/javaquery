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
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.google.common.collect.Iterables;
import one.pieringer.javaquery.database.ResultSet;
import one.pieringer.javaquery.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashSet;
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
    public ResultSet analyze(@Nonnull final SourceCodeProvider sourceCodeProvider) {
        Objects.requireNonNull(sourceCodeProvider);

        Visitor visitor = new Visitor(javaParserWrapper);
        sourceCodeProvider.visitJavaFiles(javaFile -> visitor.visit(javaParserWrapper.parseFile(javaFile), null));

        return new ResultSet.ResultSetBuilder()
                .addCreateInstanceRelationships(visitor.createInstanceRelationships)
                .addFieldRelationships(visitor.fieldRelationships)
                .addInheritanceRelationships(visitor.inheritanceRelationships)
                .addInvokeRelationships(visitor.invokeRelationships)
                .addAccessFieldRelationships(visitor.accessFieldRelationships)
                .addTypes(visitor.typeSet.getTypes())
                .build();
    }

    private static class Visitor extends VoidVisitorAdapter<Void> {
        @Nonnull
        private final JavaParserWrapper javaParserWrapper;
        @Nonnull
        private final TypeSet typeSet = new TypeSet();
        @Nonnull
        private final Set<CreateInstanceRelationship> createInstanceRelationships = new HashSet<>();
        @Nonnull
        private final Set<FieldRelationship> fieldRelationships = new HashSet<>();
        @Nonnull
        private final Set<InheritanceRelationship> inheritanceRelationships = new HashSet<>();
        @Nonnull
        private final Set<InvokeRelationship> invokeRelationships = new HashSet<>();
        @Nonnull
        private final Set<AccessFieldRelationship> accessFieldRelationships = new HashSet<>();

        public Visitor(@Nonnull final JavaParserWrapper javaParserWrapper) {
            this.javaParserWrapper = Objects.requireNonNull(javaParserWrapper);
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
                    createInstanceRelationships.add(new CreateInstanceRelationship(typeSet.getOrCreateType(containingType), typeSet.getOrCreateType(fullyQualifiedName)));
                }
            } catch (UnsolvedSymbolException | UnsupportedOperationException e) {   // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("Cannot resolve {} in {}.", type, containingType);
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
                        fieldRelationships.add(new FieldRelationship(typeSet.getOrCreateType(containingType), variableDeclarator.getNameAsString(), typeSet.getOrCreateType(fullyQualifiedName)));
                    }
                }
            } catch (UnsolvedSymbolException | UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("Cannot resolve {} in {}.", type, containingType);
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
                        inheritanceRelationships.add(new InheritanceRelationship(typeSet.getOrCreateType(containingType), typeSet.getOrCreateType(fullyQualifiedName)));
                    }
                } catch (UnsolvedSymbolException | UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                    LOG.debug("Cannot resolve {} in {}.", type, containingType);
                }
            }
        }

        @Override
        public void visit(MethodCallExpr n, Void ignored) {
            super.visit(n, ignored);
            Objects.requireNonNull(n);

            String containingType = javaParserWrapper.getParentClassOrInterfaceDeclaration(n);

            ResolvedMethodDeclaration methodDeclaration;
            try {
                methodDeclaration = n.resolve();
            } catch (RuntimeException e) {    // Don't know why the RuntimeException is thrown.
                LOG.debug("Cannot resolve {} in {}.", n, containingType);
                return;
            }

            if (StringUtils.isNotBlank(methodDeclaration.getPackageName())) {
                invokeRelationships.add(new InvokeRelationship(typeSet.getOrCreateType(containingType), n.getNameAsString(), typeSet.getOrCreateType(methodDeclaration.getPackageName() + "." + methodDeclaration.getClassName())));
            } else {
                invokeRelationships.add(new InvokeRelationship(typeSet.getOrCreateType(containingType), n.getNameAsString(), typeSet.getOrCreateType(methodDeclaration.getClassName())));
            }
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
                LOG.debug("Cannot resolve {} in {}.", n, containingType);
                return;
            }

            try {
                if (valueDeclaration instanceof ResolvedFieldDeclaration) {
                    ResolvedFieldDeclaration resolvedFieldDeclaration = (ResolvedFieldDeclaration) valueDeclaration;
                    if (resolvedFieldDeclaration.declaringType().isType()) {
                        accessFieldRelationships.add(
                                new AccessFieldRelationship(typeSet.getOrCreateType(containingType),
                                        n.getNameAsString(),
                                        typeSet.getOrCreateType(resolvedFieldDeclaration.declaringType().asReferenceType().getQualifiedName())));
                    }
                }

            } catch (UnsolvedSymbolException | UnsupportedOperationException e) { // Don't know why the UnsupportedOperationException is thrown.
                LOG.debug("Cannot resolve {} in {}.", valueDeclaration, containingType);
            }
        }
    }
}
