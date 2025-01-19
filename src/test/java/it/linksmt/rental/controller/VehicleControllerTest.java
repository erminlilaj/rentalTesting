package it.linksmt.rental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.linksmt.rental.dto.CreateVehicleRequest;
import it.linksmt.rental.dto.ReservationResponse;
import it.linksmt.rental.dto.UpdateVehicleRequest;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.*;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.repository.VehicleRepository;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.JwtService;
import it.linksmt.rental.service.VehicleBusinessLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private VehicleBusinessLayer vehicleBusinessLayer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String userToken;
    private VehicleEntity testVehicle;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();

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

        // Mock the behavior of authenticationService.isAdmin()
        when(authenticationService.isAdmin()).thenReturn(true);

        // Generate JWT token for the admin user
        adminToken = jwtService.generateToken(adminUser);

        // Create a test vehicle
        testVehicle = new VehicleEntity();
        testVehicle.setBrand("Toyota");
        testVehicle.setModel("Camry");
        testVehicle.setYear(2022);
        testVehicle.setGearboxType(GearboxType.AUTOMATIC);
        testVehicle.setFuelType(FuelType.DIESEL);
        testVehicle.setColor("Black");
        testVehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        testVehicle.setDailyFee(100.0);

        testVehicle = vehicleRepository.save(testVehicle);
    }

    @Test
    void createVehicleTest() throws Exception {
        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(
                "Honda",
                "Civic",
                2023,
                GearboxType.AUTOMATIC,
                FuelType.DIESEL,
                "Red",
                VehicleStatus.AVAILABLE,
                90.0
        );
        String requestBody = objectMapper.writeValueAsString(createVehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("Honda"))
                .andExpect(jsonPath("$.model").value("Civic"))
                .andExpect(jsonPath("$.gearboxType").value("AUTOMATIC"))
                .andExpect(jsonPath("$.fuelType").value("DIESEL"));
    }

    @Test
    void getAllVehiclesTest() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].brand").value("Toyota"))
                .andExpect(jsonPath("$[0].model").value("Camry"));
    }

    @Test
    void getVehicleByIdTest() throws Exception {
        mockMvc.perform(get("/api/vehicles/{id}", testVehicle.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.gearboxType").value("AUTOMATIC"))
                .andExpect(jsonPath("$.fuelType").value("DIESEL"));
    }

    @Test
    void deleteVehicleTest() throws Exception {
        // Setup mock response
        List<ReservationResponse> mockReservations = new ArrayList<>();
        when(vehicleBusinessLayer.deleteVehicle(testVehicle.getId())).thenReturn(mockReservations);

        // Perform delete request
        mockMvc.perform(delete("/api/vehicles/{id}", testVehicle.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify vehicleBusinessLayer.deleteVehicle was called
        verify(vehicleBusinessLayer).deleteVehicle(testVehicle.getId());
    }

    @Test
    void deleteVehicle_WithReservationsTest() throws Exception {
        // Setup mock response with some reservations
        List<ReservationResponse> mockReservations = Arrays.asList(
                new ReservationResponse(/* add necessary fields */),
                new ReservationResponse(/* add necessary fields */)
        );
        when(vehicleBusinessLayer.deleteVehicle(testVehicle.getId())).thenReturn(mockReservations);

        // Perform delete request
        mockMvc.perform(delete("/api/vehicles/{id}", testVehicle.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify vehicleBusinessLayer.deleteVehicle was called
        verify(vehicleBusinessLayer).deleteVehicle(testVehicle.getId());
    }

    @Test
    void deleteVehicle_ErrorTest() throws Exception {
        // Setup mock to throw exception
        when(vehicleBusinessLayer.deleteVehicle(testVehicle.getId()))
                .thenThrow(new ServiceException(ErrorCode.VEHICLE_NOT_FOUND, "Vehicle not found"));

        mockMvc.perform(delete("/api/vehicles/{id}", testVehicle.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateVehicleTest() throws Exception {
        UpdateVehicleRequest updateVehicleRequest = new UpdateVehicleRequest(
                "Corolla",
                "Blue",
                95.0,
                VehicleStatus.MAINTENANCE
        );
        String requestBody = objectMapper.writeValueAsString(updateVehicleRequest);

        mockMvc.perform(put("/api/vehicles/{id}", testVehicle.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.color").value("Blue"))
                .andExpect(jsonPath("$.vehicleStatus").value("MAINTENANCE"));
    }

    @Test
    void createVehicle_ValidationFailureTest() throws Exception {
        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(
                null, // brand is @NotNull
                "Civic",
                -2023, // year cannot be negative
                GearboxType.AUTOMATIC,
                FuelType.DIESEL,
                "Red",
                VehicleStatus.AVAILABLE,
                -90.0 // dailyFee cannot be negative
        );
        String requestBody = objectMapper.writeValueAsString(createVehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getVehicleById_NotFoundTest() throws Exception {
        mockMvc.perform(get("/api/vehicles/{id}", 999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }



    @Test
    void createVehicle_NoTokenTest() throws Exception {
        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(
                "Honda",
                "Civic",
                2023,
                GearboxType.AUTOMATIC,
                FuelType.DIESEL,
                "Red",
                VehicleStatus.AVAILABLE,
                90.0
        );
        String requestBody = objectMapper.writeValueAsString(createVehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void createVehicle_ForbiddenTest() throws Exception {
        // Mock non-admin user
        when(authenticationService.isAdmin()).thenReturn(false);

        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(
                "Honda",
                "Civic",
                2023,
                GearboxType.AUTOMATIC,
                FuelType.DIESEL,
                "Red",
                VehicleStatus.AVAILABLE,
                90.0
        );
        String requestBody = objectMapper.writeValueAsString(createVehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }
}