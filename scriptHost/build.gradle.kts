plugins {
    kotlin("jvm")
    idea
}

val kotlinVersion: String by rootProject.extra

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    api(project(":script"))

    api("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")

    kotlinScriptDef(project(":script"))
    testKotlinScriptDef(project(":script"))
}
