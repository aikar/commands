/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("co.aikar.java-conventions")
}

description = "ACF (Core)"

dependencies {
    api("co.aikar:Table:1.0.0-SNAPSHOT")
    api("co.aikar:locales:1.0-SNAPSHOT")
    api("net.jodah:expiringmap:0.5.9")
}

tasks {
  processResources {
    from("${project.projectDir}/../languages/core/")
  }
}