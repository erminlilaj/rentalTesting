package it.linksmt.rental;

import it.linksmt.rental.dto.CreateVehicleRequest;
import it.linksmt.rental.dto.UpdateVehicleRequest;
import it.linksmt.rental.dto.VehicleResponse;
import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.ErrorCode;
import it.linksmt.rental.enums.FuelType;
import it.linksmt.rental.enums.GearboxType;
import it.linksmt.rental.enums.VehicleStatus;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.VehicleRepository;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.serviceImpl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class vehicleServiceTest {

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateVehicle_Success() {
        // Arrange
        CreateVehicleRequest request = new CreateVehicleRequest();
        request.setBrand("Toyota");
        request.setModel("Corolla");
        request.setYear(2020);
        request.setGearboxType(GearboxType.AUTOMATIC);
        request.setFuelType(FuelType.DIESEL);
        request.setColor("Red");
        request.setVehicleStatus(VehicleStatus.AVAILABLE);
        request.setDailyFee(100);

        VehicleEntity savedVehicle = new VehicleEntity();
        savedVehicle.setId(1L);
        savedVehicle.setBrand("Toyota");
        savedVehicle.setModel("Corolla");

        when(authenticationService.isAdmin()).thenReturn(true);
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(savedVehicle);

        // Act
        VehicleEntity result = vehicleService.createVehicle(request);

        // Assert
        assertNotNull(result);
        assertEquals("Toyota", result.getBrand());
        assertEquals("Corolla", result.getModel());
        verify(vehicleRepository, times(1)).save(any(VehicleEntity.class));
    }

    @Test
    void testCreateVehicle_Unauthorized() {
        // Arrange
        CreateVehicleRequest request = new CreateVehicleRequest();
        when(authenticationService.isAdmin()).thenReturn(false);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.createVehicle(request));
        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
    }

    @Test
    void testCreateVehicle_InvalidYear() {
        // Arrange
        CreateVehicleRequest request = new CreateVehicleRequest();
        request.setYear(1970);
        when(authenticationService.isAdmin()).thenReturn(true);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.createVehicle(request));
        assertEquals(ErrorCode.BAD_VEHICLE_DETAILS, exception.getErrorCode());
    }

    @Test
    void testFindAllVehicle_Success() {
        // Arrange
        VehicleEntity vehicle = new VehicleEntity();
        when(vehicleRepository.findCurrentVehicles()).thenReturn(List.of(vehicle));
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));

        // Act
        List<VehicleEntity> result = vehicleService.findAllVehicle();

        // Assert
        assertFalse(result.isEmpty());
    }

    @Test
    void testFindAllVehicle_NoVehicles() {
        // Arrange
        when(vehicleRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.findAllVehicle());
        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testFindVehicleById_Success() {
        // Arrange
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // Act
        VehicleResponse result = vehicleService.findVehicleById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindVehicleById_NotFound() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.findVehicleById(1L));
        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testDeleteVehicle_Success() {
        // Arrange
        when(authenticationService.isAdmin()).thenReturn(true);
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        VehicleEntity vehicle = new VehicleEntity();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // Act
        boolean result = vehicleService.deleteVehicle(1L);

        // Assert
        assertTrue(result);
        verify(vehicleRepository, times(1)).save(any(VehicleEntity.class));
    }

    @Test
    void testDeleteVehicle_Unauthorized() {
        // Arrange
        when(authenticationService.isAdmin()).thenReturn(false);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.deleteVehicle(1L));
        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
    }

    @Test
    void testDeleteVehicle_NotFound() {
        // Arrange
        when(authenticationService.isAdmin()).thenReturn(true);
        when(vehicleRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.deleteVehicle(1L));
        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testUpdateVehicle_Success() {
        // Arrange
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setModel("NewModel");
        request.setColor("Blue");
        request.setDailyFee(120.0);
        request.setVehicleStatus(VehicleStatus.AVAILABLE);

        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(1L);
        when(authenticationService.isAdmin()).thenReturn(true);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(VehicleEntity.class))).thenReturn(vehicle);

        // Act
        VehicleEntity result = vehicleService.updateVehicle(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals("NewModel", result.getModel());
        assertEquals("Blue", result.getColor());
        verify(vehicleRepository, times(1)).save(any(VehicleEntity.class));
    }

    @Test
    void testUpdateVehicle_Unauthorized() {
        // Arrange
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        when(authenticationService.isAdmin()).thenReturn(false);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.updateVehicle(1L, request));
        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
    }

    @Test
    void testUpdateVehicle_NotFound() {
        // Arrange
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        when(authenticationService.isAdmin()).thenReturn(true);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.updateVehicle(1L, request));
        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testGetVehiclePrice_Success() {
        // Arrange
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setDailyFee(150);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // Act
        double result = vehicleService.getVehiclePrice(1L);

        // Assert
        assertEquals(150, result);
    }

    @Test
    void testGetVehiclePrice_NotFound() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.getVehiclePrice(1L));
        assertEquals(ErrorCode.VEHICLE_NOT_FOUND, exception.getErrorCode());
    }
}