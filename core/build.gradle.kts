import org.jreleaser.model.Active

plugins {
    id("java-library")
    id("java-test-fixtures")
    id("maven-publish")
    id("org.jreleaser").version("1.13.1")
    id("signing")
}

/**
 * 📎 On the "Maven Central" website (which is actually called "sonatype"?),
 * the `groupId` is referred to as the `namespace`: https://central.sonatype.com/publishing/namespaces
 */
val mavenGroupId = "io.github.brandoncimino"
val mavenArtifactId = "brave-core"
val mavenDescription = "Brandon's generic Java utilities."

val githubUsername = "brandoncimino"
val githubProfile = "https://github.com/$githubUsername"
val githubProject = "https://github.com/$githubUsername/brava"

group = "brava"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.jar {
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier = ""
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

publishing {
    publications {
        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = mavenGroupId
            artifactId = mavenArtifactId
            // 📎 `description` and `url` are *required* by Maven Central: https://central.sonatype.org/publish/requirements/#project-name-description-and-url
            // That may or may not actually refer to the `pom` object below...
            // Either way, there is no `url` property here, so...I guess I won't set it 🤷‍♀️
            description = mavenDescription
        }
        withType<MavenPublication> {
            // This looks to basically be a 1:1 representation of a maven `pom.xml` file, but using Kotlin object initializers. 
            pom {
                packaging = "jar"
                name = "core"
                // 📎 `description` and `url` are *required* by Maven Central: https://central.sonatype.org/publish/requirements/#project-name-description-and-url
                description = mavenDescription
                url = githubProject
                licenses {
                    license {
                        name = "MIT license"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = githubUsername
                        name = "Brandon Cimino"
                        email = "brandon.cimino@gmail.com"
                        // The example on Maven Central's website also includes "organization", but that seems suspect
                        organizationUrl = "https://github.com/brandoncimino"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/brandoncimino/brava.git"
                    developerConnection = "scm:git:ssh:git@github.com:brandoncimino/brava.git"
                    url = githubProject
                }
            }
        }
    }
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

jreleaser {
    signing {
        active = Active.ALWAYS
        armored = true
    }
    deploy {
        maven {
            nexus2 {
                // ⚠ The call to `create()` was missing from the official documentation on https://jreleaser.org/guide/latest/examples/maven/maven-central.html#_gradle
                // I pulled it from: https://dev.to/tschuehly/how-to-publish-a-kotlinjava-spring-boot-library-with-gradle-to-maven-central-complete-guide-402a#52-configure-jreleaser-maven-plugin
                create("maven-central") {
                    active = Active.ALWAYS
                    url = "https://s01.oss.sonatype.org/service/local"
                    // 📎 `closeRepository` and `releaseRepository` seem both to be related to "release versions" as opposed to "snapshot versions".
                    closeRepository = false
                    releaseRepository = false
                    stagingRepositories.add("build/staging-deploy")
                }
            }
        }
    }
}