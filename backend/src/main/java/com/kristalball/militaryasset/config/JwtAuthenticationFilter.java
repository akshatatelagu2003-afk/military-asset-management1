package com.kristalball.militaryasset.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs once per HTTP request.
 *
 * For every request it:
 *  1. Reads the "Authorization: Bearer <token>" header.
 *  2. Validates the token using JwtUtil.
 *  3. Loads the user from the DB via UserDetailsServiceImpl.
 *  4. Sets the authentication in Spring Security's SecurityContext.
 *
 * If the token is missing or invalid the request is NOT blocked here —
 * it simply passes through unauthenticated and SecurityConfig decides what to do.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extract the Authorization header
        String authHeader = request.getHeader("Authorization");

        // If there is no header or it does not start with "Bearer ", skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract the raw token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // 3. Validate the token
        if (!jwtUtil.validateToken(token)) {
            // Invalid or expired — let the request pass through unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Extract the email from the token
        String email = jwtUtil.extractEmail(token);

        // 5. Only set authentication if not already authenticated for this request
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load the full user from DB so Spring Security can check authorities
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 6. Build a Spring Security authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,                         // no credentials after authentication
                            userDetails.getAuthorities()  // roles
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 7. Store it in the SecurityContext so the rest of the request is authenticated
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
