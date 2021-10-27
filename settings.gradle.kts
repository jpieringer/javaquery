rootProject.name = "javaquery"
include("javaquery-cli")
include("javaquery-gradle-plugin")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven(url = "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/")
    }
}
