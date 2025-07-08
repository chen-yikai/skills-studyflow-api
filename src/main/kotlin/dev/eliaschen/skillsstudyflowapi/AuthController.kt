package dev.eliaschen.skillsstudyflowapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import org.slf4j.LoggerFactory


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
    private var jwtExpiration: Long = 86400000 // 24 hours

    // In-memory storage for demo purposes - use database in production
    private val users = mutableMapOf(
        "demo" to "password123",
        "user1" to "pass123",
        "admin" to "admin123"
    )


    @GetMapping("/signin")
    @Operation(
        summary = "Show sign in page",
        description = "Redirects to the login form HTML page"
    )
    @ApiResponse(responseCode = "302", description = "Redirect to login page")
    fun showSignInPage(
        @RequestParam(required = false, defaultValue = "studyflow://oauth") redirectUri: String
    ): ResponseEntity<Void> {
        val redirectUrl = if (redirectUri != "studyflow://oauth") {
            "/login.html?redirect_uri=${java.net.URLEncoder.encode(redirectUri, "UTF-8")}"
        } else {
            "/login.html"
        }
        
        return ResponseEntity.status(302)
            .header("Location", redirectUrl)
            .build()
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

    @PostMapping("/signout")
    @Operation(
        summary = "Sign out user",
        description = "Signs out the user by removing session data (if any)"
    )
    @ApiResponse(responseCode = "200", description = "Sign out successful")
    fun signOut(): ResponseEntity<Map<String, String>> {
        // Clearing logic if needed
        logger.info("User signed out.")
        return ResponseEntity.ok(mapOf("message" to "Sign out successful"))
    }

    @GetMapping("/verify")
    @Operation(
        summary = "Verify access token",
        description = "Verifies if an access token is valid"
    )
    fun verifyToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        try {
            val token = authHeader.removePrefix("Bearer ")
            val userInfo = verifyJwtToken(token)

            return ResponseEntity.ok(
                mapOf(
                    "valid" to true,
                    "user" to userInfo
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.status(401).body(
                mapOf(
                    "valid" to false,
                    "message" to "Invalid token"
                )
            )
        }
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
