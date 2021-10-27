package one.pieringer.javaquery.gradleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JavaqueryPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project target) {
        System.out.println("Applying to " + target.getDisplayName());
    }
}
