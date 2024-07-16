plugins {
    id("java-library")
}

group = "brava"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jetbrains.annotations)
    api(libs.guava)
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.assertj)
}

tasks.test {
    useJUnitPlatform()
}