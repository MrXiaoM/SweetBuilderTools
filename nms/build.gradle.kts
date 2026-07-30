import top.mrxiaom.gradle.LibraryHelper

plugins {
    java
}

val base = rootProject.extra["base"] as LibraryHelper

subprojects {
    apply(plugin="java")
}
allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://jitpack.io")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    configure<JavaPluginExtension> {
        disableAutoTargetJvm()
    }
    dependencies {
        add("compileOnly", "org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    }
}
subprojects {
    val targetJavaVersion = project.extra.get("targetJavaVersion").toString().toInt()
    dependencies {
        if (project.name != "shared") {
            add("compileOnly", project(":nms:shared"))
        }
    }
    tasks.withType<JavaCompile>().configureEach {
        options.release = targetJavaVersion
    }
}
dependencies {
    compileOnly(base.modules.actions)
    compileOnly(base.depend.annotations)
    compileOnly("de.tr7zw:item-nbt-api:2.16.0")
    for (module in subprojects) {
        compileOnly(module)
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.release = 8
}
