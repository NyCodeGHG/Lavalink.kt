import org.ajoberstar.gradle.git.publish.GitPublishExtension

plugins {
    `maven-publish`
    kotlin("multiplatform") version Versions.kotlin apply false
    kotlin("plugin.serialization") version Versions.kotlin apply false
    id("org.jetbrains.dokka") version "1.5.0"
    id("kotlinx-atomicfu") version Versions.atomicFu apply false
    id("org.ajoberstar.git-publish") version "2.1.3"
}

group = "dev.schlaubi.lavakord"
version = "2.1.0"

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }
}

tasks {
    dokkaHtmlMultiModule {
        outputDirectory.set(rootProject.file("docs"))
    }

    val docs = task<Copy>("createDocsIndex") {
        dependsOn(dokkaHtmlMultiModule)
        val outputDirectory = dokkaHtmlMultiModule.get().outputDirectory.get()
        from(outputDirectory, rootProject.projectDir)
        include("CNAME", "-modules.html")
        into(outputDirectory)

        rename("-modules.html", "index.html")
    }

    gitPublishPush {
        dependsOn(docs)
    }
}

configure<GitPublishExtension> {
    repoUri.set("https://github.com/DRSchlaubi/lavakord.git")
    branch.set("gh-pages")

    contents {
        from(file("docs"))
    }

    commitMessage.set("Update Docs")
}

subprojects {
    group = rootProject.group

    tasks {
        withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
            dokkaSourceSets {
                configureEach {
                    includeNonPublic.set(false)

                    perPackageOption {
                        matchingRegex.set(".*\\.internal.*") // will match all .internal packages and sub-packages
                        suppress.set(true)
                    }
                }

                if (asMap.containsKey("jsMain")) {
                    named("jsMain") {
                        displayName.set("JS")
                    }
                }

                if (asMap.containsKey("jvmMain")) {
                    named("jvmMain") {
                        jdkVersion.set(8)
                        displayName.set("JVM")
                    }
                }
            }
        }
    }
}
