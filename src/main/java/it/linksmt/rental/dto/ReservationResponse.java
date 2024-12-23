package it.linksmt.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationResponse {
    private Long reservationId;
    private Long userId;
    private Long vehicleId;
    private String vehicleName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private int duration;
    private double price;

}
