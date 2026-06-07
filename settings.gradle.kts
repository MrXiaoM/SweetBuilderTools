rootProject.name = "SweetBuilderTools"

include(":nms")
file("nms").listFiles()?.forEach { folder ->
    if (folder.resolve("build.gradle.kts").exists()) {
        include(":nms:${folder.name}")
    }
}

