package it.linksmt.rental.dto;

import it.linksmt.rental.enums.FuelType;
import it.linksmt.rental.enums.GearboxType;
import it.linksmt.rental.enums.VehicleStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVehicleRequest {
    @NotNull
    private String brand;
    @NotNull
    private String model;
    @Min(value = 0, message = "Year cannot be negativee")
    private int year;
    private GearboxType gearboxType;
    private FuelType fuelType;
    private String color;
    @NotNull
    private VehicleStatus vehicleStatus;
    @Min(value = 0, message = "Fee cannot be negative")
    private double dailyFee;

}
