plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

application {
    mainClass.set("app.ApplicationKt")

    ktlint {
        ignoreFailures.set(true)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-gson")
    testImplementation("io.ktor:ktor-server-tests")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("at.favre.lib:bcrypt:0.9.0")
}
