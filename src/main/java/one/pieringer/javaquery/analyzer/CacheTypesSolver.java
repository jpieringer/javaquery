package one.pieringer.javaquery.analyzer;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class CacheTypesSolver implements TypeSolver {

    @Nonnull
    private final TypeSolver typeSolver;
    @CheckForNull
    private TypeSolver parent = null;
    @Nonnull
    private final Map<String, SymbolReference<ResolvedReferenceTypeDeclaration>> cache;

    public CacheTypesSolver(@Nonnull final TypeSolver typeSolver, @Nonnull final Map<String, SymbolReference<ResolvedReferenceTypeDeclaration>> cache) {
        this.typeSolver = Objects.requireNonNull(typeSolver);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    @CheckForNull
    public TypeSolver getParent() {
        return parent;
    }

    @Override
    public void setParent(@CheckForNull TypeSolver parent) {
        this.parent = parent;
    }

    @Override
    public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        SymbolReference<ResolvedReferenceTypeDeclaration> reference = typeSolver.tryToSolveType(name);

        if (reference.isSolved()) {
            cache.put(name, SymbolReference.solved(reference.getCorrespondingDeclaration()));
        } else {
            cache.put(name, SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class));
        }

        return cache.get(name);
    }
}
