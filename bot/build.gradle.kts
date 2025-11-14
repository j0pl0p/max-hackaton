plugins {
    id("buildsrc.convention.kotlin-jvm")
}

group = "org.white_powerbank"
version = "unspecified"

dependencies {
    implementation(project(":domain"))
    implementation(project(":utils"))
    implementation("com.github.error404egor:max-bot-sdk-java:081fec8fd2")
    implementation(libs.kotlinxCoroutines)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}