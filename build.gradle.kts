plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

// Version of project
val projectVersion = "1.4.0"
// Where this builds on the server
val serverLocation = "Skript/1-21-11"
// Minecraft version to build against
val minecraftVersion = "1.21.11"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    mavenLocal()

    // Paper
    maven("https://repo.papermc.io/repository/maven-public/")

    // Skript
    maven("https://repo.skriptlang.org/releases")

    // JitPack
    maven("https://jitpack.io")
}

dependencies {
    // Paper
    paperweight.paperDevBundle("${minecraftVersion}-R0.1-SNAPSHOT")

    // Skript
    compileOnly("com.github.SkriptLang:Skript:2.10.2")

    // SkBee
    compileOnly("com.github.ShaneBeee:SkBee:3.8.0")

    // bStats
    implementation("org.bstats:bstats-bukkit:3.1.0")
}

tasks {
    register("server", Copy::class) {
        dependsOn("shadowJar")
        from("build/libs") {
            include("SkNMS-*.jar")
            destinationDir = file("/Users/ShaneBee/Desktop/Server/${serverLocation}/plugins/")
        }
    }
    processResources {
        expand("version" to projectVersion, "minecraft" to minecraftVersion.split("-")[0])
    }
    compileJava {
        options.release = 21
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
