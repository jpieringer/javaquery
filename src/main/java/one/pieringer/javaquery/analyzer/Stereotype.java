package one.pieringer.javaquery.analyzer;

import com.google.common.base.MoreObjects;
import one.pieringer.javaquery.model.Type;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Stereotype {

    @Nonnull
    private final Type clazz;
    @Nonnull
    private final String name;

    public Stereotype(@Nonnull final Type clazz, @Nonnull final String name) {
        this.clazz = Objects.requireNonNull(clazz);
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("clazz", clazz)
                .add("name", name)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stereotype that = (Stereotype) o;
        return clazz.equals(that.clazz) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, name);
    }
}
