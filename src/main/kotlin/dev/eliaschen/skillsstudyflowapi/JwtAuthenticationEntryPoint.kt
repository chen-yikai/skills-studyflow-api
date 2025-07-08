package dev.eliaschen.skillsstudyflowapi

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint::class.java)
    private val objectMapper = ObjectMapper()

    @Throws(IOException::class, ServletException::class)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.warn("Unauthorized access attempt to: ${request.requestURI}")
        
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        
        val errorResponse = mapOf(
            "error" to "Unauthorized",
            "message" to "Authentication required. Please provide a valid Bearer token.",
            "path" to request.requestURI,
            "status" to 401
        )
        
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
