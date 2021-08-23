package one.pieringer.javaquery.analyzer;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SourceCodeAnalyzerFactory {

    @Nonnull
    private final ASTUtils astUtils;

    public SourceCodeAnalyzerFactory(@Nonnull final ASTUtils astUtils) {
        this.astUtils = Objects.requireNonNull(astUtils);
    }

    @Nonnull
    public SourceCodeAnalyzer create() {
        return new SourceCodeAnalyzer(astUtils);
    }
}
