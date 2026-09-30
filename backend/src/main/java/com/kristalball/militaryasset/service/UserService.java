package com.kristalball.militaryasset.service;

import com.kristalball.militaryasset.entity.User;
import com.kristalball.militaryasset.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for the User entity.
 *
 * SECURITY: Passwords are BCrypt-hashed before storing. The raw password is
 * NEVER persisted in plain text and NEVER returned in responses.
 *
 * RBAC: This service is accessed only by ADMIN users (enforced by SecurityConfig).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Return all users. */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Return a single user by ID.
     * Throws RuntimeException if not found.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /**
     * Create a new user.
     * The passwordHash field in the request is treated as the plain-text password
     * and is BCrypt-hashed before saving.
     */
    public User createUser(User user) {
        // Hash the plain-text password before saving
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return userRepository.save(user);
    }

    /**
     * Update an existing user.
     * If a new password is provided, it is BCrypt-hashed before saving.
     * If no password is provided, the existing hash is kept.
     */
    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);
        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        existing.setRole(updatedUser.getRole());
        existing.setBase(updatedUser.getBase());
        // Only update password if a new one is provided — hash it before saving
        if (updatedUser.getPasswordHash() != null && !updatedUser.getPasswordHash().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(updatedUser.getPasswordHash()));
        }
        return userRepository.save(existing);
    }

    /**
     * Delete a user by ID.
     * Throws RuntimeException if not found.
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
