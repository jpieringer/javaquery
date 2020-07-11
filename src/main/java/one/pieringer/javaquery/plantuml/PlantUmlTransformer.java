package one.pieringer.javaquery.plantuml;

import one.pieringer.javaquery.database.ResultSet;
import one.pieringer.javaquery.model.FieldRelationship;
import one.pieringer.javaquery.model.InheritanceRelationship;
import one.pieringer.javaquery.model.Type;

import javax.annotation.Nonnull;
import java.util.Objects;

public class PlantUmlTransformer {
    public static final String START_UML = "@startuml\n";
    public static final String END_UML = "@enduml\n";

    public String transform(@Nonnull final ResultSet resultSet) {
        Objects.requireNonNull(resultSet);

        StringBuilder uml = new StringBuilder();
        uml.append(START_UML);

        for (Type clazz : resultSet.getTypes()) {
            uml.append(transform(clazz));
        }

        for (FieldRelationship fieldRelationship : resultSet.getFieldRelationships()) {
            uml.append(transform(fieldRelationship));
        }

        for (InheritanceRelationship inheritanceRelationship : resultSet.getInheritanceRelationships()) {
            uml.append(transform(inheritanceRelationship));
        }

        uml.append(END_UML);

        return uml.toString();
    }

    private StringBuilder transform(@Nonnull final Type clazz) {
        StringBuilder uml = new StringBuilder();
        uml.append("class ");
        uml.append(clazz.getFullyQualifiedName());
        uml.append(" {\n");
        uml.append("}\n");
        return uml;
    }

    private StringBuilder transform(@Nonnull final FieldRelationship fieldRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(fieldRelationship.getContainingType().getFullyQualifiedName());
        uml.append(" ");
        uml.append("--> ");
        uml.append(fieldRelationship.getFieldType().getFullyQualifiedName());
        uml.append("\n");
        return uml;
    }

    private StringBuilder transform(@Nonnull final InheritanceRelationship inheritanceRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(inheritanceRelationship.getSubType().getFullyQualifiedName());
        uml.append(" ");
        uml.append("--|> ");
        uml.append(inheritanceRelationship.getSuperType().getFullyQualifiedName());
        uml.append("\n");
        return uml;
    }
}
