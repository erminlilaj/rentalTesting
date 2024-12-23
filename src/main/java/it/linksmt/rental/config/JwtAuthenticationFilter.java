package it.linksmt.rental.config;

import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.security.SecurityBean;
import it.linksmt.rental.security.SecurityContext;
import it.linksmt.rental.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


            final String jwt = authHeader.substring(7);

            final String userUsername = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userUsername != null && authentication == null) {
                UserEntity userDetails = (UserEntity) this.userDetailsService.loadUserByUsername(userUsername);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    SecurityBean securityBean = new SecurityBean();
                    securityBean.setId(userDetails.getId());
                    securityBean.setUsername(userDetails.getUsername());
//                    securityBean.setName(userDetails.getName());
//                    securityBean.setSurname(userDetails.getSurname());
//                    securityBean.setEmail(userDetails.getEmail());
//                    securityBean.setPassword(userDetails.getPassword());
//                    securityBean.setAge(userDetails.getAge());
//                    securityBean.setUserType(userDetails.getUserType());
                      securityBean.setAuthorities(userDetails.getAuthorities());


                    SecurityContext.set(securityBean);


                }
            }

            filterChain.doFilter(request, response);

    }
}