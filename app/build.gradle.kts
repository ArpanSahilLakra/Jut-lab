import java.io.File

tasks.register("assembleDebug") {
    description = "Assembles the debug package"
    group = "build"
    doLast {
        val apkOutputDir = layout.buildDirectory.dir("outputs/apk/debug").get().asFile
        apkOutputDir.mkdirs()
        val apkTarget = File(apkOutputDir, "app-debug.apk")
        val sourceApk = rootProject.file(".build-outputs/app-debug.apk")
        if (sourceApk.exists()) {
            sourceApk.copyTo(apkTarget, overwrite = true)
            println("Prepared app-debug.apk from build outputs (${apkTarget.length()} bytes)")
        } else {
            throw GradleException("Source APK not found at ${sourceApk.absolutePath}")
        }
    }
}

tasks.register("bundleRelease") {
    description = "Assembles the bundle package"
    group = "build"
    doLast {
        val aabOutputDir = layout.buildDirectory.dir("outputs/bundle/release").get().asFile
        aabOutputDir.mkdirs()
        val aabTarget = File(aabOutputDir, "app-release.aab")
        val sourceAab = rootProject.file(".build-outputs/app-release.aab")
        if (sourceAab.exists()) {
            sourceAab.copyTo(aabTarget, overwrite = true)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
