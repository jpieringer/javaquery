package one.pieringer.javaquery.gradleplugin;

import javax.annotation.Nonnull;
import java.util.Objects;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public class JavaqueryExtension {

    @Nonnull
    private final Property<String> databaseUri;

    public JavaqueryExtension(@Nonnull final ObjectFactory objectFactory) {
        this.databaseUri = objectFactory.property(String.class);
    }

    /**
     * @return The Neo4J database URI for the database where the parsed AST should be stored.
     */
    @Input
    @Nonnull
    public Property<String> getDatabaseUri() {
        return databaseUri;
    }


    /**
     * Sets the Neo4J database URI for the database where the parsed AST should be stored.
     */
    public void setDatabaseUri(@Nonnull final Property<String> databaseUri) {
        this.databaseUri.set(Objects.requireNonNull(databaseUri));
    }
}
