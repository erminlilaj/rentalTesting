package it.linksmt.rental.systemTesting;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EndToEndSystemTest {

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
    private String userToken;
    private Long vehicleId;
    private Long userId;
    private Long reservationId;

    private static final String ADMIN_USERNAME = "admintest";
    private static final String ADMIN_PASSWORD = "adminpass123";
    private static final String USER_USERNAME = "usertest";
    private static final String USER_PASSWORD = "userpass123";

    @BeforeAll
    void initialSetup() throws Exception {
        // Clean start
        reservationRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin
        UserEntity adminUser = new UserEntity();
        adminUser.setUsername(ADMIN_USERNAME);
        adminUser.setName("Admin");
        adminUser.setSurname("Test");
        adminUser.setEmail("admin@test.com");
        String salt = "salt";
        adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD + salt));
        adminUser.setAge(35);
        adminUser.setUserType(UserType.ADMIN);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(adminUser);

        // Create regular user
        UserEntity regularUser = new UserEntity();
        regularUser.setUsername(USER_USERNAME);
        regularUser.setName("Regular");
        regularUser.setSurname("User");
        regularUser.setEmail("user@test.com");
        regularUser.setPassword(passwordEncoder.encode(USER_PASSWORD + salt));
        regularUser.setAge(25);
        regularUser.setUserType(UserType.USER);
        regularUser.setCreatedAt(LocalDateTime.now());
        regularUser.setUpdatedAt(LocalDateTime.now());
        regularUser = userRepository.save(regularUser);
        userId = regularUser.getId();
    }

    @BeforeEach
    void setupAuth() throws Exception {
        // Set up security context for admin
        List<SimpleGrantedAuthority> adminAuthorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        UsernamePasswordAuthenticationToken adminAuthentication =
                new UsernamePasswordAuthenticationToken(ADMIN_USERNAME, null, adminAuthorities);
        SecurityContextHolder.getContext().setAuthentication(adminAuthentication);

        SecurityBean adminSecurityBean = new SecurityBean();
        adminSecurityBean.setAuthorities(adminAuthorities);
        adminSecurityBean.setUsername(ADMIN_USERNAME);
        it.linksmt.rental.security.SecurityContext.set(adminSecurityBean);

        // Get fresh tokens
        // Admin token
        LoginUserRequest adminLoginRequest = new LoginUserRequest(ADMIN_USERNAME, ADMIN_PASSWORD);
        MvcResult adminLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = adminLoginResult.getResponse().getContentAsString().replace("\"", "");

        // User token
        LoginUserRequest userLoginRequest = new LoginUserRequest(USER_USERNAME, USER_PASSWORD);
        MvcResult userLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        userToken = userLoginResult.getResponse().getContentAsString().replace("\"", "");

        // Verify admin privileges
        MvcResult isAdminResult = mockMvc.perform(get("/auth/isAdmin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("true", isAdminResult.getResponse().getContentAsString());
    }

    @Test
    @Order(1)
    void testCompleteFlow() throws Exception {
        // 1. Admin creates vehicle
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
                .andReturn();

        VehicleEntity createdVehicle = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                VehicleEntity.class
        );
        vehicleId = createdVehicle.getId();
        assertNotNull(vehicleId, "Vehicle ID should not be null");

        // 2. User makes reservation
        CreateReservationRequest reservationRequest = new CreateReservationRequest(
                vehicleId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)
        );

        // Check availability
        mockMvc.perform(post("/api/reservations/check-availability")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        // Create reservation
        MvcResult reservationResult = mockMvc.perform(post("/api/reservations/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(vehicleId))
                .andReturn();

        JsonNode reservation = objectMapper.readTree(reservationResult.getResponse().getContentAsString());
        reservationId = reservation.get("reservationId").asLong();

        // 3. User cancels reservation
        mockMvc.perform(delete("/api/reservations/cancel/{id}", reservationId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // 4. Admin tries to delete vehicle
        mockMvc.perform(delete("/api/vehicles/{id}", vehicleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(500));
    }

    @AfterEach
    void cleanupSecurity() {
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