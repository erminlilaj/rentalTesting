package it.linksmt.rental.dto;




import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReservationRequest {

    private Long vehicleId;

    @NotNull(message = "Start date cannot be null")
   // @DateTimeFormat(pattern = "dd/MM/yyyy")
//@FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDateTime startDate;

    @NotNull(message = "End date cannot be null")
    //@DateTimeFormat(pattern = "dd/MM/yyyy")
   // @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

}
