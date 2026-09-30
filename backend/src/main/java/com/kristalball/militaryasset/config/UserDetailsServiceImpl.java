package com.kristalball.militaryasset.config;

import com.kristalball.militaryasset.entity.User;
import com.kristalball.militaryasset.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads a user from the database by email for Spring Security authentication.
 *
 * Spring Security calls this during login to fetch the user and compare passwords.
 * The "username" in Spring Security's model maps to our user's email field.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user by their email address.
     *
     * @param email the email entered at login (Spring Security passes "username" here)
     * @throws UsernameNotFoundException if no user with that email exists
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Wrap our User entity in Spring Security's UserDetails format.
        // The role is stored as "ADMIN", "BASE_COMMANDER", or "LOGISTICS_OFFICER" in the DB;
        // Spring Security expects roles to start with "ROLE_".
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
