package dev.eliaschen.skillsstudyflowapi

import org.springdoc.core.properties.SwaggerUiConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.ApplicationRunner
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.streams.toList

@SpringBootApplication
class SkillsStudyflowApiApplication {
    private val logger = LoggerFactory.getLogger(SkillsStudyflowApiApplication::class.java)

    @Bean
    fun cleanupFilesOnStartup() = ApplicationRunner {
        logger.info("Starting cleanup of uploaded files...")
        
        val recordsDir: Path = Paths.get("records").toAbsolutePath()
        val screenshotsDir: Path = Paths.get("screenshots").toAbsolutePath()
        
        // Clean up records directory
        if (recordsDir.exists()) {
            try {
                val recordFiles = Files.list(recordsDir)
                    .filter { Files.isRegularFile(it) }
                    .toList()
                
                recordFiles.forEach { file ->
                    try {
                        file.deleteIfExists()
                        logger.info("Deleted record file: ${file.fileName}")
                    } catch (ex: Exception) {
                        logger.error("Failed to delete record file ${file.fileName}: ${ex.message}")
                    }
                }
                
                logger.info("Cleaned up ${recordFiles.size} record files")
            } catch (ex: Exception) {
                logger.error("Error accessing records directory: ${ex.message}")
            }
        }
        
        // Clean up screenshots directory
        if (screenshotsDir.exists()) {
            try {
                val screenshotFiles = Files.list(screenshotsDir)
                    .filter { Files.isRegularFile(it) }
                    .toList()
                
                screenshotFiles.forEach { file ->
                    try {
                        file.deleteIfExists()
                        logger.info("Deleted screenshot file: ${file.fileName}")
                    } catch (ex: Exception) {
                        logger.error("Failed to delete screenshot file ${file.fileName}: ${ex.message}")
                    }
                }
                
                logger.info("Cleaned up ${screenshotFiles.size} screenshot files")
            } catch (ex: Exception) {
                logger.error("Error accessing screenshots directory: ${ex.message}")
            }
        }
        
        logger.info("File cleanup completed")
    }
}

fun main(args: Array<String>) {
    runApplication<SkillsStudyflowApiApplication>(*args)
}
