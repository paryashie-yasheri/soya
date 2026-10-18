plugins {
    alias(libs.plugins.kotlin.jvm)
    id("war")
}

dependencies {
    compileOnly(libs.jakarta.ws.rs.api)
    compileOnly(libs.jakarta.servlet.api)

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
