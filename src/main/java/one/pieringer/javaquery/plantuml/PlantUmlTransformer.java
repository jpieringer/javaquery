package one.pieringer.javaquery.plantuml;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import one.pieringer.javaquery.database.ResultSet;
import one.pieringer.javaquery.model.AccessRelationship;
import one.pieringer.javaquery.model.Constructor;
import one.pieringer.javaquery.model.Executable;
import one.pieringer.javaquery.model.Field;
import one.pieringer.javaquery.model.HasConstructorRelationship;
import one.pieringer.javaquery.model.HasFieldRelationship;
import one.pieringer.javaquery.model.HasMethodRelationship;
import one.pieringer.javaquery.model.InheritanceRelationship;
import one.pieringer.javaquery.model.InvokeRelationship;
import one.pieringer.javaquery.model.Method;
import one.pieringer.javaquery.model.OfTypeRelationship;
import one.pieringer.javaquery.model.Type;

public class PlantUmlTransformer {
    public static final String START_UML = "@startuml\n";
    public static final String END_UML = "@enduml\n";

    private static final Logger LOG = LogManager.getLogger(PlantUmlTransformer.class);

    public String transform(@Nonnull final ResultSet resultSet, @Nonnull final Map<Type, List<String>> classToStereotypesMap) {
        Objects.requireNonNull(resultSet);

        StringBuilder uml = new StringBuilder();
        uml.append(START_UML);

        addTypes(resultSet, classToStereotypesMap, uml);
        addInheritanceArrow(resultSet, uml);
        addHasFieldArrow(resultSet, uml);
        addInvokeArrow(resultSet, uml);
        addCreateArrow(resultSet, uml);
        addAccessArrow(resultSet, uml);

        uml.append(END_UML);

        return uml.toString();
    }

    private void addTypes(@Nonnull final ResultSet resultSet, @Nonnull final Map<Type, List<String>> classToStereotypesMap, @Nonnull final StringBuilder uml) {
        Objects.requireNonNull(resultSet);
        Objects.requireNonNull(classToStereotypesMap);
        Objects.requireNonNull(uml);

        for (Type clazz : resultSet.getTypes()) {
            uml.append(addType(clazz, classToStereotypesMap.getOrDefault(clazz, new ArrayList<>())));
        }
    }

    private StringBuilder addType(@Nonnull final Type clazz, @Nonnull final List<String> stereotypes) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(stereotypes);

        StringBuilder uml = new StringBuilder();
        if (clazz.isAbstract()) {
            uml.append("abstract ");
        }

        if (clazz.isClass()) {
            uml.append("class ");
        } else if (clazz.isEnum()) {
            uml.append("enum ");
        } else if (clazz.isInterface()) {
            uml.append("interface ");
        } else {
            uml.append("class ");
//            throw new IllegalArgumentException("Found a type that is not a class/enum/interface: " + clazz);
        }
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

    private void addInheritanceArrow(@Nonnull final ResultSet resultSet, @Nonnull final StringBuilder uml) {
        Objects.requireNonNull(resultSet);
        Objects.requireNonNull(uml);

        for (InheritanceRelationship inheritanceRelationship : resultSet.getInheritanceRelationships()) {
            uml.append(addInheritanceArrow(inheritanceRelationship));
        }
    }

    private StringBuilder addInheritanceArrow(@Nonnull final InheritanceRelationship inheritanceRelationship) {
        Objects.requireNonNull(inheritanceRelationship);

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

    private void addHasFieldArrow(@Nonnull final ResultSet resultSet, @Nonnull final StringBuilder uml) {
        Objects.requireNonNull(resultSet);
        Objects.requireNonNull(uml);

        for (HasFieldRelationship hasFieldRelationship : resultSet.getHasFieldRelationships()) {
            final Type declaringType = hasFieldRelationship.getDeclaringType();

            final OfTypeRelationship ofTypeRelationship = resultSet.getOfTypeRelationship(hasFieldRelationship.getField());
            if (ofTypeRelationship == null) {
                LOG.warn("Could not find an OfTypeRelationship for the HasFieldRelationship {}. Ignoring it.", hasFieldRelationship);
                continue;
            }

            uml.append(addHasFieldArrow(declaringType, ofTypeRelationship.getFieldType()));
        }
    }

    private StringBuilder addHasFieldArrow(@Nonnull final Type declaringType, @Nonnull final Type fieldType) {
        Objects.requireNonNull(declaringType);
        Objects.requireNonNull(fieldType);

        StringBuilder uml = new StringBuilder();
        uml.append(declaringType.getName());
        uml.append(" ");
        uml.append("-[bold]-> ");
        uml.append(fieldType.getName());
        uml.append(" ");
        uml.append(": has-field");
        uml.append("\n");
        return uml;
    }

    private void addInvokeArrow(@Nonnull final ResultSet resultSet, @Nonnull final StringBuilder uml) {
        Objects.requireNonNull(resultSet);
        Objects.requireNonNull(uml);

        for (InvokeRelationship invokeRelationship : resultSet.getInvokeRelationships()) {
            final Type fromType = getDeclaringType(invokeRelationship.getInvokingExecutable(), resultSet);
            if (fromType == null) {
                LOG.warn("Could not find an HasConstructorRelationship or HasMethodRelationship for the InvokeRelationship {}. Ignoring it.", invokeRelationship);
                continue;
            }

            if (!(invokeRelationship.getInvokedExecutable() instanceof Method)) {
                continue;
            }
            final HasMethodRelationship hasMethodRelationship = resultSet.getHasMethodRelationship((Method) invokeRelationship.getInvokedExecutable());
            if (hasMethodRelationship == null) {
                LOG.warn("Could not find an HasMethodRelationship for the InvokeRelationship {}. Ignoring it.", invokeRelationship);
                continue;
            }
            Type toType = hasMethodRelationship.getDeclaringType();

            uml.append(addInvokeArrow(fromType, toType, invokeRelationship.getInvokedExecutable()));
        }
    }

    @CheckForNull
    private Type getDeclaringType(@Nonnull final Executable executable, @Nonnull final ResultSet resultSet) {
        if (executable instanceof Constructor constructor) {
            final HasConstructorRelationship hasConstructorRelationship = resultSet.getHasConstructorRelationship(constructor);
            if (hasConstructorRelationship == null) {
                return null;
            }
            return hasConstructorRelationship.getDeclaringType();
        } else if (executable instanceof Method method) {
            final HasMethodRelationship hasMethodRelationship = resultSet.getHasMethodRelationship(method);
            if (hasMethodRelationship == null) {
                return null;
            }
            return hasMethodRelationship.getDeclaringType();
        }

        throw new AssertionError("Unknown child class of Executable " + executable.getClass());
    }

    @Nonnull
    private StringBuilder addInvokeArrow(@Nonnull final Type fromType, @Nonnull final Type toType, @Nonnull final Executable executable) {
        Objects.requireNonNull(fromType);
        Objects.requireNonNull(toType);

        StringBuilder uml = new StringBuilder();
        uml.append(fromType.getName());
        uml.append(" ");
        uml.append("-[dotted]-> ");
        uml.append(toType.getName());
        uml.append(" ");
        uml.append(": invokes " + executable.getName());
        uml.append("\n");
        return uml;
    }

    private void addCreateArrow(@Nonnull final ResultSet resultSet, @Nonnull final StringBuilder uml) {
        Objects.requireNonNull(resultSet);
        Objects.requireNonNull(uml);

        for (InvokeRelationship invokeRelationship : resultSet.getInvokeRelationships()) {
            final Type fromType = getDeclaringType(invokeRelationship.getInvokingExecutable(), resultSet);
            if (fromType == null) {
                LOG.warn("Could not find an HasConstructorRelationship or HasMethodRelationship for the InvokeRelationship {}. Ignoring it.", invokeRelationship);
                continue;
            }

            if (!(invokeRelationship.getInvokedExecutable() instanceof Constructor)) {
                continue;
            }
            final HasConstructorRelationship hasConstructorRelationship = resultSet.getHasConstructorRelationship((Constructor) invokeRelationship.getInvokedExecutable());
            if (hasConstructorRelationship == null) {
                LOG.warn("Could not find an HasConstructorRelationship for the InvokeRelationship {}. Ignoring it.", invokeRelationship);
                continue;
            }
            final Type toType = hasConstructorRelationship.getDeclaringType();

            uml.append(addCreateArrow(fromType, toType));
        }
    }

    @Nonnull
    private StringBuilder addCreateArrow(@Nonnull final Type fromType, @Nonnull final Type toType) {
        Objects.requireNonNull(fromType);
        Objects.requireNonNull(toType);

        StringBuilder uml = new StringBuilder();
        uml.append(fromType.getName());
        uml.append(" ");
        uml.append("-[plain]-> ");
        uml.append(toType.getName());
        uml.append(" ");
        uml.append(": creates");
        uml.append("\n");
        return uml;
    }

    private void addAccessArrow(@Nonnull final ResultSet resultSet, @Nonnull final StringBuilder uml) {
        Objects.requireNonNull(resultSet);
        Objects.requireNonNull(uml);

        for (AccessRelationship accessRelationship : resultSet.getAccessRelationships()) {
            final Type accessingType = getDeclaringType(accessRelationship.getAccessingExecutable(), resultSet);
            if (accessingType == null) {
                LOG.warn("Could not find an HasConstructorRelationship or HasMethodRelationship for the AccessRelationship {}. Ignoring it.", accessRelationship);
                continue;
            }

            final HasFieldRelationship hasFieldRelationship = resultSet.getHasFieldRelationship(accessRelationship.getField());
            if (hasFieldRelationship == null) {
                LOG.warn("Could not find an HasFieldRelationship for the AccessRelationship {}. Ignoring it.", accessRelationship);
                continue;
            }

            uml.append(addAccessArrow(accessingType, hasFieldRelationship.getDeclaringType(), accessRelationship.getField()));
        }
    }

    @Nonnull
    private StringBuilder addAccessArrow(@Nonnull final Type accessingType, @Nonnull final Type declaringType, @Nonnull final Field field) {
        StringBuilder uml = new StringBuilder();
        uml.append(accessingType.getName());
        uml.append(" ");
        uml.append("-[dashed]-> ");
        uml.append(declaringType.getName());
        uml.append(" ");
        uml.append(": accesses " + field.getName());
        uml.append("\n");
        return uml;
    }
}
