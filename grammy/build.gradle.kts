plugins {
    alias(libs.plugins.kotlin.jvm)
    id("war")
}

dependencies {
    compileOnly(libs.jakarta.ws.rs.api)
    compileOnly(libs.jakarta.servlet.api)
    compileOnly(libs.jakarta.cdi.api)
    compileOnly(libs.jakarta.persistence.api)
    compileOnly("jakarta.transaction:jakarta.transaction-api:2.0.1")
    compileOnly("org.eclipse.microprofile.config:microprofile-config-api:3.0.3")

    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.core)
    runtimeOnly(libs.jersey.media.json.jackson)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
