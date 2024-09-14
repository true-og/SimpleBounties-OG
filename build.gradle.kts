plugins {
    java // Tell gradle this is a java project.
    id("io.github.goooler.shadow") version "8.1.8"
    eclipse // Import eclipse plugin for IDE integration.
}

group = "me.bribedjupiter" // From groupId in pom.xml
version = "1.0-OG" // From version in pom.xml
val apiVersion = "1.19" // Declare Minecraft server target version.

// Java version set to 1.8 to match pom.xml
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to version,
        "apiVersion" to apiVersion
    )

    inputs.properties(props) // Indicates to rerun if version changes.

    filesMatching("plugin.yml") {
        expand(props)
    }
}

repositories {
    mavenCentral()

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
    implementation(project(":libs:DiamondBank-OG"))
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    exclude("io.github.miniplaceholders.*") // Exclude the MiniPlaceholders package from being shadowed.
    minimize()
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("part")
}

tasks.shadowJar {
    archiveClassifier.set("") // Use empty string instead of null
    from("LICENSE") {
        into("/")
    }
}

tasks.jar {
    dependsOn("shadowJar")
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
