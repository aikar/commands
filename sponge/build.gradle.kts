/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("co.aikar.java-conventions")
}

dependencies {
    api(project(":acf-core"))
    compileOnly("org.spongepowered:spongeapi:5.1.0")
}

description = "ACF (Sponge)"

tasks {
  processResources {
    from("${project.projectDir}/../languages/minecraft/")
  }
}