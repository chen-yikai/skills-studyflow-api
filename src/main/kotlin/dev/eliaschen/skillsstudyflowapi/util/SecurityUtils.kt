package dev.eliaschen.skillsstudyflowapi.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

object SecurityUtils {
    
    /**
     * Get the current authenticated user's username
     */
    fun getCurrentUsername(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return when {
            authentication == null || !authentication.isAuthenticated -> "anonymous"
            authentication.principal is UserDetails -> (authentication.principal as UserDetails).username
            authentication.principal is String -> authentication.principal as String
            else -> "unknown"
        }
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated
    }
}
