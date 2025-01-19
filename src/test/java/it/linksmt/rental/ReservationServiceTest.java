package it.linksmt.rental;

import it.linksmt.rental.dto.CreateReservationRequest;
import it.linksmt.rental.dto.ReservationResponse;
import it.linksmt.rental.dto.ReservationStatisticsResponse;
import it.linksmt.rental.entity.ReservationEntity;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.ErrorCode;
import it.linksmt.rental.enums.ReservationStatus;
import it.linksmt.rental.enums.VehicleStatus;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.ReservationRepository;
import it.linksmt.rental.repository.projections.ReservationStatisticsProjection;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.serviceImpl.ReservationServiceImpl;
import it.linksmt.rental.service.serviceImpl.UserServiceImpl;
import it.linksmt.rental.service.serviceImpl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VehicleServiceImpl vehicleService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private AuthenticationService authenticationService;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime startDate = now.plusDays(2);
    private final LocalDateTime endDate = now.plusDays(5);

    @BeforeEach
    void setUp() {
        // This ensures mocks are properly initialized for each test
    }

    @Test
    void testCreateReservation_Success() {
        // Arrange
        CreateReservationRequest request = new CreateReservationRequest(1L, startDate, endDate);
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.AVAILABLE);
        UserEntity user = new UserEntity(1L, "JohnDoe", "John", "Doe", "john.doe@example.com", "password", 30);
        ReservationEntity savedReservation = new ReservationEntity(1L, vehicle, user, startDate, endDate, ReservationStatus.RESERVED, 3, 300.0);

        when(authenticationService.getCurrentUserId()).thenReturn(1L);
        when(vehicleService.getVehicleById(1L)).thenReturn(vehicle);
        when(reservationRepository.areDatesOverlapping(1L, startDate, endDate)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(user);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);

        // Act
        ReservationResponse response = reservationService.createReservation(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getVehicleId());
        assertEquals(ReservationStatus.RESERVED.toString(), response.getStatus());
        verify(reservationRepository, times(1)).save(any(ReservationEntity.class));
    }

    @Test
    void testCreateReservation_VehicleNotFound() {
        // Arrange
        CreateReservationRequest request = new CreateReservationRequest(1L, startDate, endDate);

        when(vehicleService.getVehicleById(1L)).thenReturn(null);

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.createReservation(request)
        );

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testCreateReservation_StartDateBeforeNow() {
        // Arrange
        CreateReservationRequest request = new CreateReservationRequest(1L, now.minusDays(1), endDate);
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.AVAILABLE);

        when(vehicleService.getVehicleById(1L)).thenReturn(vehicle);

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.createReservation(request)
        );

        assertEquals(ErrorCode.BAD_RESERVATION_DETAILS, exception.getErrorCode());
        assertEquals("Start date cannot be before current date", exception.getMessage());
    }

    @Test
    void testCreateReservation_EndDateBeforeStartDate() {
        // Arrange
        CreateReservationRequest request = new CreateReservationRequest(1L, endDate, startDate);
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.AVAILABLE);

        when(vehicleService.getVehicleById(1L)).thenReturn(vehicle);

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.createReservation(request)
        );

        assertEquals(ErrorCode.BAD_RESERVATION_DETAILS, exception.getErrorCode());
        assertEquals("End date cannot be before start date", exception.getMessage());
    }

    @Test
    void testCreateReservation_VehicleNotAvailable() {
        // Arrange
        CreateReservationRequest request = new CreateReservationRequest(1L, startDate, endDate);
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.AVAILABLE);

        when(vehicleService.getVehicleById(1L)).thenReturn(vehicle);
        when(reservationRepository.areDatesOverlapping(1L, startDate, endDate)).thenReturn(true);

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.createReservation(request)
        );

        assertEquals(ErrorCode.VEHICLE_NOT_AVAILABLE, exception.getErrorCode());
        assertEquals("Vehicle is not available for the selected dates", exception.getMessage());
    }

    @Test
    void testCancelReservation_Success() {
        // Arrange
        UserEntity user = new UserEntity(1L, "JohnDoe", "John", "Doe", "john.doe@example.com", "password", 30);
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.AVAILABLE);
        ReservationEntity reservation = new ReservationEntity(1L, vehicle, user, startDate, endDate, ReservationStatus.RESERVED, 3, 300.0);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> {
            ReservationEntity saved = invocation.getArgument(0);
            saved.setStatus(ReservationStatus.CANCELLED); // Simulate the status update
            return saved;
        });

        // Act
        ReservationResponse response = reservationService.cancelReservation(1L);

        // Assert
        assertNotNull(response);
        assertEquals(ReservationStatus.CANCELLED.toString(), response.getStatus());
        verify(reservationRepository, times(1)).save(any(ReservationEntity.class));
    }

    @Test
    void testCancelReservation_ReservationNotFound() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.cancelReservation(1L)
        );

        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
        assertEquals("Reservation not found", exception.getMessage());
    }

    @Test
    void testCancelReservation_AlreadyCancelled() {
        // Arrange
        UserEntity user = new UserEntity(1L, "JohnDoe", "John", "Doe", "john.doe@example.com", "password", 30);
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.AVAILABLE);
        ReservationEntity reservation = new ReservationEntity(1L, vehicle, user, startDate, endDate, ReservationStatus.CANCELLED, 3, 300.0);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.cancelReservation(1L)
        );

        assertEquals(ErrorCode.RESERVATION_IS_CANCELLED_OR_ONGOING, exception.getErrorCode());
        assertEquals("Reservation is already cancelled", exception.getMessage());
    }

    @Test
    void testGetReservationById_InvalidId() {
        // Arrange
        when(reservationRepository.findById(100L)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.getReservationById(100L)
        );

        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
        assertEquals("Reservation not found", exception.getMessage());
    }
    @Test
    void testListOfActiveOrFutureReservations_Success() {
        // Arrange
        Long vehicleId = 1L;
        LocalDateTime currentTime = LocalDateTime.now();
        UserEntity user = new UserEntity(1L, "JohnDoe", "John", "Doe", "john.doe@example.com", "password", 30);
        VehicleEntity vehicle = new VehicleEntity(vehicleId, "Toyota", "Corolla", VehicleStatus.AVAILABLE);
        ReservationEntity reservation = new ReservationEntity(1L, vehicle, user, startDate, endDate, ReservationStatus.RESERVED, 3, 300.0);

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(vehicle);
        when(reservationRepository.listOfActiveOrFutureReservations(eq(vehicleId), any(LocalDateTime.class)))
                .thenReturn(List.of(reservation));

        // Act
        List<ReservationResponse> responses = reservationService.listOfActiveOrFutureReservations(vehicleId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(vehicleId, responses.get(0).getVehicleId());
        verify(vehicleService, times(1)).getVehicleById(vehicleId);
        verify(reservationRepository, times(1)).listOfActiveOrFutureReservations(eq(vehicleId), any(LocalDateTime.class));
    }



    @Test
    void testListOfActiveOrFutureReservations_NoReservations() {
        // Arrange
        Long vehicleId = 1L;
        VehicleEntity vehicle = new VehicleEntity(vehicleId, "Toyota", "Corolla", VehicleStatus.AVAILABLE);

        when(vehicleService.getVehicleById(vehicleId)).thenReturn(vehicle);
        when(reservationRepository.listOfActiveOrFutureReservations(eq(vehicleId), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.listOfActiveOrFutureReservations(vehicleId)
        );

        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
        assertEquals("There are no reservations for this vehicle with id: " + vehicleId, exception.getMessage());
        verify(vehicleService, times(1)).getVehicleById(vehicleId);
        verify(reservationRepository, times(1)).listOfActiveOrFutureReservations(eq(vehicleId), any(LocalDateTime.class));
    }



    @Test
    void testListOfActiveOrFutureReservations_VehicleNotFound() {
        // Arrange
        when(vehicleService.getVehicleById(1L)).thenReturn(null);

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.listOfActiveOrFutureReservations(1L)
        );

        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, exception.getErrorCode());
        assertEquals("Vehicle not found", exception.getMessage());
    }


    @Test
    void testCheckAvailability_VehicleNotAvailable() {
        // Arrange
        CreateReservationRequest request = new CreateReservationRequest(1L, startDate, endDate);
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.MAINTENANCE);

        when(vehicleService.getVehicleById(1L)).thenReturn(vehicle);

        // Act
        boolean isAvailable = reservationService.checkAvailability(request);

        // Assert
        assertFalse(isAvailable);
    }

    @Test
    void testCancelReservationsOfVehicle_Success() {
        // Arrange
        Long vehicleId = 1L;
        LocalDateTime fixedNow = LocalDateTime.now();
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.AVAILABLE);
        UserEntity user = new UserEntity(1L, "JohnDoe", "John", "Doe", "john.doe@example.com", "password", 30);
        ReservationEntity reservation = new ReservationEntity(1L, vehicle, user, startDate, endDate, ReservationStatus.RESERVED, 3, 300.0);

        when(reservationRepository.listOfActiveOrFutureReservations(vehicleId, fixedNow)).thenReturn(List.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> {
            ReservationEntity saved = invocation.getArgument(0);
            saved.setStatus(ReservationStatus.CANCELLED);
            return saved;
        });

        try (MockedStatic<LocalDateTime> mockedNow = mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(fixedNow);

            // Act
            List<ReservationResponse> responses = reservationService.cancelReservationsOfVehicle(vehicleId);

            // Assert
            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals(ReservationStatus.CANCELLED.toString(), responses.get(0).getStatus());
            assertEquals(vehicleId, responses.get(0).getVehicleId());
            verify(reservationRepository, times(1)).listOfActiveOrFutureReservations(vehicleId, fixedNow);
            verify(reservationRepository, times(1)).save(any(ReservationEntity.class));
        }
    }

    @Test
    void testCancelReservationsOfVehicle_NoReservations() {
        // Arrange
        Long vehicleId = 1L;
        LocalDateTime fixedNow = LocalDateTime.now();

        when(reservationRepository.listOfActiveOrFutureReservations(vehicleId, fixedNow)).thenReturn(List.of());

        try (MockedStatic<LocalDateTime> mockedNow = mockStatic(LocalDateTime.class)) {
            mockedNow.when(LocalDateTime::now).thenReturn(fixedNow);

            // Act & Assert
            ServiceException exception = assertThrows(
                    ServiceException.class,
                    () -> reservationService.cancelReservationsOfVehicle(vehicleId)
            );

            assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
            assertEquals("There are no reservations for this vehicle with id: " + vehicleId, exception.getMessage());
            verify(reservationRepository, times(1)).listOfActiveOrFutureReservations(vehicleId, fixedNow);
            verify(reservationRepository, never()).save(any(ReservationEntity.class));
        }
    }

    @Test
    void testIsReservationOngoingOrCompleted_True() {
        // Arrange
        Long reservationId = 1L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(new ReservationEntity()));
        when(reservationRepository.isReservationOngoingOrCompleted(eq(reservationId), any(LocalDateTime.class)))
                .thenReturn(true);

        // Act
        boolean result = reservationService.isReservationOngoingOrCompleted(reservationId);

        // Assert
        assertTrue(result);
        verify(reservationRepository, times(1)).isReservationOngoingOrCompleted(eq(reservationId), any(LocalDateTime.class));
    }

    @Test
    void testIsReservationOngoingOrCompleted_NotFound() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.isReservationOngoingOrCompleted(1L)
        );

        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testGetReservationStatistics_Success() {
        // Arrange
        String givenDate = "01-2025";
        LocalDateTime startOfMonth = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endOfMonth = LocalDateTime.of(2025, 1, 31, 23, 59, 59).withNano(999_999_999);

        when(reservationRepository.areReservationsForMonth(eq(startOfMonth), eq(endOfMonth)))
                .thenReturn(true);

        // Mock projections
        ReservationStatisticsProjection completed = mock(ReservationStatisticsProjection.class);
        ReservationStatisticsProjection ongoing = mock(ReservationStatisticsProjection.class);
        ReservationStatisticsProjection cancelled = mock(ReservationStatisticsProjection.class);

        when(reservationRepository.completedReservationsWithStats(eq(startOfMonth), eq(endOfMonth), any()))
                .thenReturn(completed);
        when(reservationRepository.ongoingReservationsWithStats(eq(startOfMonth), eq(endOfMonth), any()))
                .thenReturn(ongoing);
        when(reservationRepository.cancelledReservationsWithStats(eq(startOfMonth), eq(endOfMonth)))
                .thenReturn(cancelled);

        // Act
        List<ReservationStatisticsResponse> stats = reservationService.getReservationStatistics(givenDate);

        // Assert
        assertNotNull(stats);
        assertEquals(3, stats.size());
        verify(reservationRepository, times(1)).areReservationsForMonth(eq(startOfMonth), eq(endOfMonth));
    }

    @Test
    void testCreateReservation_NullRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> reservationService.createReservation(null));
    }

    @Test
    void testCheckAvailability_DatesOverlap() {
        // Arrange
        CreateReservationRequest request = new CreateReservationRequest(1L, startDate, endDate);
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.AVAILABLE);

        when(vehicleService.getVehicleById(1L)).thenReturn(vehicle);
        when(reservationRepository.areDatesOverlapping(1L, startDate, endDate)).thenReturn(true);

        // Act
        boolean isAvailable = reservationService.checkAvailability(request);

        // Assert
        assertFalse(isAvailable);
    }

    @Test
    void testGetReservationListOfUser_NoReservations() {
        // Arrange
        when(authenticationService.getCurrentUserId()).thenReturn(1L);
        when(reservationRepository.findUsersReservations(1L)).thenReturn(List.of());

        // Act
        List<ReservationResponse> responses = reservationService.getReservationListOfUser();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void testGetReservationListOfUser_WithReservations() {
        // Arrange
        Long userId = 1L;
        when(authenticationService.getCurrentUserId()).thenReturn(userId);
        UserEntity user = new UserEntity(1L, "JohnDoe", "John", "Doe", "john.doe@example.com", "password", 30);
        VehicleEntity vehicle = new VehicleEntity(1L, "Toyota", "Corolla", VehicleStatus.AVAILABLE);
        ReservationEntity reservation = new ReservationEntity(1L, vehicle, user, startDate, endDate, ReservationStatus.RESERVED, 3, 300.0);
        when(reservationRepository.findUsersReservations(userId)).thenReturn(List.of(reservation));

        // Act
        List<ReservationResponse> responses = reservationService.getReservationListOfUser();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(vehicle.getId(), responses.get(0).getVehicleId());
    }

    @Test
    void testGetReservationStatistics_NoReservations() {
        // Arrange
        String givenDate = "01-2025";
        LocalDateTime startOfMonth = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endOfMonth = LocalDateTime.of(2025, 1, 31, 23, 59, 59).withNano(999_999_999);

        when(reservationRepository.areReservationsForMonth(eq(startOfMonth), eq(endOfMonth)))
                .thenReturn(false);

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.getReservationStatistics(givenDate)
        );

        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
        assertEquals("There are no reservations for the given month", exception.getMessage());
    }

    @Test
    void testSaveReservationDetails_CorrectCalculations() {
        // Arrange
        Long vehicleId = 1L;
        Long userId = 1L;
        double dailyPrice = 100.0; // Price per day
        int duration = 3; // Duration in days

        // Mock vehicle
        VehicleEntity vehicle = new VehicleEntity(vehicleId, "Toyota", "Corolla", VehicleStatus.AVAILABLE);
        when(vehicleService.getVehiclePrice(vehicleId)).thenReturn(dailyPrice);

        // Mock user
        UserEntity user = new UserEntity(userId, "JohnDoe", "John", "Doe", "john.doe@example.com", "password", 30);
        when(userService.getUserById(userId)).thenReturn(user);

        // Mock repository save
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CreateReservationRequest request = new CreateReservationRequest(vehicleId, startDate, endDate);
        ReservationEntity result = reservationService.saveReservationDetails(request, vehicle, userId);

        // Assert
        assertNotNull(result, "The saved reservation entity should not be null");
        assertEquals(vehicle, result.getVehicle(), "The vehicle should match the input");
        assertEquals(user, result.getUser(), "The user should match the input");
        assertEquals(duration, result.getDurationDays(), "The duration should match the expected value");
        assertEquals(dailyPrice * duration, result.getTotalPrice(), "The total price should be calculated correctly");
    }


    @Test
    void testGetReservationStatistics_InvalidDateFormat() {
        // Arrange
        String invalidDate = "2025-01";

        // Act & Assert
        assertThrows(DateTimeParseException.class, () -> reservationService.getReservationStatistics(invalidDate));
    }

    @Test
    void testCancelReservation_OngoingOrCompleted() {
        // Arrange
        ReservationEntity reservation = new ReservationEntity();
        reservation.setStatus(ReservationStatus.COMPLETED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.isReservationOngoingOrCompleted(eq(1L), any(LocalDateTime.class))).thenReturn(true);

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reservationService.cancelReservation(1L)
        );

        assertEquals(ErrorCode.RESERVATION_IS_CANCELLED_OR_ONGOING, exception.getErrorCode());
        assertEquals("Cannot cancel an ongoing or completed reservation", exception.getMessage());
    }
}
