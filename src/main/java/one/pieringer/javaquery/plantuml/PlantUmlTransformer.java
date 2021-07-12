package one.pieringer.javaquery.plantuml;

import one.pieringer.javaquery.database.ResultSet;
import one.pieringer.javaquery.model.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        // // TODO implement me with the new data model
//        for (HasFieldRelationship fieldRelationship : resultSet.getFieldRelationships()) {
//            uml.append(transform(fieldRelationship));
//        }

        for (CreateInstanceRelationship createInstanceRelationship : resultSet.getCreateInstanceRelationships()) {
            uml.append(transform(createInstanceRelationship));
        }

        for (AccessRelationship accessRelationship : resultSet.getAccessFieldRelationships()) {
            uml.append(transform(accessRelationship));
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
// TODO implement me with the new data model
//    private StringBuilder transform(@Nonnull final HasFieldRelationship fieldRelationship) {
//        StringBuilder uml = new StringBuilder();
//        uml.append(fieldRelationship.getContainingType().getName());
//        uml.append(" ");
//        uml.append("-[bold]-> ");
//        uml.append(fieldRelationship.getFieldType().getName());
//        uml.append(" ");
//        uml.append(": has-field");
//        uml.append("\n");
//        return uml;
//    }

    private StringBuilder transform(@Nonnull final CreateInstanceRelationship createInstanceRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(createInstanceRelationship.getInvokingExecutable().getName());
        uml.append(" ");
        uml.append("-[plain]-> ");
        uml.append(createInstanceRelationship.getInvokedConstructor().getName());
        uml.append(" ");
        uml.append(": creates");
        uml.append("\n");
        return uml;
    }

    private StringBuilder transform(@Nonnull final AccessRelationship accessRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(accessRelationship.getAccessingExecutable().getName());
        uml.append(" ");
        uml.append("-[dashed]-> ");
        uml.append(accessRelationship.getField().getName());
        uml.append(" ");
        uml.append(": accesses");
        uml.append("\n");
        return uml;
    }

    private StringBuilder transform(@Nonnull final InvokeRelationship invokeRelationship) {
        StringBuilder uml = new StringBuilder();
        uml.append(invokeRelationship.getInvokingExecutable().getName());
        uml.append(" ");
        uml.append("-[dotted]-> ");
        uml.append(invokeRelationship.getInvokedExecutable().getName());
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
