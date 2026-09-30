package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.config.JwtUtil;
import com.kristalball.militaryasset.dto.LoginRequest;
import com.kristalball.militaryasset.dto.LoginResponse;
import com.kristalball.militaryasset.dto.SignupRequest;
import com.kristalball.militaryasset.dto.SignupResponse;
import com.kristalball.militaryasset.entity.User;
import com.kristalball.militaryasset.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 * Base URL: /api/auth
 *
 * Public endpoints — no JWT required:
 *   POST /api/auth/login   — authenticate and receive a JWT
 *   POST /api/auth/signup  — register as a LOGISTICS_OFFICER
 *
 * All other /api/** endpoints require a valid JWT (configured in SecurityConfig).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * POST /api/auth/login
     *
     * Request body:  { "email": "user@example.com", "password": "secret" }
     * Response body: { "token": "<jwt>", "email": "user@example.com", "role": "ADMIN" }
     *
     * Flow:
     *  1. Spring Security authenticates the email + password against the DB.
     *  2. If valid, generate a JWT token containing the email and role.
     *  3. Return the token along with the user's email and role.
     *  4. If invalid, return 401 UNAUTHORIZED.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Authenticate — throws BadCredentialsException if wrong credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 2. Load the user to get their role (Spring Security's UserDetails only has authorities)
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            // 3. Generate the JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

            // 4. Return the token + user info
            return ResponseEntity.ok(new LoginResponse(token, user.getEmail(), user.getRole()));

        } catch (BadCredentialsException e) {
            // Wrong email or password
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }
    }

    /**
     * POST /api/auth/signup
     *
     * Public registration endpoint. Any person can register an account.
     *
     * SECURITY GUARANTEES:
     *  - The role is ALWAYS forced to LOGISTICS_OFFICER — it is never read from the
     *    request body, so no client can self-assign ADMIN or BASE_COMMANDER.
     *  - base_id is always null (LOGISTICS_OFFICER has no base assignment at signup).
     *  - The name field defaults to the email prefix if not provided.
     *  - Password is BCrypt-hashed before persistence.
     *  - Duplicate emails are rejected with 409 CONFLICT.
     *  - Weak passwords are rejected with 400 BAD_REQUEST.
     *  - passwordHash is NEVER returned in the response.
     *
     * Request body:  { "email": "newuser@example.com", "password": "Password@123" }
     * Response body: { "message": "...", "email": "...", "role": "LOGISTICS_OFFICER" }
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {

        // 1. Validate email
        String email = signupRequest.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        email = email.trim().toLowerCase();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.badRequest().body("Please provide a valid email address.");
        }

        // 2. Validate password
        String password = signupRequest.getPassword();
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Password is required.");
        }
        if (password.length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Z].*")) {
            return ResponseEntity.badRequest().body("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            return ResponseEntity.badRequest().body("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*\\d.*")) {
            return ResponseEntity.badRequest().body("Password must contain at least one digit.");
        }

        // 3. Check for duplicate email — return 409 CONFLICT
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("An account with this email already exists.");
        }

        // 4. Build the user entity.
        //    SECURITY: role is hardcoded — never sourced from the request.
        //    base is null — LOGISTICS_OFFICER has no base at signup.
        User newUser = new User();
        newUser.setEmail(email);
        // Default name: part before @ in email
        newUser.setName(email.contains("@") ? email.split("@")[0] : email);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setRole("LOGISTICS_OFFICER"); // ALWAYS — never from request body
        newUser.setBase(null);                // ALWAYS — user cannot assign themselves

        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SignupResponse(
                        "Registration successful. Please log in with your credentials.",
                        newUser.getEmail(),
                        newUser.getRole()
                )
        );
    }
}
