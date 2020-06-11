package one.pieringer.javaquery.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SourceCodeAnalyzerFactory {

    @Nonnull
    public SourceCodeAnalyzer create(@Nonnull final SourceCodeProvider sourceCodeProvider) {
        Objects.requireNonNull(sourceCodeProvider);

        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        sourceCodeProvider.getSourceFolders().forEach(sourceFolder -> typeSolver.add(new JavaParserTypeSolver(sourceFolder)));
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        JavaParserWrapper javaParserWrapper = new JavaParserWrapper(new JavaParser(parserConfiguration));

        return new SourceCodeAnalyzer(javaParserWrapper);
    }
}
