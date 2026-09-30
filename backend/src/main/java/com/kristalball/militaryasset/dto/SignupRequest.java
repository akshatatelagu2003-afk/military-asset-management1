package com.kristalball.militaryasset.dto;

/**
 * DTO for the public signup/registration request.
 *
 * Only email and password are accepted from the client.
 * The role is NEVER accepted from the client — it is always forced to
 * LOGISTICS_OFFICER server-side, preventing any privilege escalation.
 *
 * Any extra fields (e.g. "role", "base") sent by the client are simply ignored.
 */
public class SignupRequest {

    private String email;
    private String password;

    public SignupRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
