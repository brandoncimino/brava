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
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation(libs.guava)
    implementation(libs.jetbrains.annotations)
    api(project(":core"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(platform(libs.assertj.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation(testFixtures(project(":core")))
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}