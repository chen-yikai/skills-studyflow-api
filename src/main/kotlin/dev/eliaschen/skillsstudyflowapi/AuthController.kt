package dev.eliaschen.skillsstudyflowapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.nio.charset.StandardCharsets


data class UserInfo(
    val id: String,
    val username: String,
    val email: String? = null
)


@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Local authentication API for mobile apps")
class AuthController {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @Value("\${app.auth.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.auth.jwt.expiration}")
    private var jwtExpiration: Long = 86400000

    private val users = mutableMapOf(
        "elias" to "meow",
        "yang" to "password",
        "scplay" to "q123456",
        "user" to "password123"
    )


    @GetMapping("/signin", produces = [MediaType.TEXT_HTML_VALUE])
    @Operation(
        summary = "Show sign in page",
        description = "Serves the login form HTML page directly"
    )
    @ApiResponse(responseCode = "200", description = "Login page served successfully")
    fun showSignInPage(
        @RequestParam(required = false, defaultValue = "studyflow://oauth") redirectUri: String
    ): ResponseEntity<String> {
        try {
            // Read the HTML file from the classpath
            val resource = ClassPathResource("templates/login.html")
            val htmlContent = resource.inputStream.readBytes().toString(StandardCharsets.UTF_8)

            // Replace the placeholder with the actual redirect URI
            val processedHtml = htmlContent.replace("\$redirectUri", redirectUri)

            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(processedHtml)
        } catch (e: Exception) {
            logger.error("Error serving login page", e)
            return ResponseEntity.status(500)
                .body("<html><body><h1>Error loading login page</h1></body></html>")
        }
    }

    @PostMapping("/authenticate")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticates user credentials and returns a JWT token"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Authentication successful"),
            ApiResponse(responseCode = "400", description = "Missing required fields"),
            ApiResponse(responseCode = "401", description = "Authentication failed")
        ]
    )
    fun authenticate(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val username = request["username"]
        val password = request["password"]

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Missing required fields"))
        }

        // Check credentials
        if (users[username] != password) {
            logger.warn("Authentication failed for username: $username")
            return ResponseEntity.status(401).body(mapOf("message" to "Invalid credentials"))
        }

        // Generate JWT token
        val token = generateJwtToken(
            UserInfo(
                id = UUID.randomUUID().toString(),
                username = username,
                email = "$username@example.com"
            )
        )

        logger.info("User authenticated successfully: $username")

        return ResponseEntity.ok(
            mapOf(
                "accessToken" to token,
                "tokenType" to "Bearer",
                "username" to username
            )
        )
    }

    @GetMapping("/verify")
    @Operation(
        summary = "Verify access token",
        description = "Verifies if an access token is valid"
    )
    fun verifyToken(): ResponseEntity<Any> {
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated) {
                val userDetails = authentication.principal as UserDetails
                
                return ResponseEntity.ok(
                    mapOf(
                        "valid" to true,
                        "user" to mapOf(
                            "username" to userDetails.username,
                            "authenticated" to true
                        )
                    )
                )
            } else {
                return ResponseEntity.status(401).body(
                    mapOf(
                        "valid" to false,
                        "message" to "No valid authentication found"
                    )
                )
            }
        } catch (e: Exception) {
            return ResponseEntity.status(401).body(
                mapOf(
                    "valid" to false,
                    "message" to "Invalid token"
                )
            )
        }
    }

    @PostMapping("/signout")
    @Operation(
        summary = "Sign out user",
        description = "Signs out the user by removing session data (if any)"
    )
    @ApiResponse(responseCode = "200", description = "Sign out successful")
    fun signOut(): ResponseEntity<Map<String, String>> {
        // Clear security context
        SecurityContextHolder.clearContext()
        logger.info("User signed out.")
        return ResponseEntity.ok(mapOf("message" to "Sign out successful"))
    }

    private fun generateJwtToken(userInfo: UserInfo): String {
        val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

        return Jwts.builder()
            .subject(userInfo.id)
            .claim("username", userInfo.username)
            .claim("email", userInfo.email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key)
            .compact()
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
