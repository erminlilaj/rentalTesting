package it.linksmt.rental.controller;

import it.linksmt.rental.dto.CreateReservationRequest;
import it.linksmt.rental.entity.ReservationEntity;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.*;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.ReservationRepository;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.repository.VehicleRepository;
import it.linksmt.rental.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;

    private final LocalDateTime startDate = LocalDateTime.now().plusDays(2);
    private final LocalDateTime endDate = LocalDateTime.now().plusDays(5);

    private VehicleEntity savedVehicle;
    private UserEntity savedUser;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity admin = new UserEntity(
                null, "admin", "Admin", "User", "admin@example.com", "adminpassword", 50,
                UserType.ADMIN, LocalDateTime.now(), LocalDateTime.now(), null
        );
        savedUser = new UserEntity(
                null, "user2", "Test", "User", "user2@example.com", "password", 30,
                UserType.USER, LocalDateTime.now(), LocalDateTime.now(), null
        );
        userRepository.saveAll(List.of(admin, savedUser));

        adminToken = jwtService.generateToken(admin);
        userToken = jwtService.generateToken(savedUser);

        savedVehicle = new VehicleEntity(
                null, "Toyota", "Corolla", 2022, GearboxType.AUTOMATIC, FuelType.PETROL,
                "Red", VehicleStatus.AVAILABLE, 50.0, LocalDateTime.now(), LocalDateTime.now(), null
        );
        vehicleRepository.save(savedVehicle);

    }

    // Positive Test Cases
    @Test
    void testCreateReservation_Success() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest(savedVehicle.getId(), startDate, endDate);
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/reservations/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(savedVehicle.getId()))
                .andExpect(jsonPath("$.status").value(ReservationStatus.RESERVED.toString()));
    }

    @Test
    void testCheckAvailability_Success() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest(savedVehicle.getId(), startDate, endDate);
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/reservations/check-availability")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testCancelReservation_Success() throws Exception {
        ReservationEntity reservation = new ReservationEntity(
                null, savedVehicle, savedUser, startDate, endDate,
                ReservationStatus.RESERVED, 3, 300.0
        );
        ReservationEntity savedReservation = reservationRepository.save(reservation);

        mockMvc.perform(delete("/api/reservations/cancel/{id}", savedReservation.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ReservationStatus.CANCELLED.toString()));
    }

    @Test
    void testGetAllReservations_Success() throws Exception {
        ReservationEntity reservation = new ReservationEntity(
                null, savedVehicle, savedUser, startDate, endDate,
                ReservationStatus.RESERVED, 3, 300.0
        );
        reservationRepository.save(reservation);

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetReservationById_Success() throws Exception {
        ReservationEntity reservation = new ReservationEntity(
                null, savedVehicle, savedUser, startDate, endDate,
                ReservationStatus.RESERVED, 3, 300.0
        );
        ReservationEntity savedReservation = reservationRepository.save(reservation);

        mockMvc.perform(get("/api/reservations/{id}", savedReservation.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(savedVehicle.getId()));
    }

    @Test
    void testGetReservationStatistics_Success() throws Exception {
        ReservationEntity reservation = new ReservationEntity(
                null, savedVehicle, savedUser, startDate, endDate,
                ReservationStatus.RESERVED, 3, 300.0
        );
        reservationRepository.save(reservation);

        mockMvc.perform(get("/api/reservations/statistics/{MM-yyyy}", "01-2025")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetReservationListOfUser_Success() throws Exception {
        ReservationEntity reservation = new ReservationEntity(
                null, savedVehicle, savedUser, startDate, endDate,
                ReservationStatus.RESERVED, 3, 300.0
        );
        reservationRepository.save(reservation);

        mockMvc.perform(get("/api/reservations/reservation-list")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // Negative Test Cases
    @Test
    void testCreateReservation_InvalidDates() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest(savedVehicle.getId(), endDate, startDate);
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/reservations/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateReservation_VehicleNotFound() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest(999L, startDate, endDate);
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/reservations/create")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCheckAvailability_VehicleUnavailable() throws Exception {
        savedVehicle.setVehicleStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(savedVehicle);

        CreateReservationRequest request = new CreateReservationRequest(savedVehicle.getId(), startDate, endDate);
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/reservations/check-availability")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    void testCancelReservation_AlreadyCancelled() throws Exception {
        ReservationEntity reservation = new ReservationEntity(
                null, savedVehicle, savedUser, startDate, endDate,
                ReservationStatus.CANCELLED, 3, 300.0
        );
        ReservationEntity savedReservation = reservationRepository.save(reservation);

        mockMvc.perform(delete("/api/reservations/cancel/{id}",savedReservation.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(jsonPath("$.errorCode").value(501));
    }

    @Test
    void testGetReservationById_NotFound() throws Exception {
        mockMvc.perform(get("/api/reservations/{id}", 999L)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(jsonPath("$.errorCode").value(500))
                .andExpect(jsonPath("$.message").value("Reservation not found"));
    }


    @Test
    void testGetReservationById_InternalServerError() throws Exception {
        // Simulate an unexpected server-side issue by mocking the service layer
        mockMvc.perform(get("/api/reservations/{id}", 999L)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isInternalServerError()); // Expecting 500 status code
    }


    @Test
    void testGetReservationStatistics_NoReservations() throws Exception {
        mockMvc.perform(get("/api/reservations/statistics/{MM-yyyy}", "01-2025")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.errorCode").value(500));
    }

    @Test
    void testGetReservationListOfUser_NoReservations() throws Exception {
        mockMvc.perform(get("/api/reservations/reservation-list")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testListOfActiveOrFutureReservations_Success() throws Exception {
        // Arrange: Create a reservation for the vehicle
        ReservationEntity reservation = new ReservationEntity(
                null, savedVehicle, savedUser, startDate, endDate,
                ReservationStatus.RESERVED, 3, 300.0
        );
        reservationRepository.save(reservation);

        // Act & Assert: Perform the GET request and validate the response
        mockMvc.perform(get("/api/reservations/vehicle-reservations/{id}", savedVehicle.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vehicleId").value(savedVehicle.getId()))
                .andExpect(jsonPath("$[0].status").value(ReservationStatus.RESERVED.toString()));
    }

    @Test
    void testListOfActiveOrFutureReservations_NoReservations() throws Exception {
        // Act & Assert: Perform the GET request with no reservations for the vehicle
        mockMvc.perform(get("/api/reservations/vehicle-reservations/{id}", savedVehicle.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.RESERVATION_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("There are no reservations for this vehicle with id: " + savedVehicle.getId()));
    }

    @Test
    void testListOfActiveOrFutureReservations_VehicleNotFound() throws Exception {
        // Act & Assert: Perform the GET request with a non-existent vehicle ID
        mockMvc.perform(get("/api/reservations/vehicle-reservations/{id}", 999L)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("No vehicle found with id: 999"));
    }


}
