package it.linksmt.rental;

import it.linksmt.rental.dto.ReservationResponse;
import it.linksmt.rental.service.ReservationService;
import it.linksmt.rental.service.VehicleService;
import it.linksmt.rental.service.serviceImpl.VehicleBusinessImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleBusinessImplTest {

    @InjectMocks
    private VehicleBusinessImpl vehicleBusiness;

    @Mock
    private ReservationService reservationService;

    @Mock
    private VehicleService vehicleService;

    @Test
    void testDeleteVehicle_Success() {
        // Arrange
        Long vehicleId = 1L;
        ReservationResponse reservationResponse = new ReservationResponse();
        List<ReservationResponse> mockResponse = List.of(reservationResponse);

        when(vehicleService.deleteVehicle(vehicleId)).thenReturn(true);
        when(reservationService.cancelReservationsOfVehicle(vehicleId))
                .thenReturn(mockResponse);

        // Act
        List<ReservationResponse> response = vehicleBusiness.deleteVehicle(vehicleId);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(vehicleService, times(1)).deleteVehicle(vehicleId);
        verify(reservationService, times(2)).cancelReservationsOfVehicle(vehicleId);
    }

    @Test
    void testDeleteVehicle_Failure() {
        // Arrange
        Long vehicleId = 1L;
        when(vehicleService.deleteVehicle(vehicleId))
                .thenThrow(new RuntimeException("Vehicle deletion failed"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            vehicleBusiness.deleteVehicle(vehicleId);
        });

        assertEquals("Vehicle deletion failed", exception.getMessage());
        verify(vehicleService, times(1)).deleteVehicle(vehicleId);
        verify(reservationService, never()).cancelReservationsOfVehicle(vehicleId);
    }

    @Test
    void testDeleteVehicle_NoReservations() {
        // Arrange
        Long vehicleId = 1L;
        List<ReservationResponse> emptyResponse = new ArrayList<>();

        when(vehicleService.deleteVehicle(vehicleId)).thenReturn(true);
        when(reservationService.cancelReservationsOfVehicle(vehicleId))
                .thenReturn(emptyResponse);

        // Act
        List<ReservationResponse> response = vehicleBusiness.deleteVehicle(vehicleId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(vehicleService, times(1)).deleteVehicle(vehicleId);
        verify(reservationService, times(2)).cancelReservationsOfVehicle(vehicleId);
    }

    @Test
    void testDeleteVehicle_ReservationServiceFailure() {
        // Arrange
        Long vehicleId = 1L;
        when(vehicleService.deleteVehicle(vehicleId)).thenReturn(true);
        when(reservationService.cancelReservationsOfVehicle(vehicleId))
                .thenThrow(new RuntimeException("Reservation cancellation failed"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            vehicleBusiness.deleteVehicle(vehicleId);
        });

        assertEquals("Reservation cancellation failed", exception.getMessage());
        verify(vehicleService, times(1)).deleteVehicle(vehicleId);
        verify(reservationService, times(1)).cancelReservationsOfVehicle(vehicleId);
    }

    @Test
    void testActivateOrFutureReservationOfVehicle_Success() {
        // Arrange
        Long vehicleId = 1L;
        ReservationResponse reservationResponse = new ReservationResponse();
        List<ReservationResponse> mockResponse = List.of(reservationResponse);

        when(reservationService.listOfActiveOrFutureReservations(vehicleId))
                .thenReturn(mockResponse);

        // Act
        List<ReservationResponse> response = vehicleBusiness.activateOrFutureReservationOfVehicle(vehicleId);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(reservationService, times(1)).listOfActiveOrFutureReservations(vehicleId);
    }

    @Test
    void testActivateOrFutureReservationOfVehicle_NoReservations() {
        // Arrange
        Long vehicleId = 1L;
        List<ReservationResponse> emptyList = new ArrayList<>();

        when(reservationService.listOfActiveOrFutureReservations(vehicleId))
                .thenReturn(emptyList);

        // Act
        List<ReservationResponse> response = vehicleBusiness.activateOrFutureReservationOfVehicle(vehicleId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(reservationService, times(1)).listOfActiveOrFutureReservations(vehicleId);
    }

    @Test
    void testActivateOrFutureReservationOfVehicle_ServiceFailure() {
        // Arrange
        Long vehicleId = 1L;
        when(reservationService.listOfActiveOrFutureReservations(vehicleId))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            vehicleBusiness.activateOrFutureReservationOfVehicle(vehicleId);
        });

        assertEquals("Service unavailable", exception.getMessage());
        verify(reservationService, times(1)).listOfActiveOrFutureReservations(vehicleId);
    }
}