import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.palantir.docker") version "0.22.1"
}

sourceSets {
    create("integrationTest") {
        java {
            compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
            runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
        }
    }
}

dependencies {
    implementation("org.eclipse.jdt:org.eclipse.jdt.core:3.26.0")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("commons-io:commons-io:2.7")
    implementation("org.apache.commons:commons-lang3:3.10")
    // Neo4J embedded
    implementation("org.neo4j:neo4j:4.0.6") {
        exclude("org.slf4j", "slf4j-nop")
    }
    implementation("org.apache.logging.log4j:log4j-api:2.13.3")
    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")
    // Neo4J driver
    implementation("org.neo4j:neo4j-ogm-core:3.2.16")
    implementation("org.neo4j:neo4j-ogm-bolt-driver:3.2.16")
    implementation("org.neo4j:neo4j-ogm-bolt-native-types:3.2.16")
    // PlantUml
    implementation(files("libs/plantuml-1.2021.9.jar"))
    // PDF Export
    implementation("org.apache.xmlgraphics:batik:1.13")
    implementation("org.apache.xmlgraphics:fop-transcoder:2.5")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.8")
    implementation("commons-cli:commons-cli:1.4")
    implementation("com.google.guava:guava:30.1.1-jre")

    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:3.9.0")
    testImplementation("org.assertj:assertj-core:3.20.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveFileName.set("javaquery-cli-full.jar")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "one.pieringer.javaquery.Main"))
        }
    }

    build {
        dependsOn(shadowJar)
    }


    val integrationTest = register<Test>("integrationTest") {
        description = "Runs the integration tests"
        group = "verification"
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        mustRunAfter(test)

        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    check {
        dependsOn(integrationTest)
    }

    named<DefaultTask>("dockerPrepare") {
        dependsOn(build)
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    create<DefaultTask>("printClasspath") {
        doLast {
            println("Project Source")
            sourceSets.main.get().java.srcDirs.forEach {
                println(it.absolutePath.replace("\\build\\classes\\java\\main", "\\src\\main\\java"))
            }

            println("Dependencies via compileClasspath")
            configurations.compileClasspath.get().resolve().forEach {
                println(it.absolutePath.replace("\\build\\classes\\java\\main", "\\src\\main\\java"))
            }

            println("Dependencies via resolution result")
            val dependencies = HashSet<File>()
            dependencies.addAll(sourceSets.main.get().java.srcDirs)
            configurations.compileClasspath.get().incoming.resolutionResult.allDependencies.forEach { dependency ->
                when (val requested = dependency.requested) {
                    is ProjectComponentSelector -> {
                        dependencies.addAll(project.rootProject.project(requested.displayName.substring(8)).sourceSets.main.get().java.srcDirs)
                    }
                    is ModuleComponentSelector -> {
                        val path = configurations.compileClasspath.get().files { dependencyUnderTest ->
                            dependencyUnderTest.group == requested.group &&
                                    dependencyUnderTest.name == requested.module &&
                                    dependencyUnderTest.version == requested.version
                        }
                        dependencies.addAll(path)
                    }
                    else -> {
                        println(" -- None")
                    }
                }
            }
            dependencies.forEach { singleArtifact -> println(singleArtifact) }
        }
    }
}

docker {
    name = "piri/javaquery:${project.version}"
    copySpec.from("build/libs/javaquery-full.jar").into("build/libs/javaquery-full.jar")
    tag("DockerHub", "piri/javaquery:${project.version}")
}
