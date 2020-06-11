package one.pieringer.javaquery.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

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

    @CheckForNull
    public String getType(@Nonnull final Type type, @CheckForNull final CompilationUnit compilationUnit) {
        Objects.requireNonNull(type);

        if (compilationUnit == null) {
            return null;
        }

        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) type;

            String className = classOrInterfaceType.getNameAsString();
            for (ImportDeclaration anImport : compilationUnit.getImports()) {
                String importClassName = anImport.getName().getIdentifier();

                if (className.equals(importClassName)) {
                    return anImport.getName().asString();
                }
            }

            return classOrInterfaceType.resolve().getQualifiedName();
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            return getType(arrayType.getElementType(), compilationUnit);
        }

        return null;
    }

    @Nonnull
    public String getParentClassOrInterfaceDeclaration(@Nonnull final Node node) {
        if (node instanceof ClassOrInterfaceDeclaration) {
            return ((ClassOrInterfaceDeclaration) node).getFullyQualifiedName().orElseThrow();
        }
        if (node instanceof EnumDeclaration) {
            return ((EnumDeclaration) node).getFullyQualifiedName().orElseThrow();
        }
        if (node instanceof AnnotationDeclaration) {
            return ((AnnotationDeclaration) node).getFullyQualifiedName().orElseThrow();
        }

        if (node.getParentNode().isEmpty()) {
            throw new AssertionError("No class or interface declaration parent found.");
        }

        return getParentClassOrInterfaceDeclaration(node.getParentNode().get());
    }
}
