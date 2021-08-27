package one.pieringer.javaquery.analyzer;

import one.pieringer.javaquery.FullyQualifiedNameUtils;
import one.pieringer.javaquery.model.GraphBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class SourceCodeAnalyzer {

    private static final Logger LOG = LogManager.getLogger(SourceCodeAnalyzer.class);

    @Nonnull
    private final ASTUtils astUtils;

    public SourceCodeAnalyzer(@Nonnull final ASTUtils astUtils) {
        this.astUtils = Objects.requireNonNull(astUtils);
    }

    @Nonnull
    public Set<Object> analyze(@Nonnull final SourceCodeProvider sourceCodeProvider) {
        Objects.requireNonNull(sourceCodeProvider);

        final GraphBuilder graphBuilder = new GraphBuilder();

        final List<String> sourcePathEntries = sourceCodeProvider.getSourceFolders().stream().map(File::getAbsolutePath).collect(Collectors.toList());
        sourcePathEntries.addAll(sourceCodeProvider.getDependencySourceDirectories().stream().map(File::getAbsolutePath).collect(Collectors.toList()));
        final List<String> classPathEntries = sourceCodeProvider.getDependencyJarFiles().stream().map(File::getAbsolutePath).collect(Collectors.toList());

        sourceCodeProvider.getJavaFileStream().parallel().forEach((javaFile) -> analyzeJavaFile(javaFile.sourceFolder(), javaFile.javaFile(), sourcePathEntries, classPathEntries, graphBuilder));

        return graphBuilder.getGraph();
    }

    private void analyzeJavaFile(@Nonnull final File project, @Nonnull final File javaFile,
                                 @Nonnull final List<String> sourcePathEntries,
                                 @Nonnull final List<String> classPathEntries,
                                 @Nonnull final GraphBuilder graphBuilder) {
        final Visitor visitor = new Visitor(graphBuilder, astUtils);
        final ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        final Hashtable<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.latestSupportedJavaVersion());
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.latestSupportedJavaVersion());
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.latestSupportedJavaVersion());
        parser.setCompilerOptions(options);
        parser.setEnvironment(classPathEntries.toArray(String[]::new), sourcePathEntries.toArray(new String[0]), sourcePathEntries.stream().map(f -> StandardCharsets.UTF_8.name()).toArray(String[]::new), true);

        parser.setUnitName(javaFile.getAbsolutePath().replace(project.getAbsolutePath(), ""));
        try {
            final String currentJavaFileName = FilenameUtils.removeExtension(javaFile.getName());

            parser.setSource(Files.readString(javaFile.toPath()).toCharArray());

            CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
            for (IProblem problem : compilationUnit.getProblems()) {
                if (problem.isError()) {
                    LOG.debug("Compilation error in '" + currentJavaFileName + "': " + problem);
                }
            }

            visitor.setContext(new Visitor.JavaFileContext(currentJavaFileName));
            compilationUnit.accept(visitor);
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("Failed to analyze file '" + javaFile.getAbsolutePath() + "'", e);
        } catch (AssertionError e) {
            throw new AssertionError("Failed to analyze file '" + javaFile.getAbsolutePath() + "'", e);
        }
    }

    private static class Visitor extends ASTVisitor {
        @Nonnull
        private final GraphBuilder graphBuilder;
        @Nonnull
        private final ASTUtils astUtils;
        @Nonnull
        private JavaFileContext context;

        public Visitor(@Nonnull final GraphBuilder graphBuilder, @Nonnull final ASTUtils astUtils) {
            this.graphBuilder = Objects.requireNonNull(graphBuilder);
            this.astUtils = Objects.requireNonNull(astUtils);
            this.context = new JavaFileContext("<not-initialized>");
        }

        public void setContext(@Nonnull final JavaFileContext context) {
            this.context = Objects.requireNonNull(context);
        }

        @Override
        public boolean visit(final TypeDeclaration node) {
            Objects.requireNonNull(node);

            final ITypeBinding typeBinding = node.resolveBinding();
            if (typeBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring type declaration '{}'", context.currentFile, node);
                return false;
            }

            ElementNames declaredType;
            if (typeBinding.isLocal()) {
                declaredType = FullyQualifiedNameUtils.getLocalClassName(astUtils.getName(typeBinding.getDeclaringClass(), context.anonymousTypeDeclarations), context.anonymousTypeDeclarationsCounter, typeBinding.getName());
                context.anonymousTypeDeclarationsCounter++;
                context.anonymousTypeDeclarations.put(typeBinding, declaredType);
            } else {
                declaredType = astUtils.getName(typeBinding, context.anonymousTypeDeclarations);
            }

            context.fieldInitializationStorageMap.put(declaredType, new FieldInitializationStorage());
            addType(declaredType, typeBinding);

            final ArrayList<Type> superTypes = new ArrayList<>(node.superInterfaceTypes());
            if (node.getSuperclassType() != null) {
                superTypes.add(node.getSuperclassType());
            }

            for (Type rawSuperType : superTypes) {
                final ITypeBinding superTypeBinding = rawSuperType.resolveBinding();
                if (superTypeBinding == null) {
                    LOG.debug("Binding failed in '{}'. Ignoring super type '{}'", context.currentFile, node.getSuperclassType());
                } else {
                    final ElementNames superType = astUtils.getName(superTypeBinding, context.anonymousTypeDeclarations);
                    addType(superType, superTypeBinding);
                    graphBuilder.addInheritsRelationship(declaredType, superType);
                }
            }

            return true;
        }

        @Override
        public void endVisit(TypeDeclaration node) {
            Objects.requireNonNull(node);

            final ITypeBinding typeBinding = node.resolveBinding();
            if (typeBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring type declaration '{}'", context.currentFile, node);
                return;
            }

            final ElementNames declaredType = astUtils.getName(typeBinding, context.anonymousTypeDeclarations);
            addFieldInitializationCallsToTheConstructors(declaredType, context.fieldInitializationStorageMap.remove(declaredType));
        }

        // TODO add RecordDeclaration


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
        public boolean visit(EnumDeclaration node) {
            Objects.requireNonNull(node);

            final ITypeBinding typeBinding = node.resolveBinding();
            if (typeBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring enum declaration '{}'", context.currentFile, node);
                return true;
            }

            final ElementNames declaredType = astUtils.getName(typeBinding, context.anonymousTypeDeclarations);
            context.fieldInitializationStorageMap.put(declaredType, new FieldInitializationStorage());
            addType(declaredType, typeBinding);

            return true;
        }

        @Override
        public boolean visit(AnonymousClassDeclaration node) {
            Objects.requireNonNull(node);

            final ITypeBinding typeBinding = node.resolveBinding();
            if (typeBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring anonymous type declaration '{}'", context.currentFile, node);
                return true;
            }

            // Manually create the name of the anonymous class as the binary name of the anonymous class is not always available.
            final ElementNames declaredType = FullyQualifiedNameUtils.getAnonymousClassName(astUtils.getName(typeBinding.getDeclaringClass(), context.anonymousTypeDeclarations), context.anonymousTypeDeclarationsCounter);
            context.anonymousTypeDeclarationsCounter++;
            context.anonymousTypeDeclarations.put(typeBinding, declaredType);
            context.fieldInitializationStorageMap.put(declaredType, new FieldInitializationStorage());
            addType(declaredType, typeBinding);

            final ITypeBinding superTypeBinding = ((ClassInstanceCreation) node.getParent()).getType().resolveBinding();
            if (superTypeBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring super class of anonymous type declaration '{}'", context.currentFile, node);
            } else {
                final ElementNames superType = astUtils.getName(superTypeBinding, context.anonymousTypeDeclarations);
                addType(superType, superTypeBinding);
                graphBuilder.addInheritsRelationship(declaredType, superType);
            }

            return true;
        }

        @Override
        public void endVisit(AnonymousClassDeclaration node) {
            Objects.requireNonNull(node);

            final ITypeBinding typeBinding = node.resolveBinding();
            if (typeBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring anonymous type declaration '{}'", context.currentFile, node);
                return;
            }

            final ElementNames declaredType = astUtils.getName(typeBinding, context.anonymousTypeDeclarations);
            addFieldInitializationCallsToTheConstructors(declaredType, context.fieldInitializationStorageMap.remove(declaredType));
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            Objects.requireNonNull(node);

            final IMethodBinding methodBinding = node.resolveBinding();
            if (methodBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring method/constructor declaration '{}'", context.currentFile, node);
                return true;
            }

            final ElementNames containingType = astUtils.getName(methodBinding.getDeclaringClass(), context.anonymousTypeDeclarations);
            final List<ElementNames> parameterTypes = Arrays.stream(methodBinding.getParameterTypes()).map(typeBinding -> astUtils.getExactType(typeBinding, context.anonymousTypeDeclarations)).collect(Collectors.toList());

            if (node.isConstructor()) {
                final ElementNames constructor = FullyQualifiedNameUtils.getConstructorName(containingType, parameterTypes);
                graphBuilder.addConstructor(constructor);
                graphBuilder.addHasConstructorRelationship(containingType, constructor);
                context.fieldInitializationStorageMap.get(containingType).addConstructor(constructor);
            } else {
                final ElementNames method = FullyQualifiedNameUtils.getMethodName(containingType, methodBinding.getName(), parameterTypes);
                graphBuilder.addMethod(method);
                graphBuilder.addHasMethodRelationship(containingType, method);
            }

            return true;
        }


        @Override
        public boolean visit(FieldDeclaration node) {
            Objects.requireNonNull(node);

            for (VariableDeclarationFragment variableDeclarationFragment : (List<VariableDeclarationFragment>) node.fragments()) {
                final IVariableBinding variableBinding = variableDeclarationFragment.resolveBinding();

                if (variableBinding == null) {
                    LOG.debug("Binding failed in '{}'. Ignoring field declaration '{}'", context.currentFile, node);
                    continue;
                }

                final ElementNames containingType = astUtils.getName(variableBinding.getDeclaringClass(), context.anonymousTypeDeclarations);

                final ElementNames field = FullyQualifiedNameUtils.getFieldName(containingType, variableBinding.getName());
                graphBuilder.addField(field);
                graphBuilder.addHasFieldRelationship(containingType, field);

                if (variableBinding.getType().isTypeVariable() || variableBinding.getType().isCapture()) {
                    // TODO add support for type variables
                    LOG.debug("Ignoring field declaration in '{}' due to the type variable '{}'", context.currentFile, node);
                } else {
                    final ITypeBinding fieldTypeBinding = astUtils.getSimplifiedTypeBinding(variableBinding.getType());
                    final ElementNames fieldType = astUtils.getName(fieldTypeBinding, context.anonymousTypeDeclarations);
                    addType(fieldType, fieldTypeBinding);
                    graphBuilder.addOfTypeRelationship(field, fieldType);
                }
            }

            return true;
        }

        @Override
        public void endVisit(ClassInstanceCreation node) {
            Objects.requireNonNull(node);

            final IMethodBinding methodBinding = node.resolveConstructorBinding();
            if (methodBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring class instance creation '{}'", context.currentFile, node);
                return;
            }
            final IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();

            ElementNames declaringType = astUtils.getName(methodDeclaration.getDeclaringClass(), context.anonymousTypeDeclarations);
            addType(declaringType, methodDeclaration.getDeclaringClass());

            final ElementNames constructor = astUtils.getMethodBindingName(methodDeclaration, context.anonymousTypeDeclarations);

            graphBuilder.addConstructor(constructor);
            graphBuilder.addHasConstructorRelationship(declaringType, constructor);

            final ASTNode enclosingNode = astUtils.getEnclosingNode(node);
            if (enclosingNode instanceof MethodDeclaration invokingMethodDeclaration) {
                final IMethodBinding invokingMethodBinding = invokingMethodDeclaration.resolveBinding();
                if (invokingMethodBinding == null) {
                    LOG.debug("Binding failed in '{}'. Ignoring class instance creation invocation '{}'", context.currentFile, node);
                } else {
                    graphBuilder.addInvokeRelationship(astUtils.getMethodBindingName(invokingMethodBinding, context.anonymousTypeDeclarations), constructor);
                }
            } else if (enclosingNode instanceof FieldDeclaration || enclosingNode instanceof Initializer) {
                context.fieldInitializationStorageMap.get(astUtils.getParentTypeDeclaration(node, context.anonymousTypeDeclarations)).addInvokedConstructor(constructor);
            } else {
                throw new AssertionError("Unknown enclosing method type '" + enclosingNode.getClass() + "' for create instance expression '" + node + "'");
            }
        }

        @Override
        public void endVisit(MethodInvocation node) {
            Objects.requireNonNull(node);

            final IMethodBinding methodBinding = node.resolveMethodBinding();
            if (methodBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring method invocation '{}'", context.currentFile, node);
                return;
            }
            final IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();

            final ElementNames declaringType = astUtils.getName(methodDeclaration.getDeclaringClass(), context.anonymousTypeDeclarations);
            addType(declaringType, methodDeclaration.getDeclaringClass());

            final ElementNames method = astUtils.getMethodBindingName(methodDeclaration, context.anonymousTypeDeclarations);
            graphBuilder.addMethod(method);
            graphBuilder.addHasMethodRelationship(declaringType, method);

            final ASTNode enclosingNode = astUtils.getEnclosingNode(node);
            if (enclosingNode instanceof MethodDeclaration invokingMethodDeclaration) {
                final IMethodBinding invokingMethodBinding = invokingMethodDeclaration.resolveBinding();
                if (invokingMethodBinding == null) {
                    LOG.debug("Binding failed in '{}'. Ignoring method invocation '{}'", context.currentFile, node);
                } else {
                    graphBuilder.addInvokeRelationship(astUtils.getMethodBindingName(invokingMethodBinding, context.anonymousTypeDeclarations), method);
                }
            } else if (enclosingNode instanceof FieldDeclaration || enclosingNode instanceof Initializer) {
                context.fieldInitializationStorageMap.get(astUtils.getParentTypeDeclaration(node, context.anonymousTypeDeclarations)).addInvokedMethod(method);
            } else {
                throw new AssertionError("Unknown enclosing method type '" + enclosingNode.getClass() + "' for method invocation expression '" + node + "'");
            }
        }

        @Override
        public void endVisit(FieldAccess node) {
            Objects.requireNonNull(node);

            final IVariableBinding variableBinding = node.resolveFieldBinding();
            if (variableBinding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring field access '{}'", context.currentFile, node);
                return;
            }

            handleFieldAccess(node, variableBinding);
        }


        @Override
        public void endVisit(QualifiedName node) {
            Objects.requireNonNull(node);

            final IBinding binding = node.resolveBinding();
            if (binding == null) {
                LOG.debug("Binding failed in '{}'. Ignoring qualified name '{}'", context.currentFile, node);
                return;
            }

            if (binding instanceof IVariableBinding variableBinding) {
                handleFieldAccess(node, variableBinding);
            }
        }

        public void handleFieldAccess(@Nonnull final ASTNode node, @Nonnull final IVariableBinding variableBinding) {
            Objects.requireNonNull(node);
            Objects.requireNonNull(variableBinding);

            if (variableBinding.isEnumConstant()) {
                // TODO add support for enum constant access
                return;
            }

            if (variableBinding.getName().equals("length") && variableBinding.getDeclaringClass() == null) {
                // The length field of an array is the declaring class not set. Ignore it.
                return;
            }

            // Type that contains the accessed field
            final ElementNames typeThatContainsTheField = astUtils.getName(variableBinding.getDeclaringClass(), context.anonymousTypeDeclarations);
            addType(typeThatContainsTheField, variableBinding.getDeclaringClass());

            // The accessed field
            final ElementNames field = FullyQualifiedNameUtils.getFieldName(typeThatContainsTheField, variableBinding.getName());
            graphBuilder.addField(field);
            graphBuilder.addHasFieldRelationship(typeThatContainsTheField, field);

            // Type of the accessed field
            if (variableBinding.getType().isTypeVariable() || variableBinding.getType().isCapture()) {
                // TODO add support for type variables
                LOG.debug("Ignoring type of field access in '{}' due to the type variable '{}'", context.currentFile, node);
            } else {
                final ITypeBinding fieldTypeBinding = astUtils.getSimplifiedTypeBinding(variableBinding.getType());
                final ElementNames fieldType = astUtils.getName(fieldTypeBinding, context.anonymousTypeDeclarations);
                addType(fieldType, fieldTypeBinding);
                graphBuilder.addOfTypeRelationship(field, fieldType);
            }
            // The actual access field relationship
            final ASTNode enclosingNode = astUtils.getEnclosingNode(node);
            if (enclosingNode instanceof MethodDeclaration invokingMethodDeclaration) {
                final IMethodBinding invokingMethodBinding = invokingMethodDeclaration.resolveBinding();
                if (invokingMethodBinding == null) {
                    LOG.debug("Binding failed in '{}'. Ignoring field access '{}'", context.currentFile, node);
                } else {
                    graphBuilder.addAccessRelationship(astUtils.getMethodBindingName(invokingMethodBinding, context.anonymousTypeDeclarations), field);
                }
            } else if (enclosingNode instanceof FieldDeclaration || enclosingNode instanceof Initializer) {
                context.fieldInitializationStorageMap.get(astUtils.getParentTypeDeclaration(node, context.anonymousTypeDeclarations)).addAccessedField(field);
            } else {
                throw new AssertionError("Unknown enclosing method type '" + enclosingNode.getClass() + "' for field access expression '" + node + "'");
            }
        }

        @Override
        public boolean visit(ImportDeclaration node) {
            // We are not interested in ImportDeclarations
            return false;
        }

        @Override
        public boolean visit(AnnotationTypeDeclaration node) {
            // We are not interested in AnnotationTypeDeclarations
            return false;
        }

        @Override
        public boolean visit(EnumConstantDeclaration node) {
            // TODO add support for enum constants
            return false;
        }

        @Override
        public boolean visit(MarkerAnnotation node) {
            return false;
        }

        @Override
        public boolean visit(NormalAnnotation node) {
            return false;
        }

        @Override
        public boolean visit(SingleMemberAnnotation node) {
            return false;
        }

        private void addType(@Nonnull final ElementNames declaredType, @Nonnull final ITypeBinding typeBinding) {
            Objects.requireNonNull(declaredType);
            Objects.requireNonNull(typeBinding);
            graphBuilder.addType(declaredType, typeBinding.isClass(), typeBinding.isEnum(), typeBinding.isInterface(), typeBinding.isPrimitive(), Modifier.isAbstract(typeBinding.getModifiers()));
        }

        private static class JavaFileContext {
            @Nonnull
            public final String currentFile;

            /**
             * The generated names of anonymous classes.
             */
            @Nonnull
            public final HashMap<ITypeBinding, ElementNames> anonymousTypeDeclarations = new HashMap<>();
            public int anonymousTypeDeclarationsCounter = 1;

            @Nonnull
            public final HashMap<ElementNames, FieldInitializationStorage> fieldInitializationStorageMap = new HashMap<>();

            public JavaFileContext(@Nonnull String currentFile) {
                this.currentFile = currentFile;
            }
        }
    }
}
