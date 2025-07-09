package dev.eliaschen.skillsstudyflowapi

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.web.multipart.support.MultipartFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint
) {

    @Value("\${app.cors.allowed-origins}")
    private lateinit var allowedOrigins: String

    @Value("\${app.cors.allowed-methods}")
    private lateinit var allowedMethods: String

    @Value("\${app.cors.allowed-headers}")
    private lateinit var allowedHeaders: String

    @Value("\${app.cors.allow-credentials}")
    private lateinit var allowCredentials: String

    @Value("\${app.cors.exposed-headers}")
    private lateinit var exposedHeaders: String

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authz ->
                authz
                    // Allow auth endpoints without authentication
                    .requestMatchers(
                        "/auth/signin",
                        "/auth/authenticate",
                        "/auth/signout"
                    ).permitAll()
                    // Allow Swagger UI and API docs
                    .requestMatchers(
                        "/ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                    ).permitAll()
                    // Allow health check and actuator endpoints (if any)
                    .requestMatchers(
                        "/actuator/**",
                        "/health"
                    ).permitAll()
                    // Require authentication for all other endpoints
                    .anyRequest().authenticated()
            }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        
        // Parse comma-separated origins
        val origins = allowedOrigins.split(",").map { it.trim() }
        configuration.allowedOrigins = origins
        
        // Also allow all origins for development (remove in production)
        configuration.allowedOriginPatterns = listOf("*")
        
        // Parse comma-separated methods
        val methods = allowedMethods.split(",").map { it.trim() }
        configuration.allowedMethods = methods
        
        // Parse comma-separated headers
        val headers = if (allowedHeaders == "*") {
            listOf("*")
        } else {
            allowedHeaders.split(",").map { it.trim() }
        }
        configuration.allowedHeaders = headers
        
        // Set credentials
        configuration.allowCredentials = allowCredentials.toBoolean()
        
        // Parse comma-separated exposed headers
        val exposed = exposedHeaders.split(",").map { it.trim() }
        configuration.exposedHeaders = exposed
        
        // Set max age for preflight requests (optional)
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun multipartFilter(): FilterRegistrationBean<MultipartFilter> {
        val registration = FilterRegistrationBean<MultipartFilter>()
        registration.filter = MultipartFilter()
        registration.order = -100 // Before security filters
        return registration
    }
}
