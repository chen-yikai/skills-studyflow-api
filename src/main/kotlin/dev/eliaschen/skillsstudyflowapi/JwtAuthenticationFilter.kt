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
    
    private var isInitialized = false
    
    private fun ensureInitialized() {
        if (!isInitialized) {
            logger.info("JWT Secret initialized with length: ${jwtSecret.length}")
            isInitialized = true
        }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            ensureInitialized()
            
            val authHeader = request.getHeader("Authorization")
            val apiKeyHeader = request.getHeader("key")
            
            logger.debug("Processing authentication for: ${request.requestURI}")
            logger.debug("Authorization header present: ${authHeader != null}")
            logger.debug("API key header present: ${apiKeyHeader != null}")
            
            // Check for API key authentication first (for easy testing)
            if (apiKeyHeader != null && apiKeyHeader == "key") {
                logger.info("Processing API key authentication for ${request.requestURI}")
                
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
                logger.info("API key authentication successful for user: test-user")
                
            } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                logger.info("Processing JWT token authentication for ${request.requestURI}")
                val token = authHeader.substring(7)
                logger.debug("Token: ${token.take(10)}...")
                
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
                    
                    logger.info("JWT token validated successfully for user: ${userInfo.username}")
                    
                } catch (e: Exception) {
                    logger.error("JWT token validation failed for ${request.requestURI}: ${e.message}", e)
                    SecurityContextHolder.clearContext()
                }
            } else {
                logger.debug("No valid authentication header found for ${request.requestURI}")
            }
            
        } catch (e: Exception) {
            logger.error("Error processing authentication: ${e.message}")
            SecurityContextHolder.clearContext()
        }
        
        filterChain.doFilter(request, response)
    }

    private fun verifyJwtToken(token: String): UserInfo {
        try {
            logger.debug("Verifying JWT token with secret length: ${jwtSecret.length}")
            val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

            logger.debug("JWT claims: subject=${claims.subject}, username=${claims["username"]}")
            
            return UserInfo(
                id = claims.subject ?: "unknown",
                username = claims["username"] as? String ?: "unknown",
                email = claims["email"] as? String
            )
        } catch (e: Exception) {
            logger.error("JWT token verification failed: ${e.javaClass.simpleName} - ${e.message}")
            throw e
        }
    }
}
