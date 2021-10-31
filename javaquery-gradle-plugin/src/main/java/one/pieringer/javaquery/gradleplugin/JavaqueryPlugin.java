package one.pieringer.javaquery.gradleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;

import javax.annotation.Nonnull;
import java.util.Objects;

public class JavaqueryPlugin implements Plugin<Project> {

    private static final String EXTENSION_NAME = "javaquery";
    private static final String JAVAQUERY_TASK_PREFIX = "javaquery";
    private static final String JAVAQUERY_TASK_CLEAN = JAVAQUERY_TASK_PREFIX + "Clean";

    @Override
    public void apply(@Nonnull final Project project) {
        Objects.requireNonNull(project);

        final JavaqueryExtension extension = createExtension(project);

        registerJavaqueryTask(project, extension);
        registerJavaqueryCleanTask(project, extension);
    }

    private void registerJavaqueryCleanTask(@Nonnull final Project project,
                                            @Nonnull final JavaqueryExtension extension) {
        project.getPlugins()
                .withType(JavaBasePlugin.class)
                .configureEach(javaBasePlugin -> project.getTasks()
                        .register(JAVAQUERY_TASK_CLEAN,
                                JavaqueryCleanTask.class,
                                task -> task.init(extension)));
    }

    private void registerJavaqueryTask(@Nonnull final Project project,
                                       @Nonnull final JavaqueryExtension extension) {
        project.getPlugins()
                .withType(JavaBasePlugin.class)
                .configureEach(javaBasePlugin -> project.getConvention().
                        getPlugin(JavaPluginConvention.class)
                        .getSourceSets()
                        .all(sourceSet -> {
                            String name = sourceSet.getTaskName(JAVAQUERY_TASK_PREFIX, null);
                            project.getTasks()
                                    .register(name,
                                            JavaqueryTask.class,
                                            task -> {
                                                task.init(extension);
                                                task.setSourceDirs(sourceSet.getAllSource().getSourceDirectories());
                                                task.setAuxClassPaths(sourceSet.getCompileClasspath());
                                            });
                        }));
    }

    @Nonnull
    private JavaqueryExtension createExtension(@Nonnull final Project project) {
        return Objects.requireNonNull(project.getExtensions().create(EXTENSION_NAME, JavaqueryExtension.class, project.getObjects()));
    }
}
