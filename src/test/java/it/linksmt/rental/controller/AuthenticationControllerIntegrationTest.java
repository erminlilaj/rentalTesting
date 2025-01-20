package it.linksmt.rental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.linksmt.rental.dto.LoginUserRequest;
import it.linksmt.rental.dto.RegisterUserRequest;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.enums.UserType;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final String salt = "salt";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Preload admin user
        UserEntity admin = new UserEntity(
                null, "admin", "Admin", "User", "admin@example.com",
                "adminpassword" + salt, 35, UserType.ADMIN,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
        userRepository.save(admin);
    }

    // Positive Test Cases

    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "testuser", "Test", "User", "testuser@example.com", "password", 25
        );
        String requestBody = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void testGetLoggedUserId_Success() throws Exception {
        // Preload user for token generation
        UserEntity user = userRepository.findByUsername("admin").orElseThrow();
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/auth/userId")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(user.getId()));
    }

    // Negative Test Cases

    @Test
    void testRegisterUser_AlreadyExists() throws Exception {
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "admin", "Admin", "User", "admin@example.com", "password", 30
        );
        String requestBody = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with username: admin"));
    }

    @Test
    void testAuthenticate_InvalidCredentials() throws Exception {
        LoginUserRequest loginRequest = new LoginUserRequest("nonexistentuser", "wrongpassword");
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void testGetLoggedUserId_Unauthorized() throws Exception {
        mockMvc.perform(get("/auth/userId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value(103));
    }

    @Test
    void testIsAdmin_NotAdmin() throws Exception {
        // Create and preload a non-admin user
        UserEntity user = new UserEntity(
                null, "regularuser", "Regular", "User", "user@example.com",
                "password" + salt, 28, UserType.USER,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/auth/isAdmin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}
