package it.linksmt.rental.dto;

import it.linksmt.rental.enums.VehicleStatus;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVehicleRequest {

    private String model;
    private String color;
    @Min(value = 0, message = "Daily fee cannot be negative")
    private Double dailyFee;
    private VehicleStatus vehicleStatus;

}
