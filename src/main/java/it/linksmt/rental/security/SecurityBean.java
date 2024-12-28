package it.linksmt.rental.security;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityBean {
    private Long id;
    private String username;
//    private String name;
//    private String surname;
//    private String email;
//    private String password;
//    private int age;
//    private UserType userType;
      private Collection<? extends GrantedAuthority> authorities;

}
