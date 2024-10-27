// This is the shared configuration for the entire project.

rootProject.name = "brava"
include("core")
include("brava-jackson-module")

val jacksonVersion = "2.17.2"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("guava", "com.google.guava:guava:33.3.1-jre")
            library("jetbrains-annotations", "org.jetbrains:annotations:26.0.1")
            library("junit-bom", "org.junit:junit-bom:5.11.3")
            library("assertj-bom", "org.assertj:assertj-bom:3.26.3")
            library("jackson-bom", "com.fasterxml.jackson:jackson-bom:$jacksonVersion")
            library("jackson-annotations", "com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
            library("lombok", "org.projectlombok:lombok:1.18.34")
        }
    }
}

//include("testing")
