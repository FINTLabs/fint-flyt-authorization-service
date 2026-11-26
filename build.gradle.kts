buildscript {
    dependencies {
        constraints {
            classpath(libs.commons.lang3) {
                because("Fixes CVE-2025-48924 in spring-boot-buildpack-platform")
            }
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ktlint)
}

group = "no.novari"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// Dependabot resolves with JDK 21; an explicit release keeps JVM 25 as the resolution target so gradle.lockfile gets updated.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenLocal()
}

tasks.jar {
    isEnabled = false
}

dependencyLocking {
    lockAllConfigurations()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    // A platform instead of the dependency-management plugin, which puts Boot's Kotlin on ktlint's classpath.
    implementation(platform(libs.spring.boot.dependencies))
    mockitoAgent(platform(libs.spring.boot.dependencies))

    constraints {
        ktlint(libs.logback.core) {
            because(
                "ktlint 1.8.0 brings logback-core 1.3.16, which has CVE-2026-1225, CVE-2026-9828 and CVE-2026-10532",
            )
        }
    }

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.hibernate.orm:hibernate-envers")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    implementation(libs.kafka.starter)
    implementation(libs.flyt.security.starter)
    implementation(libs.flyt.audit.starter)
    implementation(libs.springdoc.openapi.webmvc.ui)
    implementation(libs.kotlin.logging)

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly(libs.logstash.logback.encoder)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation(libs.mockito.kotlin)
    // Not transitive, so singleFile is mockito-core. The platform still sets its version.
    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

ktlint {
    version = libs.versions.ktlint.cli
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgumentProviders.add(CommandLineArgumentProvider { listOf("-javaagent:${mockitoAgent.singleFile}") })
}

tasks.named("check") {
    dependsOn("ktlintCheck")
}
