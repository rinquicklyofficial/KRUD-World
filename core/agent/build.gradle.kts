plugins {
    java
}

tasks.jar {
    manifest.attributes(
        "Agent-Class" to "dev.krud.world.util.agent.Installer",
        "Premain-Class" to "dev.krud.world.util.agent.Installer",
        "Can-Redefine-Classes" to true,
        "Can-Retransform-Classes" to true
    )
}