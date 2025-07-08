package dev.eliaschen.skillsstudyflowapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val bearerSchemeName = "bearerAuth"
        val apiKeySchemeName = "key"
        
        return OpenAPI()
            .info(
                Info()
                    .title("Skills Study Flow API")
                    .description("API for managing study records with authentication")
                    .version("1.0.0")
            )
            .addSecurityItem(
                SecurityRequirement().addList(bearerSchemeName)
            )
            .addSecurityItem(
                SecurityRequirement().addList(apiKeySchemeName)
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        bearerSchemeName,
                        SecurityScheme()
                            .name(bearerSchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter the JWT token obtained from /auth/authenticate endpoint")
                    )
                    .addSecuritySchemes(
                        apiKeySchemeName,
                        SecurityScheme()
                            .name("key")
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.HEADER)
                            .description("Enter 'key' for easy testing")
                    )
            )
    }
}
