package one.pieringer.javaquery.model;

import javax.annotation.Nonnull;

public interface Executable {

    @Nonnull
    String getName();

    @Nonnull
    String getFullyQualifiedName();
}
