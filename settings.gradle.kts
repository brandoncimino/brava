// This is the shared configuration for the entire project.

rootProject.name = "brava"
include("either")
include("core")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("guava", "com.google.guava:guava:32.0.1-jre")
            library("jetbrains-annotations", "org.jetbrains:annotations:22.0.0")
            library("junit-bom", "org.junit:junit-bom:5.10.0")
            library("assertj", "org.assertj:assertj-core:3.26.3")
            library("jackson-bom", "com.fasterxml.jackson:jackson-bom:2.17.2")
            library("lombok", "org.projectlombok:lombok:1.18.34")
        }
    }
}
include("either:either-jackson")
findProject(":either:either-jackson")?.name = "either-jackson"
