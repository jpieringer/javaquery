package one.pieringer.javaquery.plantuml;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import one.pieringer.javaquery.database.ResultSet;
import one.pieringer.javaquery.model.AccessFieldRelationship;
import one.pieringer.javaquery.model.CreateInstanceRelationship;
import one.pieringer.javaquery.model.FieldRelationship;
import one.pieringer.javaquery.model.InheritanceRelationship;
import one.pieringer.javaquery.model.InvokeRelationship;
import one.pieringer.javaquery.model.Type;

public class PlantUmlTransformer {
    public static final String START_UML = "@startuml\n";
    public static final String END_UML = "@enduml\n";

    public String transform(@Nonnull final ResultSet resultSet, @Nonnull final Map<Type, List<String>> classToStereotypesMap) {
        Objects.requireNonNull(resultSet);

        StringBuilder uml = new StringBuilder();
        uml.append(START_UML);

        for (Type clazz : resultSet.getTypes()) {
            uml.append(transform(clazz, classToStereotypesMap.getOrDefault(clazz, new ArrayList<>())));
        }

        for (FieldRelationship fieldRelationship : resultSet.getFieldRelationships()) {
            uml.append(transform(fieldRelationship));
        }

        for (CreateInstanceRelationship createInstanceRelationship : resultSet.getCreateInstanceRelationships()) {
            uml.append(transform(createInstanceRelationship));
        }

        for (AccessFieldRelationship accessFieldRelationship : resultSet.getAccessFieldRelationships()) {
            uml.append(transform(accessFieldRelationship));
        }

        for (InvokeRelationship invokeRelationship : resultSet.getInvokeRelationships()) {
            uml.append(transform(invokeRelationship));
        }

        for (InheritanceRelationship inheritanceRelationship : resultSet.getInheritanceRelationships()) {
            uml.append(transform(inheritanceRelationship));
        }

        uml.append(END_UML);

        return uml.toString();
    }

    private StringBuilder transform(@Nonnull final Type clazz, @Nonnull final List<String> stereotypes) {
        StringBuilder uml = new StringBuilder();
        uml.append("class ");
        uml.append(clazz.getName());
        if (stereotypes.size() > 0) {
            uml.append(" <<");
            uml.append(String.join(", ", stereotypes));
            uml.append(" >>");
        }
        uml.append(" {\n");
        uml.append("}\n");
        return uml;
    }

    private StringBuilder transform(@Nonnull final FieldRelationship fieldRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(fieldRelationship.getContainingType().getName());
        uml.append(" ");
        uml.append("-[bold]-> ");
        uml.append(fieldRelationship.getFieldType().getName());
        uml.append(" ");
        uml.append(": has-field");
        uml.append("\n");
        return uml;
    }

    private StringBuilder transform(@Nonnull final CreateInstanceRelationship createInstanceRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(createInstanceRelationship.getContainingType().getName());
        uml.append(" ");
        uml.append("-[plain]-> ");
        uml.append(createInstanceRelationship.getObjectType().getName());
        uml.append(" ");
        uml.append(": creates");
        uml.append("\n");
        return uml;
    }

    private StringBuilder transform(@Nonnull final AccessFieldRelationship accessFieldRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(accessFieldRelationship.getContainingType().getName());
        uml.append(" ");
        uml.append("-[dashed]-> ");
        uml.append(accessFieldRelationship.getFieldDeclaringType().getName());
        uml.append(" ");
        uml.append(": accesses");
        uml.append("\n");
        return uml;
    }

    private StringBuilder transform(@Nonnull final InvokeRelationship invokeRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(invokeRelationship.getContainingType().getName());
        uml.append(" ");
        uml.append("-[dotted]-> ");
        uml.append(invokeRelationship.getInvokedType().getName());
        uml.append(" ");
        uml.append(": invokes");
        uml.append("\n");
        return uml;
    }

    private StringBuilder transform(@Nonnull final InheritanceRelationship inheritanceRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(inheritanceRelationship.getSubType().getName());
        uml.append(" ");
        uml.append("--|> ");
        uml.append(inheritanceRelationship.getSuperType().getName());
        uml.append(" ");
        uml.append(": inherits");
        uml.append("\n");
        return uml;
    }
}
