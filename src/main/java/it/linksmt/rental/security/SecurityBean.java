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

    private Collection<? extends GrantedAuthority> authorities;

    public SecurityBean(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public Object getCurrentUser() {
        return SecurityContext.get();
    }
}
