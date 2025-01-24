package it.linksmt.rental.bvt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.ErrorCode;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.service.serviceImpl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.time.Year;

class VehicleServiceImplBoundaryTest {

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void minimumValidYear_createsVehicleSuccessfully() {
        // Arrange
        int year = 1980; // Minimum valid year
        double dailyFee = 1.0; // Minimum valid daily fee

        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setYear(year);
        vehicle.setDailyFee(dailyFee);

        // Act & Assert
        assertDoesNotThrow(() -> vehicleService.createVehicle(year, dailyFee));
    }

    @Test
    void maximumValidYear_createsVehicleSuccessfully() {
        // Arrange
        int year = Year.now().getValue(); // Maximum valid year (current year)
        double dailyFee = 1.0; // Minimum valid daily fee

        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setYear(year);
        vehicle.setDailyFee(dailyFee);

        // Act & Assert
        assertDoesNotThrow(() -> vehicleService.createVehicle(year, dailyFee));
    }

    @Test
    void invalidYear_justBelow_throwsException() {
        // Arrange
        int year = 1979; // Just below valid range
        double dailyFee = 10.0; // Valid daily fee

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.createVehicle(year, dailyFee));
        assertEquals("The year must be between 1980 and currentYear", exception.getMessage());
    }

    @Test
    void invalidYear_justAbove_throwsException() {
        // Arrange
        int year = Year.now().getValue() + 1; // Just above valid range
        double dailyFee = 10.0; // Valid daily fee

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> vehicleService.createVehicle(year, dailyFee));
        assertEquals("The year must be between 1980 and currentYear", exception.getMessage());
    }

    @Test
    void minimumValidFee_createsVehicleSuccessfully() {
        // Arrange
        int year = 1990; // Valid year
        double dailyFee = 0.01; // Minimum valid daily fee

        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setYear(year);
        vehicle.setDailyFee(dailyFee);

        // Act & Assert
        assertDoesNotThrow(() -> vehicleService.createVehicle(year, dailyFee));
    }

}

