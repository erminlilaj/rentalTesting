package it.linksmt.rental.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @NotNull
    private String username;
    @NotNull
    private String password;
    @Min(value = 0, message = "Age cannot be negative")
    private int age;
}
