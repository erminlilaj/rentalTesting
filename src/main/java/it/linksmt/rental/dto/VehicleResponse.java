package it.linksmt.rental.dto;

import it.linksmt.rental.enums.FuelType;
import it.linksmt.rental.enums.GearboxType;
import it.linksmt.rental.enums.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponse {
    private Long id;
    private String brand;
    private String model;
    private int year;
    private GearboxType gearboxType;
    private FuelType fuelType;
    private String color;
    private VehicleStatus vehicleStatus;
    private double dailyFee;
}
