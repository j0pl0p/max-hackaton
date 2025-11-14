plugins {
    id("buildsrc.convention.kotlin-jvm")
}

group = "org.white_powerbank"
version = "unspecified"

dependencies {
    implementation(libs.kotlinxDatetime)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}