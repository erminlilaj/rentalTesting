//package it.linksmt.rental.systemTesting;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import it.linksmt.rental.dto.CreateReservationRequest;
//import it.linksmt.rental.dto.LoginUserRequest;
//import it.linksmt.rental.dto.RegisterUserRequest;
//import it.linksmt.rental.entity.VehicleEntity;
//import it.linksmt.rental.enums.*;
//import it.linksmt.rental.repository.ReservationRepository;
//import it.linksmt.rental.repository.UserRepository;
//import it.linksmt.rental.repository.VehicleRepository;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public class UserFunctionalityE2ETest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private VehicleRepository vehicleRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ReservationRepository reservationRepository;
//
//    private static String authToken;
//    private static Long userId;
//    private VehicleEntity vehicle;
//    private final LocalDateTime startDate = LocalDateTime.now().plusDays(2);
//    private final LocalDateTime endDate = LocalDateTime.now().plusDays(5);
//
//    @BeforeAll
//    void setUp() throws Exception {
//        // Clean up existing test data
//        reservationRepository.deleteAll();
//     vehicleRepository.deleteAll();
//      userRepository.deleteAll();
//        // Create a test vehicle
//        vehicle = new VehicleEntity(
//                null,
//                "Toyota",
//                "Corolla",
//                2023,
//                GearboxType.AUTOMATIC,
//                FuelType.PETROL,
//                "Red",
//                VehicleStatus.AVAILABLE,
//                100.0,
//                LocalDateTime.now(),
//                LocalDateTime.now(),
//                null
//        );
//        vehicle = vehicleRepository.save(vehicle);
//
//        // Register user and get token
//        RegisterUserRequest registerRequest = new RegisterUserRequest(
//                "e2etestuser",
//                "E2E",
//                "Test",
//                "e2etest@example.com",
//                "testpassword",
//                30
//        );
//
//        // Register user
//        MvcResult registrationResult = mockMvc.perform(post("/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(registerRequest)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        JsonNode registeredUser = objectMapper.readTree(registrationResult.getResponse().getContentAsString());
//        userId = registeredUser.get("id").asLong();
//
//        // Login to get token
//        LoginUserRequest loginRequest = new LoginUserRequest(
//                "e2etestuser",
//                "testpassword"
//        );
//
//        MvcResult loginResult = mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        authToken = "Bearer " + loginResult.getResponse().getContentAsString().replace("\"", "");
//    }
//
//    @AfterAll
//    void cleanup() {
////        reservationRepository.deleteAll();
////        vehicleRepository.deleteAll();
////        userRepository.deleteAll();
//    }
//
//    @Test
//    @Order(1)
//    void testUserAuthentication() throws Exception {
//        // Verify user ID endpoint
//        MvcResult userIdResult = mockMvc.perform(get("/auth/userId")
//                        .header("Authorization", authToken))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//
//        Long retrievedUserId = Long.parseLong(userIdResult.getResponse().getContentAsString());
//        assertEquals(userId, retrievedUserId, "Retrieved user ID should match registered user ID");
//    }
//
//    @Test
//    @Order(2)
//    void testCheckAvailability() throws Exception {
//        CreateReservationRequest request = new CreateReservationRequest(
//                vehicle.getId(),
//                startDate,
//                endDate
//        );
//
//        mockMvc.perform(post("/api/reservations/check-availability")
//                        .header("Authorization", authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").value(true));
//    }
//
//    @Test
//    @Order(3)
//    void testCreateReservation() throws Exception {
//        CreateReservationRequest request = new CreateReservationRequest(
//                vehicle.getId(),
//                startDate,
//                endDate
//        );
//
//        mockMvc.perform(post("/api/reservations/create")
//                        .header("Authorization", authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.vehicleId").value(vehicle.getId()))
//                .andExpect(jsonPath("$.status").value("RESERVED"));
//    }
//
//    @Test
//    @Order(4)
//    void testGetUserReservations() throws Exception {
//        mockMvc.perform(get("/api/reservations/reservation-list")
//                        .header("Authorization", authToken))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(1))
//                .andExpect(jsonPath("$[0].status").value("RESERVED"));
//    }
//
//    @Test
//    @Order(5)
//    void testCancelReservation() throws Exception {
//        // Get the reservation ID from the list
//        MvcResult result = mockMvc.perform(get("/api/reservations/reservation-list")
//                        .header("Authorization", authToken))
//                .andReturn();
//
//        JsonNode reservations = objectMapper.readTree(result.getResponse().getContentAsString());
//        Long reservationId = reservations.get(0).get("reservationId").asLong();
//
//        // Cancel the reservation
//        mockMvc.perform(delete("/api/reservations/cancel/{id}", reservationId)
//                        .header("Authorization", authToken))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("CANCELLED"));
//    }
//}

package it.linksmt.rental.systemTesting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.linksmt.rental.dto.*;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.*;
import it.linksmt.rental.security.SecurityBean;
import it.linksmt.rental.repository.ReservationRepository;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.repository.VehicleRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserFunctionalityE2ETest {

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

    private String userToken;
    private Long userId;
    private VehicleEntity testVehicle;
    private Long reservationId;
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private final LocalDateTime startDate = LocalDateTime.now().plusDays(2);
    private final LocalDateTime endDate = LocalDateTime.now().plusDays(5);

    @BeforeAll
    void setup() throws Exception {
        // Clean start
        reservationRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        // Create test vehicle
        testVehicle = new VehicleEntity();
        testVehicle.setBrand("Toyota");
        testVehicle.setModel("Corolla");
        testVehicle.setYear(2023);
        testVehicle.setGearboxType(GearboxType.AUTOMATIC);
        testVehicle.setFuelType(FuelType.PETROL);
        testVehicle.setColor("Red");
        testVehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        testVehicle.setDailyFee(100.0);
        testVehicle.setCreatedAt(LocalDateTime.now());
        testVehicle.setUpdatedAt(LocalDateTime.now());
        testVehicle = vehicleRepository.save(testVehicle);

        // Create user directly in DB
        UserEntity user = new UserEntity();
        user.setUsername(USERNAME);
        user.setName("Test");
        user.setSurname("User");
        user.setEmail("test@example.com");
        String salt = "salt";
        String hashedPassword = passwordEncoder.encode(PASSWORD + salt);
        user.setPassword(hashedPassword);
        user.setAge(30);
        user.setUserType(UserType.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        userId = user.getId();

        // Set up security context
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(USERNAME, null, Collections.singletonList(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Set up SecurityBean
        SecurityBean securityBean = new SecurityBean();
        securityBean.setAuthorities(Collections.singletonList(authority));
        securityBean.setUsername(USERNAME);
        it.linksmt.rental.security.SecurityContext.set(securityBean);

        // Login to get token
        LoginUserRequest loginRequest = new LoginUserRequest(USERNAME, PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        userToken = loginResult.getResponse().getContentAsString();
        userToken = userToken.replace("\"", ""); // Remove quotes

        assertNotNull(userToken, "User token should not be null");
    }

    @Test
    @Order(1)
    void completeReservationFlow() throws Exception {
        // 1. Verify user authentication
        MvcResult userIdResult = mockMvc.perform(get("/auth/userId")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();

        Long retrievedUserId = Long.parseLong(userIdResult.getResponse().getContentAsString());
        assertEquals(userId, retrievedUserId, "Retrieved user ID should match created user ID");

        // 2. Check vehicle availability
        CreateReservationRequest checkRequest = new CreateReservationRequest(
                testVehicle.getId(),
                startDate,
                endDate
        );

        mockMvc.perform(post("/api/reservations/check-availability")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        // 3. Create reservation
        MvcResult createResult = mockMvc.perform(post("/api/reservations/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(testVehicle.getId()))
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andReturn();

        JsonNode reservation = objectMapper.readTree(createResult.getResponse().getContentAsString());
        reservationId = reservation.get("reservationId").asLong();

        // 4. Get user's reservations
        mockMvc.perform(get("/api/reservations/reservation-list")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("RESERVED"))
                .andExpect(jsonPath("$[0].reservationId").value(reservationId));

        // 5. Cancel reservation
        mockMvc.perform(delete("/api/reservations/cancel/{id}", reservationId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // 6. Verify reservation is cancelled
        mockMvc.perform(get("/api/reservations/reservation-list")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    @AfterEach
    void cleanup() {
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