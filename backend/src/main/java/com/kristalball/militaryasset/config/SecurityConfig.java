package com.kristalball.militaryasset.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration with full RBAC enforcement.
 *
 * ┌─────────────────────────┬───────┬─────────────────┬──────────────────┐
 * │ Endpoint                │ ADMIN │ BASE_COMMANDER  │ LOGISTICS_OFFICER│
 * ├─────────────────────────┼───────┼─────────────────┼──────────────────┤
 * │ POST /api/auth/login    │  ✓    │       ✓         │        ✓         │
 * │ GET  /api/bases/**      │  ✓    │       ✓         │        ✓         │
 * │ POST /api/bases/**      │  ✓    │       ✗         │        ✗         │
 * │ PUT  /api/bases/**      │  ✓    │       ✗         │        ✗         │
 * │ DELETE /api/bases/**    │  ✓    │       ✗         │        ✗         │
 * │ GET  /api/users/**      │  ✓    │       ✗         │        ✗         │
 * │ POST /api/users/**      │  ✓    │       ✗         │        ✗         │
 * │ PUT  /api/users/**      │  ✓    │       ✗         │        ✗         │
 * │ DELETE /api/users/**    │  ✓    │       ✗         │        ✗         │
 * │ GET  /api/equipment/**  │  ✓    │       ✓         │        ✓         │
 * │ POST /api/equipment/**  │  ✓    │       ✗         │        ✗         │
 * │ PUT  /api/equipment/**  │  ✓    │       ✗         │        ✗         │
 * │ DELETE /api/equipment/**│  ✓    │       ✗         │        ✗         │
 * │ /api/purchases/**       │  ✓    │       ✓ (own base)│      ✓         │
 * │ /api/transfers/**       │  ✓    │       ✓ (own base)│      ✓         │
 * │ /api/assignments/**     │  ✓    │       ✓ (own base)│      ✗         │
 * │ /api/expenditures/**    │  ✓    │       ✓ (own base)│      ✗         │
 * │ /api/audit-logs/**      │  ✓    │       ✗         │        ✗         │
 * │ GET /api/dashboard/**   │  ✓    │       ✓ (own base)│      ✓         │
 * └─────────────────────────┴───────┴─────────────────┴──────────────────┘
 *
 * Note: BASE_COMMANDER "own base" filtering is enforced in the service layer
 * using the authenticated user's base_id from the SecurityContext —
 * NOT from any request parameter.
 *
 * Session policy: STATELESS — every request carries a JWT.
 * @EnableMethodSecurity is active for fine-grained @PreAuthorize checks.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize, @PostAuthorize in controllers/services
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth

                // ── PUBLIC ──────────────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()

                // ── BASES: read by all, write by ADMIN only ──────────────────
                .requestMatchers(org.springframework.http.HttpMethod.GET,  "/api/bases/**")
                    .hasAnyRole("ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER")
                .requestMatchers("/api/bases/**")
                    .hasRole("ADMIN")

                // ── USERS: ADMIN only ────────────────────────────────────────
                .requestMatchers("/api/users/**")
                    .hasRole("ADMIN")

                // ── EQUIPMENT: read by all, write by ADMIN only ──────────────
                .requestMatchers(org.springframework.http.HttpMethod.GET,  "/api/equipment/**")
                    .hasAnyRole("ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER")
                .requestMatchers("/api/equipment/**")
                    .hasRole("ADMIN")

                // ── PURCHASES: ADMIN + BASE_COMMANDER (own base) + LOGISTICS_OFFICER ─
                .requestMatchers("/api/purchases/**")
                    .hasAnyRole("ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER")

                // ── TRANSFERS: ADMIN + BASE_COMMANDER (own base) + LOGISTICS_OFFICER ─
                .requestMatchers("/api/transfers/**")
                    .hasAnyRole("ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER")

                // ── ASSIGNMENTS: ADMIN + BASE_COMMANDER (own base) only ──────
                .requestMatchers("/api/assignments/**")
                    .hasAnyRole("ADMIN", "BASE_COMMANDER")

                // ── EXPENDITURES: ADMIN + BASE_COMMANDER (own base) only ─────
                .requestMatchers("/api/expenditures/**")
                    .hasAnyRole("ADMIN", "BASE_COMMANDER")

                // ── AUDIT LOGS: ADMIN only ───────────────────────────────────
                .requestMatchers("/api/audit-logs/**")
                    .hasRole("ADMIN")

                // ── DASHBOARD: all authenticated users ──────────────────────
                .requestMatchers("/api/dashboard/**")
                    .hasAnyRole("ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER")

                // ── EVERYTHING ELSE: must be authenticated ───────────────────
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                    org.springframework.http.HttpStatus.UNAUTHORIZED))
            )
            .addFilterBefore(jwtAuthenticationFilter,
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @org.springframework.beans.factory.annotation.Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
