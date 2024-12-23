package it.linksmt.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationStatisticsResponse {
    private String month;
    private int year;
    private String status;
    private int count;
    private double profit;
}
