plugins {
    id("com.gradleup.shadow") version "8.3.6" // Import shadow API.
    java // Tell gradle this is a java project.
    eclipse // Import eclipse plugin for IDE integration.
}

java {
    // Declare java version.
    sourceCompatibility = JavaVersion.VERSION_17
}

group = "me.bribedjupiter" // From groupId in pom.xml
version = "0.3" // Declare plugin version (will be in .jar).
val apiVersion = "1.19" // Declare Minecraft server target version.

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to version,
        "apiVersion" to apiVersion
    )

    inputs.properties(props) // Indicates to rerun if version changes.

    filesMatching("plugin.yml") {
        expand(props)
    }
    from("LICENSE") { // Bundle license into .jars.
        into("/")
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://repo.purpurmc.org/snapshots")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        url = uri("https://repo.essentialsx.net/releases/")
    }
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT") // Purpur API
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3") // MiniPlaceholders API
    compileOnly("net.essentialsx:EssentialsX:2.19.0") {
        exclude(group = "org.bstats", module = "bstats-bukkit")
    }
    compileOnly(project(":libs:Utilities-OG"))
    compileOnly(project(":libs:GxUI-OG"))
    compileOnly(project(":libs:DiamondBank-OG"))
}

tasks.withType<AbstractArchiveTask>().configureEach { // Ensure reproducible .jars
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    exclude("io.github.miniplaceholders.*") // Exclude the MiniPlaceholders package from being shadowed.
    archiveClassifier.set("") // Use empty string instead of null.
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    archiveClassifier.set("part")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:deprecation") // Triggers deprecation warning messages.
    options.encoding = "UTF-8"
    options.isFork = true
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}
