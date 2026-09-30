package com.kristalball.militaryasset.config;

import com.kristalball.militaryasset.entity.User;
import com.kristalball.militaryasset.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Helper service that resolves the currently authenticated user's full entity
 * from the database, using the email stored in the SecurityContext.
 *
 * This is the ONLY safe way to determine who the caller is and which base
 * they belong to. Controllers and services must NEVER trust a base_id
 * supplied in the request body or query parameters for authorization decisions.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the full User entity for the currently authenticated caller.
     * Throws UsernameNotFoundException if the token email doesn't match any user.
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // populated by JwtAuthenticationFilter
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found: " + email));
    }

    /**
     * Returns the role of the currently authenticated caller ("ADMIN",
     * "BASE_COMMANDER", or "LOGISTICS_OFFICER").
     */
    public String getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    /**
     * Returns the base ID of the currently authenticated caller.
     * Returns null for ADMIN and LOGISTICS_OFFICER (they are not assigned to a specific base).
     */
    public Long getCurrentUserBaseId() {
        User user = getCurrentUser();
        return user.getBase() != null ? user.getBase().getId() : null;
    }

    /** Convenience: is the current user an ADMIN? */
    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }

    /** Convenience: is the current user a BASE_COMMANDER? */
    public boolean isBaseCommander() {
        return "BASE_COMMANDER".equals(getCurrentUserRole());
    }

    /** Convenience: is the current user a LOGISTICS_OFFICER? */
    public boolean isLogisticsOfficer() {
        return "LOGISTICS_OFFICER".equals(getCurrentUserRole());
    }
}
