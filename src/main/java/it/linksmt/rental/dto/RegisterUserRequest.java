package it.linksmt.rental.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.linksmt.rental.enums.UserType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {
    @NotNull
    private String username;
    @NotNull
    private String name;
    @NotNull
    private String surname;
    @NotNull
    private String email;
    @NotNull
    private String password;
    @Min(value = 0, message = "Age cannot be negative")
    private int age;

}
