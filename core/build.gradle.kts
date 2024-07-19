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
    implementation(libs.jetbrains.annotations)
    compileOnly(libs.jackson.annotations)
    api(libs.guava)

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testFixturesImplementation(libs.assertj)
}

tasks.test {
    useJUnitPlatform()
}