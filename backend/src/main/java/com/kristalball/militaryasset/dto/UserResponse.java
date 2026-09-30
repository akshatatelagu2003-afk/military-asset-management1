package com.kristalball.militaryasset.dto;

import com.kristalball.militaryasset.entity.User;

/**
 * Safe response DTO for User objects.
 * Deliberately excludes the passwordHash field to prevent exposing it in API responses.
 */
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private Long baseId;
    private String baseName;

    public UserResponse() {}

    /** Convenience constructor that maps from a User entity. */
    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.id       = user.getId();
        dto.name     = user.getName();
        dto.email    = user.getEmail();
        dto.role     = user.getRole();
        dto.baseId   = user.getBase() != null ? user.getBase().getId()   : null;
        dto.baseName = user.getBase() != null ? user.getBase().getName() : null;
        return dto;
    }

    public Long getId()       { return id; }
    public String getName()   { return name; }
    public String getEmail()  { return email; }
    public String getRole()   { return role; }
    public Long getBaseId()   { return baseId; }
    public String getBaseName(){ return baseName; }

    public void setId(Long id)           { this.id = id; }
    public void setName(String name)     { this.name = name; }
    public void setEmail(String email)   { this.email = email; }
    public void setRole(String role)     { this.role = role; }
    public void setBaseId(Long baseId)   { this.baseId = baseId; }
    public void setBaseName(String baseName) { this.baseName = baseName; }
}
