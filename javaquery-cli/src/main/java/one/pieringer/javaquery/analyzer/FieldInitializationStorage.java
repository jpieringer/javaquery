package one.pieringer.javaquery.analyzer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FieldInitializationStorage {

    @Nonnull
    private final List<ElementNames> invokedMethods = new ArrayList<>();
    @Nonnull
    private final List<ElementNames> invokedConstructors = new ArrayList<>();
    @Nonnull
    private final List<ElementNames> accessedFields = new ArrayList<>();
    @Nonnull
    private final List<ElementNames> constructors = new ArrayList<>();

    public void addInvokedMethod(@Nonnull final ElementNames method) {
        Objects.requireNonNull(method);
        this.invokedMethods.add(method);
    }

    public void addInvokedConstructor(@Nonnull final ElementNames constructor) {
        Objects.requireNonNull(constructor);
        this.invokedConstructors.add(constructor);
    }

    public void addAccessedField(@Nonnull final ElementNames field) {
        Objects.requireNonNull(field);
        this.accessedFields.add(field);
    }

    public void addConstructor(@Nonnull final ElementNames constructor) {
        Objects.requireNonNull(constructor);
        this.constructors.add(constructor);
    }

    @Nonnull
    public List<ElementNames> getInvokedMethods() {
        return invokedMethods;
    }

    @Nonnull
    public List<ElementNames> getInvokedConstructors() {
        return invokedConstructors;
    }

    @Nonnull
    public List<ElementNames> getAccessedFields() {
        return accessedFields;
    }

    @Nonnull
    public List<ElementNames> getConstructors() {
        return constructors;
    }

    public boolean hasFieldInitializationStored() {
        return !invokedConstructors.isEmpty() || !invokedMethods.isEmpty() || !accessedFields.isEmpty();
    }
}
