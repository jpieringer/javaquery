package one.pieringer.javaquery;

import one.pieringer.javaquery.analyzer.Analyzer;
import one.pieringer.javaquery.analyzer.SourceCodeAnalyzerFactory;
import one.pieringer.javaquery.database.GraphPersistence;
import org.mockito.ArgumentCaptor;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public class AnalyzerTestRunner {
    /**
     * Run the {@link Analyzer} on all classes that are located in the resources directory of the passed test class.
     *
     * @param testClass The test class in whose resource directory the classes to analyze are located.
     * @return A {@link Set} containing all nodes and edges that were found.
     */
    @Nonnull
    public static Set<Object> analyzeClassesOfTest(@Nonnull final Class<?> testClass) throws URISyntaxException {
        final URL testSourceDirectoryUrl = Objects.requireNonNull(testClass.getResource(testClass.getSimpleName()));
        final Analyzer analyzer = new Analyzer(new SourceCodeAnalyzerFactory());
        final GraphPersistence graphPersistenceMock = mock(GraphPersistence.class);
        //noinspection unchecked
        final ArgumentCaptor<Set<Object>> actualElementsCaptor = ArgumentCaptor.forClass(Set.class);
        doNothing().when(graphPersistenceMock).persist(actualElementsCaptor.capture());

        analyzer.analyze(Collections.singletonList(new File(testSourceDirectoryUrl.toURI()).getAbsolutePath()), graphPersistenceMock);

        return actualElementsCaptor.getAllValues().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }
}
