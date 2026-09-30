package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.dto.UserResponse;
import com.kristalball.militaryasset.entity.User;
import com.kristalball.militaryasset.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing system users.
 * Base URL: /api/users
 *
 * RBAC: ADMIN only (enforced by SecurityConfig).
 *
 * SECURITY: All responses use UserResponse DTO — passwordHash is NEVER returned.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/users — returns all users (without password hashes) */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /** GET /api/users/{id} — returns a single user (without password hash) or 404 */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(UserResponse.from(userService.getUserById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/users — creates a new user, returns 201 CREATED.
     * The request body must include a plain-text password that will be BCrypt-hashed
     * by UserService before saving.
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(created));
    }

    /** PUT /api/users/{id} — updates an existing user, returns 200 OK or 404 */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody User user) {
        try {
            return ResponseEntity.ok(UserResponse.from(userService.updateUser(id, user)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/users/{id} — deletes a user, returns 204 NO CONTENT or 404 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
