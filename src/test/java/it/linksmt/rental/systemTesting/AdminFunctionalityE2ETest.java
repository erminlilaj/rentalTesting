package it.linksmt.rental.systemTesting;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.linksmt.rental.dto.*;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.*;
import it.linksmt.rental.repository.ReservationRepository;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.repository.VehicleRepository;
import it.linksmt.rental.security.SecurityBean;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminFunctionalityE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Long vehicleId;
    private Long adminId;
    private static final String ADMIN_PASSWORD = "password123";
    private static final String ADMIN_USERNAME = "admintest";

    @BeforeAll
    void setup() throws Exception {
        // Clean start
        reservationRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin user directly in DB
        UserEntity adminUser = new UserEntity();
        adminUser.setUsername(ADMIN_USERNAME);
        adminUser.setName("Admin");
        adminUser.setSurname("Test");
        adminUser.setEmail("admin@test.com");
        // Add salt to password as done in AuthenticationService
        String salt = "salt";
        String password = ADMIN_PASSWORD + salt;
        adminUser.setPassword(passwordEncoder.encode(password));
        adminUser.setAge(35);
        adminUser.setUserType(UserType.ADMIN);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);
        adminId = adminUser.getId();

        // Set up security context with admin role
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(ADMIN_USERNAME, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Set up SecurityBean with admin role
        SecurityBean securityBean = new SecurityBean();
        securityBean.setAuthorities(authorities);
        securityBean.setUsername(ADMIN_USERNAME);
        it.linksmt.rental.security.SecurityContext.set(securityBean);

        // Login to get token
        LoginUserRequest loginRequest = new LoginUserRequest(ADMIN_USERNAME, ADMIN_PASSWORD);
        String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Get token directly from response body
        adminToken = loginResult.getResponse().getContentAsString();
        adminToken = adminToken.replace("\"", ""); // Remove quotes from token string

        assertNotNull(adminToken, "Admin token should not be null");
        assertTrue(adminToken.length() > 0, "Admin token should not be empty");

        // Verify admin user was created and has correct role
        assertTrue(userRepository.findByUsername(ADMIN_USERNAME).isPresent(),
                "Admin user should be present in database");

        // Verify admin authorization
        MvcResult isAdminResult = mockMvc.perform(get("/auth/isAdmin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("true", isAdminResult.getResponse().getContentAsString(),
                "User should have admin privileges");
    }

    @Test
    @Order(1)
    void completeVehicleManagementFlow() throws Exception {
        // 1. Create a new vehicle
        CreateVehicleRequest createRequest = new CreateVehicleRequest(
                "BMW",
                "X5",
                2024,
                GearboxType.AUTOMATIC,
                FuelType.DIESEL,
                "Black",
                VehicleStatus.AVAILABLE,
                150.0
        );

        MvcResult createResult = mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("BMW"))
                .andExpect(jsonPath("$.model").value("X5"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        VehicleEntity createdVehicle = objectMapper.readValue(responseBody, VehicleEntity.class);
        vehicleId = createdVehicle.getId();

        assertNotNull(vehicleId, "Vehicle ID should not be null");

        // 2. Get vehicle details and verify
        mockMvc.perform(get("/api/vehicles/{id}", vehicleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("BMW"))
                .andExpect(jsonPath("$.model").value("X5"))
                .andExpect(jsonPath("$.vehicleStatus").value("AVAILABLE"));

        // 3. Update vehicle status to maintenance
        UpdateVehicleRequest updateRequest = new UpdateVehicleRequest(
                "X5 M",
                "Metallic Black",
                175.0,
                VehicleStatus.MAINTENANCE
        );

        mockMvc.perform(put("/api/vehicles/{id}", vehicleId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("X5 M"))
                .andExpect(jsonPath("$.vehicleStatus").value("MAINTENANCE"));

        // 4. Verify vehicle is in maintenance status
        mockMvc.perform(get("/api/vehicles/{id}", vehicleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleStatus").value("MAINTENANCE"));

        // 5. Delete the vehicle
        // We expect a 500 error since there are no reservations for this vehicle
        mockMvc.perform(delete("/api/vehicles/{id}", vehicleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(500));

        // 6. Vehicle should still exist since deletion failed
        mockMvc.perform(get("/api/vehicles/{id}", vehicleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId));
    }

    @AfterEach
    void cleanup() {
        // Clear security context
        SecurityContextHolder.clearContext();
        it.linksmt.rental.security.SecurityContext.set(null);
    }

    @AfterAll
    void finalCleanup() {
        reservationRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();
    }
}