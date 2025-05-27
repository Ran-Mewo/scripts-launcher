package com.github.ranmewo.scriptslauncher.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import kotlin.random.Random

@Service(Service.Level.PROJECT)
class MyProjectService(project: Project) {

    init {
        thisLogger().info("Scripts Launcher service initialized for project: ${project.name}")
    }
    
    fun getRandomNumber(): Int {
        return Random.nextInt()
    }
}
