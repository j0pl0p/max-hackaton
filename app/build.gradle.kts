plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
}

group = "org.white_powerbank"
version = "unspecified"

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":bot"))
    implementation(project(":utils"))
    
    // MAX Bot SDK
    implementation("com.github.error404egor:max-bot-sdk-java:081fec8fd2")
    
    // Koin DI
    implementation("io.insert-koin:koin-core:3.5.3")
    
    // Configuration
    implementation("com.typesafe:config:1.4.2")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
    
    // Coroutines
    implementation(libs.kotlinxCoroutines)
    
    testImplementation(kotlin("test"))
}

application {
    mainClass = "org.white_powerbank.app.MainKt"
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}