plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    id("war")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.1"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.jetty)

    compileOnly(libs.jakarta.servlet.api)

    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)

    testImplementation(libs.spring.boot.starter.test)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    launchScript()
}
