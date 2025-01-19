package it.linksmt.rental.controller;

import it.linksmt.rental.dto.CreateUserRequest;
import it.linksmt.rental.dto.UpdateUserRequest;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.enums.ErrorCode;
import it.linksmt.rental.enums.UserType;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.JwtService;

import it.linksmt.rental.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerDbTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create and save an admin user
        UserEntity adminUser = new UserEntity(
                null,
                "admin",
                "Admin",
                "User",
                "admin@example.com",
                "$2a$10$3tSIPUuEo8lmzjBnfRZZvubO/lsr6loVy6S6LRZU0bXKo1p/X04am",
                50,
                UserType.ADMIN,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
        userRepository.save(adminUser);

        // Mock the behavior of authenticationService.isAdmin()
        when(authenticationService.isAdmin()).thenReturn(true);

        // Generate JWT token for the admin user
        adminToken = jwtService.generateToken(adminUser);

        // Debugging to verify the mock
        assert userService != null;
        System.out.println("Mocked userService is injected: " + (userService instanceof UserService));
    }


    @Test
    void createUserTest() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "johNdoe",
                "John",
                "Doe",
                "johndoe@example.com",
                "password",
                30,
                UserType.USER
        );
        String requestBody = objectMapper.writeValueAsString(createUserRequest);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("johNdoe"));
    }

    @Test
    void getUserByIdTest() throws Exception {
        UserEntity savedUser = userRepository.save(new UserEntity(
                null,
                "johNdoe",
                "John",
                "Doe",
                "johndoe@example.com",
                "password",
                30,
                UserType.USER,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        ));

        mockMvc.perform(get("/api/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johNdoe"));
    }

    @Test
    void getUserByIdNotFoundTest() throws Exception {
        mockMvc.perform(get("/api/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsersTest() throws Exception {
        userRepository.save(new UserEntity(
                null,
                "user1",
                "Test",
                "User",
                "user1@example.com",
                "password",
                30,
                UserType.USER,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        ));
        userRepository.save(new UserEntity(
                null,
                "user2",
                "Test",
                "User",
                "user2@example.com",
                "password",
                30,
                UserType.USER,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        ));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)); // Includes admin
    }

    @Test
    void deleteUserTest() throws Exception {
        UserEntity savedUser = userRepository.save(new UserEntity(
                null,
                "testuser",
                "Test",
                "User",
                "testuser@example.com",
                "password",
                30,
                UserType.USER,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        ));

        mockMvc.perform(delete("/api/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUserNotFoundTest() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 99999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserTest() throws Exception {
        UserEntity savedUser = userRepository.save(new UserEntity(
                null,
                "johNdoe",
                "John",
                "Doe",
                "johndoe@example.com",
                "password",
                30,
                UserType.USER,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        ));
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(
                "updatedUsername",
                "updatedPassword",
                35
        );
        String requestBody = objectMapper.writeValueAsString(updateUserRequest);

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedUsername"))
                .andExpect(jsonPath("$.age").value(35));
    }
}
