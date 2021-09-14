package one.pieringer.javaquery.analyzer;

import javax.annotation.Nonnull;
import java.util.Objects;

public record ElementNames(@Nonnull String fullyQualified, @Nonnull String simple) {

    public ElementNames(@Nonnull final String fullyQualified, @Nonnull final String simple) {
        this.fullyQualified = Objects.requireNonNull(fullyQualified);
        this.simple = Objects.requireNonNull(simple);
    }
}
