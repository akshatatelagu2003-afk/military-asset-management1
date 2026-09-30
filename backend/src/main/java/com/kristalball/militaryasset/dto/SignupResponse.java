package com.kristalball.militaryasset.dto;

/**
 * DTO returned after successful registration.
 * Returns only safe, non-sensitive fields. Never returns passwordHash.
 */
public class SignupResponse {

    private String message;
    private String email;
    private String role;

    public SignupResponse(String message, String email, String role) {
        this.message = message;
        this.email = email;
        this.role = role;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
