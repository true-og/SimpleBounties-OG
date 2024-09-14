rootProject.name = "SimpleBounties-OG"

// Execute bootstrap.sh
exec {
    workingDir(rootDir)
    commandLine("sh", "bootstrap.sh")
}

include("libs:DiamondBank-OG")
