// 📎 All of these "blocks" are actually method calls with a single lambda parameter.
plugins {
    // 📎 These lambdas have a secret parameter, which can be referred to as `this`.
    //    Everything inside of lambda is actually called as though it were `this.{x}`.
    id("java-library")
    id("java-test-fixtures")
    id("maven-publish")
    // TODO: Inclusion of `jreleaser` seems to cause the https://docs.gradle.org/8.5/userguide/upgrading_version_8.html#deprecated_access_to_conventions warning. Need to look up and see if this is a known issue, which it better be if `jrleaser` is a real thing.
    id("org.jreleaser").version("1.14.0")
    id("signing")
}


/**
 * 📎 On the "Maven Central" website (which is actually called "sonatype"?),
 * the `groupId` is referred to as the `namespace`: https://central.sonatype.com/publishing/namespaces
 */
val githubUsername = "brandoncimino"
val mavenGroupId = "io.github.$githubUsername"
val mavenArtifactId = "brave-core"
val mavenDescription = "Brandon's generic Java utilities."

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

dependencies {
    implementation(libs.jetbrains.annotations)
    compileOnly(libs.jackson.annotations)
    api(libs.guava)

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform(libs.assertj.bom))
    testImplementation("org.assertj:assertj-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testFixturesImplementation(platform(libs.assertj.bom))
    testFixturesImplementation("org.assertj:assertj-core")
}

tasks.jar {
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier = ""
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    // This tells the "javadoclet" to allow the tags introduced in https://openjdk.org/jeps/8068562. 
    // Source: https://gist.github.com/claudioaltamura/aba1f6506a53b9f5499fd507abd572df
    (options as StandardJavadocDocletOptions).tags(
        "apiNote:a:API Note:",
        "implSpec:a:Implementation Requirements:",
        "implNote:a:Implementation Note:"
    )

    // This tells the "javadoclet" to stop failing my build if it detects and warnings.
    // Source: https://stackoverflow.com/a/73930431/18494923
    // 📎 In a perfect world, we'd be able to just fix these issues. 
    //    However, some of these warnings are just objectively incorrect, the most notable being
    //    the `reference not found` warning issued when you use a type parameter in a `@link`, such as `{@link Function#apply(IN)}`.
    // 📎 Some people reference another option, `quiet` / `-quiet`, but it doesn't seem to do anything.
    (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true);
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
    gitRootSearch = true
    signing {
        active = org.jreleaser.model.Active.ALWAYS
        armored = true
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = org.jreleaser.model.Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("target/staging-deploy")
                }
            }
        }
    }
}
