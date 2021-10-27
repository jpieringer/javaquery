plugins {
    `java-gradle-plugin`
    `maven-publish`
}

val copyToJarConfiguration = configurations.create("copyToJarConfiguration")

dependencies {
    copyToJarConfiguration(project(path = ":javaquery-cli", configuration = "shadow"))
    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:3.9.0")
    testImplementation("org.assertj:assertj-core:3.20.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("javaquery-gradle-plugin") {
            id = "one.pieringer.javaquery.gradleplugin"
            implementationClass = "one.pieringer.javaquery.gradleplugin.JavaqueryPlugin"
        }
    }
}

tasks.jar {
    from(configurations.getByName("copyToJarConfiguration"))
}
