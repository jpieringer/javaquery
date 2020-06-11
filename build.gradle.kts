plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.16.1")
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
    implementation("org.neo4j:neo4j-ogm-core:3.2.13")
    implementation("org.neo4j:neo4j-ogm-bolt-driver:3.2.13")
    implementation("org.neo4j:neo4j-ogm-bolt-native-types:3.2.13")

    implementation(files("libs/plantuml-1.2020.14.jar"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.8")
    implementation("commons-cli:commons-cli:1.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
}
