package dev.eliaschen.skillsstudyflowapi

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    @Value("\${app.auth.jwt.secret}")
    private lateinit var jwtSecret: String

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")
            val apiKeyHeader = request.getHeader("key")
            
            // Check for API key authentication first (for easy testing)
            if (apiKeyHeader != null && apiKeyHeader == "key") {
                // Create a test user for API key authentication
                val userDetails: UserDetails = User.builder()
                    .username("test-user")
                    .password("") // Not needed for API key authentication
                    .authorities(emptyList())
                    .build()
                
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                
                SecurityContextHolder.getContext().authentication = authentication
                logger.debug("API key authentication successful")
                
            } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)
                
                try {
                    val userInfo = verifyJwtToken(token)
                    
                    // Create UserDetails from the JWT claims
                    val userDetails: UserDetails = User.builder()
                        .username(userInfo.username)
                        .password("") // Not needed for JWT authentication
                        .authorities(emptyList())
                        .build()
                    
                    // Create authentication token
                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.authorities
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().authentication = authentication
                    
                    logger.debug("JWT token validated successfully for user: ${userInfo.username}")
                    
                } catch (e: Exception) {
                    logger.warn("JWT token validation failed: ${e.message}")
                    SecurityContextHolder.clearContext()
                }
            }
            
        } catch (e: Exception) {
            logger.error("Error processing authentication: ${e.message}")
            SecurityContextHolder.clearContext()
        }
        
        filterChain.doFilter(request, response)
    }

    private fun verifyJwtToken(token: String): UserInfo {
        val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return UserInfo(
            id = claims.subject,
            username = claims["username"] as String,
            email = claims["email"] as String?
        )
    }
}
