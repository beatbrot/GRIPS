val gripsRealm = project.findProperty("gripsRealm") ?: ""
val gripsUser = project.findProperty("gripsUser") ?: ""
val gripsPW = project.findProperty("gripsPW") ?: ""

group = "de.beatbrot"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.21"
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jsoup", "jsoup", "1.12.1")

    testImplementation("io.kotlintest", "kotlintest-runner-junit5", "3.3.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    environment("GRIPS_REALM", gripsRealm)
    environment("GRIPS_USER", gripsUser)
    environment("GRIPS_PW", gripsPW)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])

            repositories {
                maven {
                    mavenLocal()
                }
            }
        }
    }
}