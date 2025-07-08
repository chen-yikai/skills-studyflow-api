package dev.eliaschen.skillsstudyflowapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory

data class AuthRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserInfo
)

data class UserInfo(
    val id: String,
    val username: String,
    val email: String? = null
)

data class AuthUrlResponse(
    val authUrl: String,
    val state: String,
    val expiresIn: Long
)

data class AuthSession(
    val state: String,
    val createdAt: Long,
    val expiresAt: Long,
    var isAuthenticated: Boolean = false,
    var userInfo: UserInfo? = null
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
    
    @Value("\${app.auth.session.timeout}")
    private var sessionTimeout: Long = 300000 // 5 minutes
    
    // In-memory storage for demo purposes - use database in production
    private val users = mutableMapOf(
        "demo" to "password123",
        "user1" to "pass123",
        "admin" to "admin123"
    )
    
    private val authSessions = ConcurrentHashMap<String, AuthSession>()

    @GetMapping("/authorize")
    @Operation(
        summary = "Get authorization URL", 
        description = "Generates an authorization URL that mobile apps can open in a browser. Returns a state parameter for tracking the auth session."
    )
    @ApiResponse(responseCode = "200", description = "Authorization URL generated successfully")
    fun getAuthorizationUrl(
        @Parameter(description = "Redirect URI for the mobile app") 
        @RequestParam(required = false) redirectUri: String?
    ): ResponseEntity<AuthUrlResponse> {
        val state = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()
        val expiresAt = currentTime + sessionTimeout
        
        val session = AuthSession(
            state = state,
            createdAt = currentTime,
            expiresAt = expiresAt
        )
        
        authSessions[state] = session
        
        val baseUrl = "http://localhost:8080" // Change this to your actual server URL
        val authUrl = "$baseUrl/auth/login?state=$state" + 
                     if (redirectUri != null) "&redirect_uri=${redirectUri}" else ""
        
        logger.info("Generated authorization URL for state: $state")
        
        return ResponseEntity.ok(AuthUrlResponse(
            authUrl = authUrl,
            state = state,
            expiresIn = sessionTimeout
        ))
    }

    @GetMapping("/login")
    @Operation(
        summary = "Login page", 
        description = "Displays a simple login form that users can fill out in their browser"
    )
    fun showLoginPage(
        @RequestParam state: String,
        @RequestParam(required = false) redirectUri: String?
    ): ResponseEntity<String> {
        val session = authSessions[state]
        if (session == null || System.currentTimeMillis() > session.expiresAt) {
            return ResponseEntity.badRequest().body("<html><body><h1>Error: Invalid or expired session</h1></body></html>")
        }
        
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>StudyFlow Login</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; }
                    .form-group { margin-bottom: 15px; }
                    label { display: block; margin-bottom: 5px; font-weight: bold; }
                    input[type="text"], input[type="password"] { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
                    button { width: 100%; padding: 12px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
                    button:hover { background-color: #0056b3; }
                    .error { color: red; margin-top: 10px; }
                    .demo-info { background-color: #f8f9fa; padding: 10px; border-radius: 4px; margin-bottom: 20px; font-size: 14px; }
                </style>
            </head>
            <body>
                <h2>StudyFlow Login</h2>
                <div class="demo-info">
                    <strong>Demo Users:</strong><br>
                    Username: demo, Password: password123<br>
                    Username: user1, Password: pass123<br>
                    Username: admin, Password: admin123
                </div>
                <form id="loginForm">
                    <div class="form-group">
                        <label for="username">Username:</label>
                        <input type="text" id="username" name="username" required>
                    </div>
                    <div class="form-group">
                        <label for="password">Password:</label>
                        <input type="password" id="password" name="password" required>
                    </div>
                    <button type="submit">Login</button>
                    <div id="error" class="error"></div>
                </form>
                
                <script>
                    document.getElementById('loginForm').addEventListener('submit', async function(e) {
                        e.preventDefault();
                        
                        const username = document.getElementById('username').value;
                        const password = document.getElementById('password').value;
                        const errorDiv = document.getElementById('error');
                        
                        try {
                            const response = await fetch('/auth/authenticate', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                },
                                body: JSON.stringify({
                                    username: username,
                                    password: password,
                                    state: '$state'
                                })
                            });
                            
                            const result = await response.json();
                            
                            if (response.ok) {
                                // Success - redirect or show success message
                                document.body.innerHTML = '<h2>Login Successful!</h2><p>You can now close this window and return to your app.</p><p>State: ' + result.state + '</p>';
                            } else {
                                errorDiv.textContent = result.message || 'Login failed';
                            }
                        } catch (error) {
                            errorDiv.textContent = 'Network error: ' + error.message;
                        }
                    });
                </script>
            </body>
            </html>
        """.trimIndent()
        
        return ResponseEntity.ok()
            .header("Content-Type", "text/html")
            .body(html)
    }

    @PostMapping("/authenticate")
    @Operation(
        summary = "Authenticate user", 
        description = "Authenticates user credentials and marks the session as authenticated"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Authentication successful"),
            ApiResponse(responseCode = "400", description = "Invalid credentials or session"),
            ApiResponse(responseCode = "401", description = "Authentication failed")
        ]
    )
    fun authenticate(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val username = request["username"]
        val password = request["password"] 
        val state = request["state"]
        
        if (username == null || password == null || state == null) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Missing required fields"))
        }
        
        val session = authSessions[state]
        if (session == null || System.currentTimeMillis() > session.expiresAt) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Invalid or expired session"))
        }
        
        // Check credentials
        if (users[username] != password) {
            logger.warn("Authentication failed for username: $username")
            return ResponseEntity.status(401).body(mapOf("message" to "Invalid credentials"))
        }
        
        // Mark session as authenticated
        session.isAuthenticated = true
        session.userInfo = UserInfo(
            id = UUID.randomUUID().toString(),
            username = username,
            email = "$username@example.com"
        )
        
        logger.info("User authenticated successfully: $username, state: $state")
        
        return ResponseEntity.ok(mapOf(
            "message" to "Authentication successful",
            "state" to state,
            "username" to username
        ))
    }

    @PostMapping("/token")
    @Operation(
        summary = "Exchange state for access token", 
        description = "Mobile app calls this endpoint with the state to get an access token after user authentication"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Token generated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid or unauthenticated session")
        ]
    )
    fun exchangeToken(@RequestBody request: Map<String, String>): ResponseEntity<Any> {
        val state = request["state"]
        
        if (state == null) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Missing state parameter"))
        }
        
        val session = authSessions[state]
        if (session == null || System.currentTimeMillis() > session.expiresAt) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Invalid or expired session"))
        }
        
        if (!session.isAuthenticated || session.userInfo == null) {
            return ResponseEntity.badRequest().body(mapOf("message" to "Session not authenticated"))
        }
        
        // Generate JWT token
        val token = generateJwtToken(session.userInfo!!)
        
        // Clean up session
        authSessions.remove(state)
        
        logger.info("Token generated for user: ${session.userInfo!!.username}")
        
        return ResponseEntity.ok(AuthResponse(
            accessToken = token,
            expiresIn = jwtExpiration,
            user = session.userInfo!!
        ))
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
            
            return ResponseEntity.ok(mapOf(
                "valid" to true,
                "user" to userInfo
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(401).body(mapOf(
                "valid" to false,
                "message" to "Invalid token"
            ))
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
