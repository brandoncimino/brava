plugins {
    id("java-library")
    id("java-test-fixtures")
}

group = "brava"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation(libs.jetbrains.annotations)
    api(project(":core"))

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.assertj)
    testImplementation("com.fasterxml.jackson.core:jackson-databind")

    testFixturesImplementation(libs.assertj)
}

tasks.test {
    useJUnitPlatform()
}