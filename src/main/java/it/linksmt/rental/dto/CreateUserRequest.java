package it.linksmt.rental.dto;


import it.linksmt.rental.enums.UserType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
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

    private UserType userType = UserType.USER;

    public CreateUserRequest(String username, String name, String surname, String email, String password, int age) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.age = age;
    }

}
