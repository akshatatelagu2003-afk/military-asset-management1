package com.kristalball.militaryasset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kristalball.militaryasset.config.JwtUtil;
import com.kristalball.militaryasset.dto.LoginRequest;
import com.kristalball.militaryasset.entity.Base;
import com.kristalball.militaryasset.entity.Purchase;
import com.kristalball.militaryasset.entity.User;
import com.kristalball.militaryasset.repository.BaseRepository;
import com.kristalball.militaryasset.repository.PurchaseRepository;
import com.kristalball.militaryasset.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BaseRepository baseRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User admin;
    private User baseCommander1;
    private User baseCommander2;
    private User logisticsOfficer;
    private Base base1;
    private Base base2;

    private String adminToken;
    private String bc1Token;
    private String bc2Token;
    private String loToken;

    @BeforeEach
    public void setup() {
        // Clean up before starting to avoid unique constraint violations
        purchaseRepository.deleteAll();
        userRepository.deleteAll();
        baseRepository.deleteAll();

        // 1. Create Bases
        base1 = new Base();
        base1.setName("Test Base 1");
        base1.setLocation("Location 1");
        base1 = baseRepository.save(base1);

        base2 = new Base();
        base2.setName("Test Base 2");
        base2.setLocation("Location 2");
        base2 = baseRepository.save(base2);

        // 2. Create Users
        admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode("password123"));
        admin.setRole("ADMIN");
        admin = userRepository.save(admin);

        baseCommander1 = new User();
        baseCommander1.setName("BC 1");
        baseCommander1.setEmail("bc1@test.com");
        baseCommander1.setPasswordHash(passwordEncoder.encode("password123"));
        baseCommander1.setRole("BASE_COMMANDER");
        baseCommander1.setBase(base1);
        baseCommander1 = userRepository.save(baseCommander1);

        baseCommander2 = new User();
        baseCommander2.setName("BC 2");
        baseCommander2.setEmail("bc2@test.com");
        baseCommander2.setPasswordHash(passwordEncoder.encode("password123"));
        baseCommander2.setRole("BASE_COMMANDER");
        baseCommander2.setBase(base2);
        baseCommander2 = userRepository.save(baseCommander2);

        logisticsOfficer = new User();
        logisticsOfficer.setName("LO");
        logisticsOfficer.setEmail("lo@test.com");
        logisticsOfficer.setPasswordHash(passwordEncoder.encode("password123"));
        logisticsOfficer.setRole("LOGISTICS_OFFICER");
        logisticsOfficer = userRepository.save(logisticsOfficer);

        // 3. Generate tokens
        adminToken = jwtUtil.generateToken(admin.getEmail(), admin.getRole());
        bc1Token   = jwtUtil.generateToken(baseCommander1.getEmail(), baseCommander1.getRole());
        bc2Token   = jwtUtil.generateToken(baseCommander2.getEmail(), baseCommander2.getRole());
        loToken    = jwtUtil.generateToken(logisticsOfficer.getEmail(), logisticsOfficer.getRole());
    }

    @AfterEach
    public void cleanup() {
        purchaseRepository.deleteAll();
        userRepository.deleteAll();
        baseRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Existing RBAC tests (must remain passing)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void testUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAdminAccess() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogisticsOfficerAccess() throws Exception {
        // LO should be forbidden from users endpoint
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + loToken))
                .andExpect(status().isForbidden());

        // LO should be allowed to view purchases
        mockMvc.perform(get("/api/purchases")
                .header("Authorization", "Bearer " + loToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testBaseCommanderAccess() throws Exception {
        // BC should be forbidden from users endpoint
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + bc1Token))
                .andExpect(status().isForbidden());

        // BC should be allowed to view purchases
        mockMvc.perform(get("/api/purchases")
                .header("Authorization", "Bearer " + bc1Token))
                .andExpect(status().isOk());
    }

    @Test
    public void testBaseCommanderCrossBaseRestriction() throws Exception {
        // Existing placeholder test — kept for RBAC continuity
    }

    @Test
    public void testPasswordSecurity() throws Exception {
        User user = userRepository.findByEmail("admin@test.com").get();
        assertTrue(user.getPasswordHash().startsWith("$2a$")); // BCrypt check

        MvcResult result = mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertFalse(content.contains("passwordHash"));
    }

    @Test
    public void testJwtLogin() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        req.setPassword("wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Signup tests
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Test 1: Signup with valid email/password succeeds.
     */
    @Test
    public void testSignup_ValidCredentials_Returns201() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "newuser@test.com");
        body.put("password", "Secure@12");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("LOGISTICS_OFFICER"))
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Test 2: Password in DB is stored as BCrypt hash, not plaintext.
     */
    @Test
    public void testSignup_PasswordIsBCryptHashed() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "hashcheck@test.com");
        body.put("password", "Secure@12");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        User saved = userRepository.findByEmail("hashcheck@test.com").orElseThrow();
        // Must start with BCrypt signature
        assertTrue(saved.getPasswordHash().startsWith("$2a$"),
                "Password must be BCrypt hashed, not plaintext");
        // Must NOT equal the plain-text password
        assertNotEquals("Secure@12", saved.getPasswordHash());
    }

    /**
     * Test 3: Duplicate email is rejected with 409 CONFLICT.
     */
    @Test
    public void testSignup_DuplicateEmail_Returns409() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "admin@test.com"); // already exists from @BeforeEach
        body.put("password", "Secure@12");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    /**
     * Test 4: Sending role=ADMIN in body does NOT create an ADMIN user.
     */
    @Test
    public void testSignup_RoleAdminInBody_IsIgnored_UserIsLogisticsOfficer() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("email", "evilAdmin@test.com");
        body.put("password", "Secure@12");
        body.put("role", "ADMIN"); // client attempt to escalate — must be ignored

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("LOGISTICS_OFFICER"));

        User saved = userRepository.findByEmail("eviladmin@test.com").orElseThrow();
        assertEquals("LOGISTICS_OFFICER", saved.getRole(),
                "Role escalation via request body must be prevented");
        assertNotEquals("ADMIN", saved.getRole());
    }

    /**
     * Test 5: Sending role=BASE_COMMANDER in body does NOT create a BASE_COMMANDER user.
     */
    @Test
    public void testSignup_RoleBaseCommanderInBody_IsIgnored_UserIsLogisticsOfficer() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("email", "evilBC@test.com");
        body.put("password", "Secure@12");
        body.put("role", "BASE_COMMANDER"); // client attempt to escalate — must be ignored

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("LOGISTICS_OFFICER"));

        User saved = userRepository.findByEmail("evilbc@test.com").orElseThrow();
        assertEquals("LOGISTICS_OFFICER", saved.getRole(),
                "Role escalation via request body must be prevented");
        assertNotEquals("BASE_COMMANDER", saved.getRole());
    }

    /**
     * Test 6: A user can log in after signup with the registered credentials.
     */
    @Test
    public void testSignup_ThenLogin_ReturnsValidJwt() throws Exception {
        // Step 1: Sign up
        Map<String, String> signupBody = new HashMap<>();
        signupBody.put("email", "loginafter@test.com");
        signupBody.put("password", "Secure@12");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupBody)))
                .andExpect(status().isCreated());

        // Step 2: Login with the same credentials — must receive a valid JWT
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("loginafter@test.com");
        loginReq.setPassword("Secure@12");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("LOGISTICS_OFFICER"))
                .andReturn();

        // Step 3: Verify the JWT contains the correct role
        String responseJson = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("token").asText();
        assertEquals("LOGISTICS_OFFICER", jwtUtil.extractRole(token),
                "JWT must carry LOGISTICS_OFFICER role");
    }

    /**
     * Test 7: Existing ADMIN login still works.
     */
    @Test
    public void testExistingAdminLogin_StillWorks() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").exists());
    }

    /**
     * Test 8: Existing BASE_COMMANDER login still works.
     */
    @Test
    public void testExistingBaseCommanderLogin_StillWorks() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("bc1@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("BASE_COMMANDER"))
                .andExpect(jsonPath("$.token").exists());
    }

    /**
     * Test 9: Existing LOGISTICS_OFFICER login still works.
     */
    @Test
    public void testExistingLogisticsOfficerLogin_StillWorks() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("lo@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("LOGISTICS_OFFICER"))
                .andExpect(jsonPath("$.token").exists());
    }

    /**
     * Test 10: Signup response never contains passwordHash.
     */
    @Test
    public void testSignup_ResponseDoesNotExposePasswordHash() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "nohash@test.com");
        body.put("password", "Secure@12");

        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertFalse(content.contains("passwordHash"),
                "Signup response must NOT expose passwordHash");
        assertFalse(content.contains("password"),
                "Signup response must NOT expose password field");
    }

    /**
     * Test 11: Signup with missing password returns 400.
     */
    @Test
    public void testSignup_MissingPassword_Returns400() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "nopassword@test.com");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 12: Signup with weak password (no uppercase) returns 400.
     */
    @Test
    public void testSignup_WeakPassword_Returns400() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "weakpass@test.com");
        body.put("password", "alllowercase1"); // no uppercase

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 13: Signup with invalid email format returns 400.
     */
    @Test
    public void testSignup_InvalidEmail_Returns400() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", "not-an-email");
        body.put("password", "Secure@12");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
