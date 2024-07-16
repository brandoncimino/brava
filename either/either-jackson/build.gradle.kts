plugins {
    id("java-library")
    id("java-test-fixtures")
}

group = "brava"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation(libs.guava)
    implementation(libs.jetbrains.annotations)
    api(project(":either"))
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.assertj)
    testImplementation(testFixtures(project(":either")))
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

tasks.test {
    useJUnitPlatform()
}