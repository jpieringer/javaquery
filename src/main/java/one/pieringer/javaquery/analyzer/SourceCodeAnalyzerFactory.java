package one.pieringer.javaquery.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SourceCodeAnalyzerFactory {

    @Nonnull
    public SourceCodeAnalyzer create(@Nonnull final SourceCodeProvider sourceCodeProvider,
                                     @Nonnull final List<File> dependencySourceDirectories,
                                     @Nonnull final List<File> dependencyJarFiles) {
        Objects.requireNonNull(sourceCodeProvider);
        Objects.requireNonNull(dependencySourceDirectories);
        Objects.requireNonNull(dependencyJarFiles);

        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        sourceCodeProvider.getSourceFolders().forEach(sourceFolder -> typeSolver.add(new JavaParserTypeSolver(sourceFolder)));
        dependencySourceDirectories.forEach(dependencySourceDirectory -> typeSolver.add(new JavaParserTypeSolver(dependencySourceDirectory)));
        dependencyJarFiles.forEach(dependencyJarFile -> typeSolver.add(createJarTypeSolver(dependencyJarFile)));
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(new CacheTypesSolver(typeSolver, new HashMap<>())));
        JavaParserWrapper javaParserWrapper = new JavaParserWrapper(new JavaParser(parserConfiguration));

        return new SourceCodeAnalyzer(javaParserWrapper);
    }

    @Nonnull
    private JarTypeSolver createJarTypeSolver(@Nonnull final File dependencyJarFile) {
        Objects.requireNonNull(dependencyJarFile);
        try {
            return new JarTypeSolver(dependencyJarFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not read the jar file '" + dependencyJarFile + "'", e);
        }
    }
}
