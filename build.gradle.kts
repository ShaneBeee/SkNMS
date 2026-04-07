plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" // the latest version can be found on the Gradle Plugin Portal
}

// Version of project
val projectVersion = "1.5.0"
// Where this builds on the server
val serverLocation = "Skript/26-1"
// Minecraft version to build against
val minecraftVersion = "26.1.1"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

repositories {
    mavenCentral()
    mavenLocal()

    // Paper
    maven("https://repo.papermc.io/repository/maven-public/")

    // Skript
    maven("https://repo.skriptlang.org/releases")

    // JitPack
    maven("https://jitpack.io") {
        metadataSources {
            mavenPom()
            artifact()
            // This ignores the .module file and forces Gradle to use the POM/JAR
        }
    }
}

dependencies {
    // Paper
    paperweight.paperDevBundle("${minecraftVersion}.build.+")

    // Skript
    compileOnly("com.github.SkriptLang:Skript:2.14.1")

    // SkBee
    compileOnly("com.github.ShaneBeee:SkBee:3.17.1@jar") // Forcing jar as it wasn't downloading

    // bStats
    implementation("org.bstats:bstats-bukkit:3.1.0")
}

tasks {
    register("server", Copy::class) {
        dependsOn("shadowJar")
        from("build/libs") {
            include("SkNMS-*.jar")
            destinationDir = file("/Users/ShaneBee/Desktop/Server/Minecraft/${serverLocation}/plugins/")
        }
    }
    processResources {
        expand("version" to projectVersion, "minecraft" to minecraftVersion.split("-")[0])
    }
    compileJava {
        options.release = 25
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
    shadowJar {
        archiveFileName.set("SkNMS-$projectVersion-$minecraftVersion.jar")
        relocate("org.bstats", "com.shanebeestudios.nms.metrics")
    }
    jar {
        dependsOn(shadowJar)
    }
}
