package dev.eliaschen.skillsstudyflowapi

import org.springdoc.core.properties.SwaggerUiConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class SkillsStudyflowApiApplication

fun main(args: Array<String>) {
    runApplication<SkillsStudyflowApiApplication>(*args)
}