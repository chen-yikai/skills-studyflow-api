package dev.eliaschen.skillsstudyflowapi

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorsFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(CorsFilter::class.java)

    @Value("\${app.cors.allowed-origins}")
    private lateinit var allowedOrigins: String

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val origin = request.getHeader("Origin")
        
        logger.debug("CORS Filter - Origin: $origin, Method: ${request.method}, URI: ${request.requestURI}")
        
        // Determine allowed origin
        val allowedOriginsList = allowedOrigins.split(",").map { it.trim() }
        val allowedOrigin = when {
            origin != null && allowedOriginsList.contains(origin) -> origin
            origin != null && (origin.contains("localhost") || origin.contains("127.0.0.1") || origin.contains("eliaschen.dev")) -> origin
            else -> "*"
        }
        
        // Set CORS headers
        response.setHeader("Access-Control-Allow-Origin", allowedOrigin)
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH")
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, Origin, User-Agent, Cache-Control, key, X-Requested-With")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Type, Accept, Origin")
        response.setHeader("Access-Control-Max-Age", "3600")
        
        logger.debug("CORS Filter - Set Access-Control-Allow-Origin: $allowedOrigin")
        
        // Handle preflight requests
        if ("OPTIONS" == request.method) {
            logger.debug("CORS Filter - Handling preflight request")
            response.status = HttpServletResponse.SC_OK
            return
        }
        
        filterChain.doFilter(request, response)
    }
}
